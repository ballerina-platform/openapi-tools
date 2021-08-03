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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * {@code Generator} generates open-api related docs for HTTP service.
 */
public class Generator {
    private static final String OPEN_API_FILE = "openapi-doc.json";
    private static final String RESOURCE_PATH_FILE = "resource-path.txt";
    private static final String RESOURCE_PATH_FORMAT = "openapi-doc-%s";
    private static final PrintStream OUT_STREAM = System.err;

    public void generate(String projectRoot, SemanticModel semanticModel, SyntaxTree syntaxTree) {
        try {
            // generate and add the open-api doc to given path
            Path openApiDocPath = Paths.get(projectRoot, "target", "resources", OPEN_API_FILE);
            Map<String, String> openAPIDefinitions = ServiceToOpenAPIConverterUtils
                    .generateOAS3Definition(syntaxTree, semanticModel, "", true, openApiDocPath);
            if (!openAPIDefinitions.isEmpty()) {
                for (Map.Entry<String, String> definition: openAPIDefinitions.entrySet()) {
                        CodegenUtils.writeFile(openApiDocPath.resolve(definition.getKey()), definition.getValue());
                }
            }

            // generate random resource path to HTTP introspection resource, add it to resource-path file and add that
            // file to given path
            Path introspectionResourceFilePath = Paths.get(projectRoot, "target, resources", RESOURCE_PATH_FILE);
            String randomResourcePathSegment = CodegenUtils.generateRandomAlphaNumericString(10);
            String introspectionResource = String.format(RESOURCE_PATH_FORMAT, randomResourcePathSegment);
            CodegenUtils.writeFile(introspectionResourceFilePath, introspectionResource);
        } catch (IOException | OpenApiConverterException e) {
            OUT_STREAM.println(e.getLocalizedMessage());
        }
    }
}
