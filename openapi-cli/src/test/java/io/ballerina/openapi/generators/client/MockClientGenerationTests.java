/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.AdvanceMockClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaMockClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.mock.MockFunctionBodyGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

/**
 * Test for mock client generation.
 */
public class MockClientGenerationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/mock").toAbsolutePath();
    @Test
    public void mockClientTest() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("basic_response_example.yaml");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        String path = "/api/v1/payment_run_schedules";
        Set<Map.Entry<PathItem.HttpMethod, Operation>> pathItem = openapi.getPaths().get(path).
                readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = pathItem.iterator();
        Map.Entry<PathItem.HttpMethod, Operation> operation = iterator.next();

        MockFunctionBodyGenerator mockFunctionBodyGenerator = new MockFunctionBodyGenerator(path, operation, openapi,
                false);
        Optional<FunctionBodyNode> functionBodyNode = mockFunctionBodyGenerator.getFunctionBodyNode();
        FunctionBodyNode body = functionBodyNode.get();
        String node = body.toString();
        Assert.assertEquals("{return {\"success\":true,\"size\":3,\"schedules\":[{\"id\":6,\"status\":\"Active\",\"filter\":\"Account.BillCycleDay = 8\",\"schedule\":\"At 6:00 AM, only on Monday and Tuesday\"}]};}", node);
    }

    @Test
    public void mockClientTestWithReferenceExample() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("ref_example.json");
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/reference_example.bal");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withPlugin(false)
                .withOpenAPI(openapi)
                .withMock(true).build();
        BallerinaMockClientGenerator mockClientGenerator = new BallerinaMockClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = mockClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test
    public void mockClientForRemoteFunction() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("basic_response_example.yaml");
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/mock_client_for_remote.bal");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withPlugin(false)
                .withOpenAPI(openapi)
                .withResourceMode(false)
                .withMock(true).build();
        BallerinaMockClientGenerator mockClientGenerator = new BallerinaMockClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = mockClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test
    public void advanceMockClientGenerator() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("basic_response_example.yaml");
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/mock_client_for_advance_return_type.bal");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withPlugin(false)
                .withOpenAPI(openapi)
                .withMock(true).build();
        AdvanceMockClientGenerator mockClientGenerator = new AdvanceMockClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = mockClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

}
