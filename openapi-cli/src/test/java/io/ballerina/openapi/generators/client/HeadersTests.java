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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * All the tests related to the Header sections in the swagger file.
 */
public class HeadersTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for header that comes under the parameter section")
    public void getHeaderTests() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/header_parameter.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_parameter.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for header that comes under the parameter section")
    public void getHeaderTestsWithoutParameter() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/header_without_parameter.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_without_parameter.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }


    @Test(description = "Test for header with default values")
    public void getHeaderTestsWithDefaultValues() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/header_param_with_default_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_param_with_default_value.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for optional headers without default values")
    public void getOptionalHeaderTestsWithoutDefaultValues() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/header_optional.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_optional.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for headers with delete values")
    public void getHeaderWithDeleteOperation() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/delete_with_header.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/delete_with_header.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Tests for integer type headers")
    public void testIntegerTypeHeaders() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/header_integer_signed32.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/header_integer_signed32.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    private BallerinaClientGenerator getBallerinaClientGenerator(Path definitionPath) throws IOException,
            BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        return ballerinaClientGenerator;
    }
}
