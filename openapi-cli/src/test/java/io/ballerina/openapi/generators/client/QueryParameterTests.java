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

import static io.ballerina.openapi.generators.common.GeneratorTestUtils
        .compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * This tests class for the tests Query parameters in swagger file.
 */
public class QueryParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "Generate Client for query parameter has default value")
    public void generateQueryParamWithDefault() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/query_param_with_default_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/query_param_with_default_value.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for query parameter without default value")
    public void generateQueryParamWithOutDefault() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/query_param_without_default_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/query_param_without_default_value.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for query parameter with referenced schema")
    public void generateQueryParamWithReferencedSchema() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/query_param_with_ref_schema.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/query_param_with_ref_schema.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate query parameters when both apikeys and http/OAuth is supported")
    public void genQueryParamsForCombinationOfApiKeyAndHTTPOrOAuth() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/combination_of_apikey_and_http_oauth.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/combination_of_apikey_and_http_oauth.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate encoding map for query parameters")
    public void genQueryParamEncodingMap() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/queryparam_encoding_map_gen.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/queryparam_encoding_map_gen.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for query parameter has integer values")
    public void testValidIntegerQueryParam() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/query_param_with_integer_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/query_param_with_integer_value.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for query parameter has content types")
    public void testContentTypeQueryParam() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/query_param_with_content_value.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/query_param_with_content_value.bal");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    private BallerinaClientGenerator getBallerinaClientGenerator(Path definitionPath) throws IOException,
            BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        return ballerinaClientGenerator;
    }
}
