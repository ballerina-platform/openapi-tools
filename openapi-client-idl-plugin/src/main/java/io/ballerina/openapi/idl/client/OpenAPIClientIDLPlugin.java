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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
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
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorUtils.modifySchemaContent;
import static io.ballerina.openapi.core.GeneratorUtils.normalizeOpenAPI;

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

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            // Check given contract is valid for the generating the client.
//            Path oasPath = idlSourceGeneratorContext.resourcePath();
            // resource with yaml, json extension
            String oasPath = "/home/hansani/ballerina_projects/openapi-features/idl_import/idl_01/openapi.yaml";
            try {
//                OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(Path.of(oasPath.toString() + ".yaml"));
                OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(Path.of(oasPath));
            } catch (IOException | BallerinaOpenApiException | NullPointerException e) {
                return false;
            }
            return true;
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceContext) {

//            Path oasPath = idlSourceContext.resourcePath();
            String oasPath = "/home/hansani/ballerina_projects/openapi-features/idl_import/idl_01/openapi.yaml";
            ModuleClientDeclarationNode clientNode = (ModuleClientDeclarationNode) idlSourceContext.clientNode();
            OASClientIDLMetaData clientIDLMetaData = extractAnnotationDetails(clientNode);
            try {
                List<GenSrcFile> genSrcFiles = generateClientFiles(Path.of(oasPath), clientIDLMetaData);
                ModuleId moduleId = ModuleId.create(Path.of(oasPath).getFileName().toString(),
                        idlSourceContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();
                genSrcFiles.stream().forEach(genSrcFile -> {
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
                        default:
                            break;
                    }

                });
                // Take spec name
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceContext.currentPackage().packageName(),
                                Path.of(oasPath).getFileName().toString()),
                        idlSourceContext.currentPackage().descriptor());
                ModuleConfig moduleConfig =
                        ModuleConfig.from(moduleId, moduleDescriptor, documents, Collections.emptyList(), null,
                                new ArrayList<>());
                idlSourceContext.addClient(moduleConfig);
            } catch (IOException | BallerinaOpenApiException | FormatterException e) {
                // Ignore need to handle with proper error handle
                // Error while generating the client as warning
            }
        }

        private List<GenSrcFile> generateClientFiles(Path openAPI, OASClientIDLMetaData clientMetaData)
                throws IOException, BallerinaOpenApiException, FormatterException {

            Filter filter = new Filter(
                    clientMetaData.getTags().isPresent() ? clientMetaData.getTags().get() : new ArrayList<>(),
                    clientMetaData.getOperations().isPresent() ? clientMetaData.getOperations().get() :
                            new ArrayList<>());

            List<GenSrcFile> sourceFiles = new ArrayList<>();
            // Normalize OpenAPI definition
            OpenAPI openAPIDef = normalizeOpenAPI(openAPI, !clientMetaData.isResource());
            // Generate ballerina service and resources.
            BallerinaClientGenerator ballerinaClientGenerator =
                    new BallerinaClientGenerator(openAPIDef, filter, clientMetaData.isNullable(),
                            clientMetaData.isResource());
            String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, null, CLIENT_FILE_NAME, mainContent));
            String utilContent = Formatter.format(
                    ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();
            if (!utilContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, null, UTIL_FILE_NAME, utilContent));
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
                        schemaContent));
            }

            // Generate test boilerplate code for test cases
//            if (this.includeTestFiles) {
//                BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(ballerinaClientGenerator);
//                String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toString();
//                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME, testContent));
//
//                String configContent = ballerinaTestGenerator.getConfigTomlFile();
//                if (!configContent.isBlank()) {
//                    sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
//                            CONFIG_FILE_NAME, configContent));
//                }
//            }

            return sourceFiles;
        }
    }

    private static OASClientIDLMetaData extractAnnotationDetails(ModuleClientDeclarationNode clientNode) {

        OASClientIDLMetaData.OASClientIDLMetaDataBuilder clientMetaDataBuilder =
                new OASClientIDLMetaData.OASClientIDLMetaDataBuilder();

        NodeList<AnnotationNode> annotations = clientNode.annotations();
        for (AnnotationNode annotationNode : annotations) {
            Node refNode = annotationNode.annotReference();
            boolean isNodeExist = refNode.toString().trim().equals("openapi:ClientInfo");
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
                    values = setFilters(list);
                }
                Node fieldName = specificField.fieldName();
                String attributeName = ((Token) fieldName).text();
                switch (attributeName) {
                    case "tags":
                        clientMetaDataBuilder.withTags(values);
                        break;
                    case "operations":
                        clientMetaDataBuilder.withOperations(values);
                        break;
                    case "nullable":
                        clientMetaDataBuilder.withNullable(expression.toString().contains("true"));
                        break;
                    case "isResource":
                        clientMetaDataBuilder.withIsResource(expression.toString().contains("true"));
                        break;
                    case "withTests":
                        clientMetaDataBuilder.withTest(expression.toString().contains("true"));
                        break;
                    default:
                        break;
                }
            }
        }
        ;
        return clientMetaDataBuilder.build();
    }

    private static List<String> setFilters(ListConstructorExpressionNode list) {

        SeparatedNodeList<Node> expressions = list.expressions();
        Iterator<Node> iterator = expressions.iterator();
        List<String> values = new ArrayList<>();
        while (iterator.hasNext()) {
            Node item = iterator.next();
            if (item.kind() == SyntaxKind.STRING_LITERAL && !item.toString().isBlank()) {
                Token stringItem = ((BasicLiteralNode) item).literalToken();
                String text = stringItem.text();
                // Here we need to do some preprocessing by removing '"' from the given values.
                if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
                    text = text.substring(1, text.length() - 1);
                } else {
                    // Missing end quote case
                    text = text.substring(1);
                }
                values.add(text);
            }
        }
        return values;
    }
}
