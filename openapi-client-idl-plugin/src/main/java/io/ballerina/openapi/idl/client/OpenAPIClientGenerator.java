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
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.IDLClientGenerator;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorUtils.normalizeOpenAPI;
import static io.ballerina.openapi.idl.client.Constants.IS_RESOURCE;
import static io.ballerina.openapi.idl.client.Constants.LICENSE;
import static io.ballerina.openapi.idl.client.Constants.NULLABLE;
import static io.ballerina.openapi.idl.client.Constants.OPENAPI_CLIENT_REFERENCE;
import static io.ballerina.openapi.idl.client.Constants.OPENAPI_REGEX_PATTERN;
import static io.ballerina.openapi.idl.client.Constants.OPERATIONS;
import static io.ballerina.openapi.idl.client.Constants.TAGS;
import static io.ballerina.openapi.idl.client.Constants.TRUE;

/**
 * The IDL plugin implementation for OpenAPI client generation.
 *
 * @since 1.3.0
 */
public class OpenAPIClientGenerator extends IDLClientGenerator {

    @Override
    public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
        // check given contract is valid for the generating the client.
        Path oasPath = idlSourceGeneratorContext.resourcePath();
        // resource with yaml, json extension
        return isOpenAPI(oasPath, idlSourceGeneratorContext);
    }

    @Override
    public void perform(IDLSourceGeneratorContext idlSourceContext) {

        try {
            List<GenSrcFile> genSrcFiles = generateClientFiles(idlSourceContext);
            if (genSrcFiles.isEmpty()) {
                return;
            }
            String moduleName = getAlias(idlSourceContext.clientNode());
            ModuleId moduleId = ModuleId.create(moduleName, idlSourceContext.currentPackage().packageId());
            List<DocumentConfig> documents = new ArrayList<>();

            genSrcFiles.forEach(genSrcFile -> {
                DocumentId documentId = DocumentId.create(genSrcFile.getFileName(), moduleId);
                DocumentConfig documentConfig = DocumentConfig.from(
                        documentId, genSrcFile.getContent(), genSrcFile.getFileName());
                documents.add(documentConfig);

            });

            ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                    ModuleName.from(idlSourceContext.currentPackage().packageName(), moduleName),
                    idlSourceContext.currentPackage().descriptor());
            ModuleConfig moduleConfig =
                    ModuleConfig.from(moduleId, moduleDescriptor, documents, new ArrayList<>(), null,
                            new ArrayList<>());
            NodeList<AnnotationNode> annotations = getAnnotationNodes(idlSourceContext);
            //only pass openapi related annotation node.
            List<AnnotationNode> openapiAnnot = filterOpenAPIAnnotation(annotations);

            idlSourceContext.addClient(moduleConfig, openapiAnnot.isEmpty() ? NodeFactory.createEmptyNodeList() :
                    NodeFactory.createNodeList(openapiAnnot));
        } catch (BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.PARSER_ERROR;
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), e.getMessage(),
                    error.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    idlSourceContext.clientNode().location());
            idlSourceContext.reportDiagnostic(diagnostic);
        } catch (IOException | FormatterException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
            reportDiagnostic(idlSourceContext, error, idlSourceContext.clientNode().location());
        }
    }

    /**
     * This method uses to check whether given specification can be handled via the openapi client generation tool.
     * This includes basic requirements like file extension check and file header check.
     */
    private static boolean isOpenAPI(Path oasPath, IDLSourceGeneratorContext context) {

        try {
            if (!(oasPath.toString().endsWith(YAML_EXTENSION) || oasPath.toString().endsWith(JSON_EXTENSION) ||
                    oasPath.toString().endsWith(YML_EXTENSION))) {
                return false;
            }
            String content = Files.readString(oasPath);
            Pattern openapiPattern = Pattern.compile(OPENAPI_REGEX_PATTERN);
            Matcher openapiMatcher = openapiPattern.matcher(content);
            return openapiMatcher.find();
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    /**
     * This function uses to filter @openapi:ClientConfig annotation from given annotation list.
     */
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

    /**
     * This method uses to generate ballerina files for openapi client stub.
     * This will return list of (client.bal, util.bal, types.bal) {@code GenSrcFile}.
     */
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
        OASClientConfig oasClientConfig = extractClientDetails(context, openAPI, annotations);


        List<GenSrcFile> sourceFiles = new ArrayList<>();

        // generate ballerina client files.
        String licenseContent = oasClientConfig.getLicense();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, null, CLIENT_FILE_NAME,
                licenseContent == null || licenseContent.isBlank() ? mainContent :
                        licenseContent + System.lineSeparator() + mainContent));
        String utilContent = Formatter.format(
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();

        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, null, UTIL_FILE_NAME,
                    licenseContent == null || licenseContent.isBlank() ? utilContent :
                            licenseContent + System.lineSeparator() + utilContent));
        }

        // generate ballerina records to represent schemas.
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        typeDefinitionNodeList.addAll(ballerinaClientGenerator.getTypeDefinitionNodeList());
        typeDefinitionNodeList.addAll(ballerinaClientGenerator
                .getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(oasClientConfig.getOpenAPI(),
                oasClientConfig.isNullable(), typeDefinitionNodeList);
        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (oasClientConfig.getFilters().getTags().size() > 0) {
            // remove unused records and enums when generating the client by the tags given.
            schemaContent = GeneratorUtils.removeUnusedEntities(schemaSyntaxTree, mainContent, schemaContent,
                    null);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, null, TYPE_FILE_NAME,
                    licenseContent == null || licenseContent.isBlank() ? schemaContent :
                            licenseContent + System.lineSeparator() + schemaContent));
        }

        return sourceFiles;
    }

    /**
     * This method extracts openapi client config details.
     * ex: openapi annotation
     * <pre>
     * public type ClientConfig record {|
     *     string[]? tags = [];
     *     string[]? operations = [];
     *     boolean nullable = true;
     *     boolean isResource = true;
     *     string license?;
     * |};
     * </pre>
     *
     * @param annotations - Client node annotation list
     * @return {@code ClientMetaData} model with all the metadata to generate client.
     */
    private static OASClientConfig extractClientDetails(IDLSourceGeneratorContext context, Path openAPI,
                                                        NodeList<AnnotationNode> annotations)
            throws IOException, BallerinaOpenApiException {

        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        clientMetaDataBuilder.withPlugin(true);
        Filter filter = new Filter();
        if (annotations == null) {
            // normalize OpenAPI definition.
            OpenAPI openAPIDef = normalizeOpenAPI(openAPI, false);
            clientMetaDataBuilder.withOpenAPI(openAPIDef);
            return clientMetaDataBuilder.build();
        }
        boolean isResources = true;
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
                        filter.setTags(values);
                        break;
                    case OPERATIONS:
                        filter.setOperations(values);
                        break;
                    case NULLABLE:
                        clientMetaDataBuilder.withNullable(expression.toString().contains(TRUE));
                        break;
                    case IS_RESOURCE:
                        isResources = expression.toString().contains(TRUE);
                        clientMetaDataBuilder.withResourceMode(isResources);
                        break;
                    case LICENSE:
                        clientMetaDataBuilder.withLicense(
                                expression.kind() == SyntaxKind.STRING_LITERAL && !expression.toString().isBlank() ?
                                        getLicenseContent(context,
                                                Paths.get(getStringValue((BasicLiteralNode) expression))) : "");
                        break;
                    default:
                        break;
                }
            }
        }
        // normalize OpenAPI definition.
        clientMetaDataBuilder.withFilters(filter);
        OpenAPI openAPIDef = normalizeOpenAPI(openAPI, !isResources);
        clientMetaDataBuilder.withOpenAPI(openAPIDef);

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
     * Util to normalize the string value by removing extra " " from given field value.
     */
    private static String getStringValue(BasicLiteralNode item) {

        Token stringItem = item.literalToken();
        String text = stringItem.text();
        // here we need to do some preprocessing by removing '"' from the given values.
        if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
            text = text.substring(1, text.length() - 1);
        } else {
            // missing end quote case
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
                    Constants.DiagnosticMessages error = Constants.DiagnosticMessages
                            .ERROR_WHILE_READING_LICENSE_FILE;
                    reportDiagnostic(context, error, location);
                    return null;
                }
                return licenseHeader;
            }
        } catch (IOException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_READING_LICENSE_FILE;
            reportDiagnostic(context, error, location);
            return null;
        }
        return licenseHeader;
    }

    private static void reportDiagnostic(IDLSourceGeneratorContext context, Constants.DiagnosticMessages error,
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

    private String getAlias(Node clientNode) {
        if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
            return ((ModuleClientDeclarationNode) clientNode).clientPrefix().toString();
        }
        return ((ClientDeclarationNode) clientNode).clientPrefix().toString();
    }
}
