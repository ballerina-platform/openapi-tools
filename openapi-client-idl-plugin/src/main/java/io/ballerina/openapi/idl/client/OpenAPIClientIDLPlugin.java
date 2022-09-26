/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.idl.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.ClientMetaData;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.openapi.idl.client.model.OASClientIDLMetaData;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorUtils.modifySchemaContent;
import static io.ballerina.openapi.core.GeneratorUtils.normalizeOpenAPI;
import static io.ballerina.openapi.idl.client.Constants.IS_RESOURCE;
import static io.ballerina.openapi.idl.client.Constants.LICENSE;
import static io.ballerina.openapi.idl.client.Constants.NULLABLE;
import static io.ballerina.openapi.idl.client.Constants.OPENAPI_CLIENT_REFERENCE;
import static io.ballerina.openapi.idl.client.Constants.OPERATIONS;
import static io.ballerina.openapi.idl.client.Constants.TAGS;
import static io.ballerina.openapi.idl.client.Constants.TRUE;

/**
 * IDL client generation class.
 *
 * @since 1.3.0
 */
public class OpenAPIClientIDLPlugin extends IDLGeneratorPlugin {

    @Override
    public void init(IDLPluginContext idlPluginContext) {

        idlPluginContext.addCodeGenerator(new OpenAPIClientGenerator());
    }

    private static class OpenAPIClientGenerator extends IDLClientGenerator {

        private OpenAPI openAPI = null;

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            // Check given contract is valid for the generating the client.
            Path oasPath = idlSourceGeneratorContext.resourcePath();
            // resource with yaml, json extension
            try {
                openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(oasPath);
            } catch (IOException | BallerinaOpenApiException | NullPointerException e) {
                return false;
            }
            return true;
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceContext) {
            try {
                List<GenSrcFile> genSrcFiles = generateClientFiles(idlSourceContext);
                if (genSrcFiles.isEmpty() || openAPI == null) {
                    return;
                }
                //TODO: proper module name
                String moduleName = "openapi-client";
                ModuleId moduleId = ModuleId.create(moduleName, idlSourceContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();
                List<DocumentConfig> testDocuments = new ArrayList<>();

                genSrcFiles.forEach(genSrcFile -> {
                    GenSrcFile.GenFileType fileType = genSrcFile.getType();
                    switch (fileType) {
                        case GEN_SRC:
                            DocumentId documentId = DocumentId.create(CLIENT_FILE_NAME, moduleId);
                            DocumentConfig documentConfig = DocumentConfig.from(
                                    documentId, genSrcFile.getContent(), CLIENT_FILE_NAME);
                            documents.add(documentConfig);
                            break;
                        case MODEL_SRC:
                            DocumentId typeId = DocumentId.create(TYPE_FILE_NAME, moduleId);
                            DocumentConfig typeConfig = DocumentConfig.from(
                                    typeId, genSrcFile.getContent(), TYPE_FILE_NAME);
                            documents.add(typeConfig);
                            break;
                        case UTIL_SRC:
                            DocumentId utilId = DocumentId.create(UTIL_FILE_NAME, moduleId);
                            DocumentConfig utilConfig = DocumentConfig.from(
                                    utilId, genSrcFile.getContent(), UTIL_FILE_NAME);
                            documents.add(utilConfig);
                            break;
                        case TEST_SRC:
                            DocumentId testId = DocumentId.create(TEST_FILE_NAME, moduleId);
                            DocumentConfig testConfig = DocumentConfig.from(
                                    testId, genSrcFile.getContent(), TEST_FILE_NAME);
                            testDocuments.add(testConfig);
                            break;
                        case CONFIG_SRC:
                            DocumentId configId = DocumentId.create(CONFIG_FILE_NAME, moduleId);
                            DocumentConfig configConfig = DocumentConfig.from(
                                    configId, genSrcFile.getContent(), CONFIG_FILE_NAME);
                            testDocuments.add(configConfig);
                            break;
                        default:
                            break;
                    }

                });

                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceContext.currentPackage().packageName(), moduleName),
                        idlSourceContext.currentPackage().descriptor());
                ModuleConfig moduleConfig =
                        ModuleConfig.from(moduleId, moduleDescriptor, documents, testDocuments, null,
                                new ArrayList<>());
                NodeList<AnnotationNode> annotations = getAnnotationNodes(idlSourceContext);
                //Only pass openapi related annotation node.
                List<AnnotationNode> openapiAnnot = filterOpenAPIAnnotation(annotations);

                idlSourceContext.addClient(moduleConfig, openapiAnnot.isEmpty() ? NodeFactory.createEmptyNodeList() :
                        NodeFactory.createNodeList(openapiAnnot));
            } catch (IOException | BallerinaOpenApiException | FormatterException e) {
                // Ignore need to handle with proper error handle
                // Error while generating the client as error
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
                reportDiagnostic(idlSourceContext, error, idlSourceContext.clientNode().location());
            }
        }

        private static List<AnnotationNode> filterOpenAPIAnnotation(NodeList<AnnotationNode> annotations) {

            List<AnnotationNode> openapiAnnot = new ArrayList<>();
            for (AnnotationNode annotationNode : annotations) {
                Node refNode = annotationNode.annotReference();
                boolean isNodeExist = refNode.toString().trim().equals(OPENAPI_CLIENT_REFERENCE);
                if (!isNodeExist) {
                    continue;
                }
                openapiAnnot.add(annotationNode);
            }
            return openapiAnnot;
        }

        private static NodeList<AnnotationNode> getAnnotationNodes(IDLSourceGeneratorContext idlSourceContext) {

            Node clientNode = idlSourceContext.clientNode();
            NodeList<AnnotationNode> annotations = null;
            if (clientNode instanceof ClientDeclarationNode) {
                annotations = ((ClientDeclarationNode) clientNode).annotations();
            } else if (clientNode instanceof ModuleClientDeclarationNode) {
                annotations = ((ModuleClientDeclarationNode) clientNode).annotations();
            }
            return annotations;
        }

        private List<GenSrcFile> generateClientFiles(IDLSourceGeneratorContext context)
                throws IOException, BallerinaOpenApiException, FormatterException {

            Path openAPI = context.resourcePath();

            // extract annotation details
            Node clientNode = context.clientNode();
            NodeList<AnnotationNode> annotations = null;
            if (clientNode instanceof ClientDeclarationNode) {
                annotations = ((ClientDeclarationNode) clientNode).annotations();
            } else if (clientNode instanceof ModuleClientDeclarationNode) {
                annotations = ((ModuleClientDeclarationNode) clientNode).annotations();
            }
            OASClientIDLMetaData oasClientIDLMetaData = extractAnnotationDetails(annotations);

            // create filter values (tags, operations)
            Filter filter = new Filter(
                    oasClientIDLMetaData.getTags().isPresent() ? oasClientIDLMetaData.getTags().get() :
                            new ArrayList<>(),
                    oasClientIDLMetaData.getOperations().isPresent() ? oasClientIDLMetaData.getOperations().get() :
                            new ArrayList<>());

            // set license header content
            String licenseContent = "";
            if (oasClientIDLMetaData.getLicense() != null) {
                licenseContent = getLicenseContent(context, Paths.get(oasClientIDLMetaData.getLicense()));
                if (licenseContent == null) {
                    return new ArrayList<>();
                }
            }

            List<GenSrcFile> sourceFiles = new ArrayList<>();
            // Normalize OpenAPI definition.
            OpenAPI openAPIDef = normalizeOpenAPI(openAPI, !oasClientIDLMetaData.isResource());

            // Generate ballerina client files.
            ClientMetaData.ClientMetaDataBuilder clientMetaDataBuilder = new ClientMetaData.ClientMetaDataBuilder();
            ClientMetaData clientMetaData = clientMetaDataBuilder
                    .withFilters(filter)
                    .withNullable(oasClientIDLMetaData.isNullable())
                    .withPlugin(true)
                    .withOpenAPI(openAPIDef).build();
            BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(clientMetaData);
            String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, null, CLIENT_FILE_NAME,
                    licenseContent.isBlank() ? mainContent : licenseContent + System.lineSeparator() + mainContent));
            String utilContent = Formatter.format(
                    ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();

            if (!utilContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, null, UTIL_FILE_NAME,
                        licenseContent.isBlank() ? utilContent :
                                licenseContent + System.lineSeparator() + utilContent));
            }

            // Generate ballerina records to represent schemas.
            BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef,
                    clientMetaData.isNullable());
            ballerinaSchemaGenerator.setTypeDefinitionNodeList(ballerinaClientGenerator.getTypeDefinitionNodeList());
            SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
            String schemaContent = Formatter.format(schemaSyntaxTree).toString();

            if (filter.getTags().size() > 0) {
                // Remove unused records and enums when generating the client by the tags given.
                schemaContent = modifySchemaContent(schemaSyntaxTree, mainContent, schemaContent, null);
            }
            if (!schemaContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, null, TYPE_FILE_NAME,
                        licenseContent.isBlank() ? schemaContent :
                                licenseContent + System.lineSeparator() + schemaContent));
            }

            return sourceFiles;
        }

        /**
         * Extract openapi client annotation details.
         * ex: openapi annotation
         * <pre>
         * public type ClientInformation record {|
         *     string[]? tags = [];
         *     string[]? operations = [];
         *     boolean nullable = true;
         *     boolean withTests = false;
         *     boolean isResource = true;
         *     string license?;
         * |};
         * </pre>
         *
         * @param annotations - Client node annotation list
         * @return {@code OASClientIDLMetaData} model with all the metadata to generate client.
         */
        private static OASClientIDLMetaData extractAnnotationDetails(NodeList<AnnotationNode> annotations) {

            OASClientIDLMetaData.OASClientIDLMetaDataBuilder clientMetaDataBuilder =
                    new OASClientIDLMetaData.OASClientIDLMetaDataBuilder();
            if (annotations == null) {
                return clientMetaDataBuilder.build();
            }
            for (AnnotationNode annotationNode : annotations) {
                Node refNode = annotationNode.annotReference();
                boolean isNodeExist = refNode.toString().trim().equals(OPENAPI_CLIENT_REFERENCE);
                if (!isNodeExist) {
                    continue;
                }
                Optional<MappingConstructorExpressionNode> annotFields = annotationNode.annotValue();
                if (annotFields.isEmpty()) {
                    continue;
                }
                for (MappingFieldNode field : annotFields.get().fields()) {
                    if (!(field instanceof SpecificFieldNode)) {
                        continue;
                    }
                    SpecificFieldNode specificField = (SpecificFieldNode) field;
                    Optional<ExpressionNode> expressionNode = specificField.valueExpr();
                    if (expressionNode.isEmpty()) {
                        continue;
                    }
                    ExpressionNode expression = expressionNode.get();
                    ListConstructorExpressionNode list;
                    List<String> values = new ArrayList<>();
                    if (expression instanceof ListConstructorExpressionNode) {
                        list = (ListConstructorExpressionNode) expression;
                        values = extractListValues(list);
                    }
                    Node fieldName = specificField.fieldName();
                    String attributeName = ((Token) fieldName).text();
                    switch (attributeName) {
                        case TAGS:
                            clientMetaDataBuilder.withTags(values);
                            break;
                        case OPERATIONS:
                            clientMetaDataBuilder.withOperations(values);
                            break;
                        case NULLABLE:
                            clientMetaDataBuilder.withNullable(expression.toString().contains(TRUE));
                            break;
                        case IS_RESOURCE:
                            clientMetaDataBuilder.withIsResource(expression.toString().contains(TRUE));
                            break;
                        case LICENSE:
                            clientMetaDataBuilder.withLicense(
                                    expression.kind() == SyntaxKind.STRING_LITERAL && !expression.toString().isBlank() ?
                                            getStringValue((BasicLiteralNode) expression) : "");
                            break;
                        default:
                            break;
                    }
                }
            }
            return clientMetaDataBuilder.build();
        }

        /**
         * This util is for extracting the list value from annotation field.
         */
        private static List<String> extractListValues(ListConstructorExpressionNode list) {

            SeparatedNodeList<Node> expressions = list.expressions();
            Iterator<Node> iterator = expressions.iterator();
            List<String> values = new ArrayList<>();
            while (iterator.hasNext()) {
                Node item = iterator.next();
                if (item.kind() == SyntaxKind.STRING_LITERAL && !item.toString().isBlank()) {
                    String text = getStringValue((BasicLiteralNode) item);
                    values.add(text);
                }
            }
            return values;
        }

        /**
         * Normalize the string value by removing extra " " from given field value.
         */
        private static String getStringValue(BasicLiteralNode item) {

            Token stringItem = item.literalToken();
            String text = stringItem.text();
            // Here we need to do some preprocessing by removing '"' from the given values.
            if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
                text = text.substring(1, text.length() - 1);
            } else {
                // Missing end quote case
                text = text.substring(1);
            }
            return text;
        }

        /**
         * Util to read license content.
         */
        private static String getLicenseContent(IDLSourceGeneratorContext context, Path licensePath) {

            Path relativePath = null;
            String licenseHeader = "";
            NodeLocation location = context.clientNode().location();
            Path ballerinaFilePath = extractBallerinaFilePath(context);
            if (ballerinaFilePath == null) {
                return null;
            }
            try {
                if (licensePath.toString().isBlank()) {
                    // Report Diagnostic
                    Constants.DiagnosticMessages error = Constants.DiagnosticMessages.LICENSE_PATH_BLANK;
                    reportDiagnostic(context, error, location);
                } else {
                    Path finalLicensePath = Paths.get(licensePath.toString());
                    if (finalLicensePath.isAbsolute()) {
                        relativePath = finalLicensePath;
                    } else {
                        File file = new File(ballerinaFilePath.toString());
                        File parentFolder = new File(file.getParent());
                        File openapiContract = new File(parentFolder, licensePath.toString());
                        relativePath = Paths.get(openapiContract.getCanonicalPath());
                    }
                }
                if (relativePath != null) {
                    try {
                        String newLine = System.lineSeparator();
                        Path filePath = Paths.get((new File(relativePath.toString()).getCanonicalPath()));
                        licenseHeader = Files.readString(Paths.get(filePath.toString()));
                        if (!licenseHeader.endsWith(newLine)) {
                            licenseHeader = licenseHeader + newLine + newLine;
                        } else if (!licenseHeader.endsWith(newLine + newLine)) {
                            licenseHeader = licenseHeader + newLine;
                        }
                    } catch (IOException e) {
                        // Report diagnostic
                        Constants.DiagnosticMessages error = Constants.DiagnosticMessages
                                .ERROR_WHILE_READING_LICENSE_FILE;
                        reportDiagnostic(context, error, location);
                        return null;
                    }
                    return licenseHeader;
                }
            } catch (IOException e) {
                // Report diagnostic
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_READING_LICENSE_FILE;
                reportDiagnostic(context, error, location);
                return null;
            }
            return licenseHeader;
        }

        public static void reportDiagnostic(IDLSourceGeneratorContext context, Constants.DiagnosticMessages error,
                                            Location location) {

            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), error.getDescription(),
                    error.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
            context.reportDiagnostic(diagnostic);
        }

        private static Path extractBallerinaFilePath(IDLSourceGeneratorContext idlSourceContext) {

            Package aPackage = idlSourceContext.currentPackage();
            DocumentId packageDoc = aPackage.getDefaultModule().documentIds().stream().findFirst().get();
            Optional<Path> path = aPackage.project().documentPath(packageDoc);
            return path.orElse(null);
        }
    }
}
