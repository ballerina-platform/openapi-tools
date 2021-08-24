/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.generators.client;

import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * All the tests related to the Header sections in the swagger file.
 */
public class HeadersTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for header that comes under the parameter section")
    public void getHeaderTests() throws IOException, BallerinaOpenApiException, FormatterException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/header_parameter.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_parameter.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }

    @Test(description = "Test for header that comes under the parameter section")
    public void getHeaderTestsWithoutParameter() throws IOException, BallerinaOpenApiException, FormatterException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/header_without_parameter.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_without_parameter.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }
    @Test(description = "Test for header with default values")
    public void getHeaderTestsWithDefaultValues() throws IOException, BallerinaOpenApiException, FormatterException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/header_param_with_default_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_param_with_default_value.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }
}
