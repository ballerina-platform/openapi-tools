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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
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
import java.util.List;

import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TEST_FILE_NAME;
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
            Path oasPath = Path.of("/home/hansani/ballerina_projects/openapi-features/idl_import/idl_01/openapi.yaml");
//            BasicLiteralNode pathNode =
//                    ((ModuleClientDeclarationNode) ((IDLPluginManager.IDLSourceGeneratorContextImpl) idlSourceGeneratorContext).clientNode).clientUri();
            try {
                OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(oasPath);
            } catch (IOException | BallerinaOpenApiException | NullPointerException e) {
                return false;
            }
            return true;
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {

//            Path oasPath = idlSourceGeneratorContext.resourcePath();

            // Call client generation logic
            // Filters
            // nullable
            // is resource method
            Path oasPath = Path.of("/home/hansani/ballerina_projects/openapi-features/idl_import/idl_01/openapi.yaml");
            try {
                List<GenSrcFile> genSrcFiles = generateClientFiles(oasPath, new Filter(), true, true);
                ModuleId moduleId = ModuleId.create("client1",
                        idlSourceGeneratorContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();
                genSrcFiles.stream().forEach(genSrcFile -> {
                    GenSrcFile.GenFileType fileType = genSrcFile.getType();
                    switch (fileType) {
                        case GEN_SRC:
                            DocumentId documentId = DocumentId.create("client", moduleId);
                            DocumentConfig documentConfig = DocumentConfig.from(
                                    documentId, genSrcFile.getContent(), "client");
                            documents.add(documentConfig);
                            break;
                        case MODEL_SRC:
                            DocumentId typeId = DocumentId.create("types", moduleId);
                            DocumentConfig typeConfig = DocumentConfig.from(
                                    typeId, genSrcFile.getContent(), "types");
                            documents.add(typeConfig);
                            break;
                        case UTIL_SRC:
                            DocumentId utilId = DocumentId.create("utils", moduleId);
                            DocumentConfig utilConfig = DocumentConfig.from(
                                    utilId, genSrcFile.getContent(), "utils");
                            documents.add(utilConfig);
                            break;
                        default:
                            break;
                    }

                });
//                DocumentId documentId = DocumentId.create("client", moduleId);
//                DocumentConfig documentConfig = DocumentConfig.from(
//                        documentId, "type openApiClient record {};", "client");
                // Take spec name
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), "client1"),
                        idlSourceGeneratorContext.currentPackage().descriptor());
//                ModuleConfig moduleConfig = ModuleConfig.from(
//                        moduleId, moduleDescriptor, Collections.singletonList(documentConfig),
//                        Collections.emptyList(), null, new ArrayList<>());
                ModuleConfig moduleConfig =
                        ModuleConfig.from(moduleId, moduleDescriptor, documents, Collections.emptyList(), null,
                                new ArrayList<>());
                idlSourceGeneratorContext.addClient(moduleConfig);
            } catch (IOException | BallerinaOpenApiException | FormatterException e) {
                // Ignore need to handle with proper error handle
                // Error while generating the client as warning
            }
        }

        private List<GenSrcFile> generateClientFiles(Path openAPI, Filter filter, boolean nullable, boolean isResource)
                throws IOException, BallerinaOpenApiException, FormatterException {

            List<GenSrcFile> sourceFiles = new ArrayList<>();
            // Normalize OpenAPI definition
            OpenAPI openAPIDef = normalizeOpenAPI(openAPI, !isResource);
            // Generate ballerina service and resources.
            BallerinaClientGenerator ballerinaClientGenerator =
                    new BallerinaClientGenerator(openAPIDef, filter, nullable
                            , isResource);
            String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, null, CLIENT_FILE_NAME, mainContent));
            String utilContent = Formatter.format(
                    ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();
            if (!utilContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, null, UTIL_FILE_NAME, utilContent));
            }

            // Generate ballerina records to represent schemas.
            BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef, nullable);
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
}
