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
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * All the tests related to the functionSignatureNode in {@link BallerinaClientGenerator} util when have diffrent
 * scenarios in Request Body.
 */
public class RequestBodyTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for generate request body payload when operation has request body")
    public void testForRequestBody() throws IOException, BallerinaOpenApiException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_basic_scenarios.bal");
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(
                RES_DIR.resolve("swagger/request_body_basic_scenarios.yaml"), true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }

    @Test(description = "Test for generate request body payload when operation has request body with AllOf scenarios")
    public void testForRequestBodyWithAllOf() throws IOException, BallerinaOpenApiException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_allOf_scenarios.bal");
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(
                RES_DIR.resolve("swagger/request_body_allOf_scenarios.yaml"), true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }

    @Test(description = "Test for generate request body payload when operation has request body OneOf scenarios")
    public void testForRequestBodyWithOneOf() throws IOException, BallerinaOpenApiException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_oneOf_scenarios.bal");
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(
                RES_DIR.resolve("swagger/request_body_oneOf_scenarios.yaml"), true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
        } catch (IOException e) {
            //Ignore the exception
        }
    }

}
