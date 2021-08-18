/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.extension.doc;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.openapi.extension.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/**
 * {@code Generator} generates open-api related docs for HTTP service.
 */
public class OpenApiDocGenerator {
    private static final String OPEN_API_DOC_NAME_FORMAT = "openapi-doc-%s.json";
    private static final String INTROSPECTION_RESOURCE_DETAILS_FILE_NAME = "introspection-resource-details.txt";
    private static final String INTROSPECTION_DETAILS = "%s, %s";
    private static final PrintStream ERR = System.err;

    public void generate(Path projectRoot, SemanticModel semanticModel,
                         SyntaxTree syntaxTree, ServiceDeclarationSymbol serviceSymbol) {
        try {
            String uniqueIdentifier = CodegenUtils.generateRandomAlphaNumericString(10);
            String openApiDocName = String.format(OPEN_API_DOC_NAME_FORMAT, uniqueIdentifier);

            // construct resource directory structure if not already exists
            Path resourcePath = retrieveResourcePath(projectRoot, serviceSymbol);
            File resourceDirectory = resourcePath.toFile();
            if (!resourceDirectory.exists()) {
                boolean resourceCreatingSuccessful = resourceDirectory.mkdirs();
                if (!resourceCreatingSuccessful) {
                    ERR.println("error [open-api extension]: could not create resources directory");
                    return;
                }
            }

            // generate open-api doc and add it to resource directory
            Map<String, String> openAPIDefinitions = ServiceToOpenAPIConverterUtils
                    .generateOAS3Definition(syntaxTree, semanticModel, null, true, resourcePath);
            if (!openAPIDefinitions.isEmpty()) {
                for (Map.Entry<String, String> definition : openAPIDefinitions.entrySet()) {
                    CodegenUtils.writeFile(
                            resourcePath.resolve(openApiDocName), definition.getValue(), false);
                }
            }

            // write service-symbol hash-code, open-api doc name to introspection-resource details file
            Path introspectionDetailsFilePath = resourcePath.resolve(INTROSPECTION_RESOURCE_DETAILS_FILE_NAME);
            String introspectionDetails = String.format(
                    INTROSPECTION_DETAILS, serviceSymbol.hashCode(), openApiDocName);
            CodegenUtils.writeFile(introspectionDetailsFilePath, introspectionDetails, true);
        } catch (IOException | OpenApiConverterException e) {
            ERR.println("error [open-api extension]: " + e.getLocalizedMessage());
        }
    }

    // current design for resources directory structure is as follows :
    //  <executable.jar>
    //      - [resources]
    //          - [orgName]
    //              - [moduleName]
    //                  - [version]
    //                      - [resource]
    private Path retrieveResourcePath(Path projectRoot, ServiceDeclarationSymbol serviceSymbol) {
        Path resourcesPath = projectRoot
                .resolve(Constants.TARGET_DIR_NAME)
                .resolve(Paths.get(Constants.BIN_DIR_NAME, Constants.RESOURCES_DIR_NAME));
        Optional<ModuleID> moduleIdOpt = serviceSymbol.getModule().map(ModuleSymbol::id);
        if (moduleIdOpt.isPresent()) {
            ModuleID moduleId = moduleIdOpt.get();
            String orgName = moduleId.orgName();
            String moduleName = moduleId.moduleName();
            String version = moduleId.version();
            Path relativeResourcePath = Paths.get(orgName, moduleName, version, Constants.RESOURCE_DIR_NAME);
            return resourcesPath.resolve(relativeResourcePath);
        }
        return resourcesPath.resolve(Constants.RESOURCE_DIR_NAME);
    }
}
