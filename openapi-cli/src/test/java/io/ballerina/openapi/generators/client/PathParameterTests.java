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
import io.ballerina.openapi.cmd.BallerinaCodeGenerator;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * This tests class for the tests Path parameters in swagger file.
 */
public class PathParameterTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Generate Client for path parameter has parameter name as key word - unit tests for method")
    public void generatePathWithPathParameterTests() throws IOException, BallerinaOpenApiException {
        // "/v1/v2"), "/v1/v2"
        // "/v1/{version}/v2/{name}", "/v1/${'version}/v2/${name}"
        // "/v1/{version}/v2/{limit}", "/v1/${'version}/v2/${'limit}"
        // "/v1/{age}/v2/{name}", "/v1/${age}/v2/${name}"

        BallerinaCodeGenerator codeGenerator = new BallerinaCodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_valid.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_parameter_valid.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for path parameter with referenced schema")
    public void generatePathParamWithReferencedSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_param_with_ref_schemas.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_param_with_ref_schema.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client while handling special characters in path parameter name")
    public void generateFormattedPathParamName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_special_name.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_parameter_with_special_name.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client with duplicated path parameter name in the path")
    public void generateFormattedDuplicatedPathParamName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_param_duplicated_name.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_param_duplicated_name.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "When path parameter has given unmatch data type in ballerina",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Invalid path parameter data type for the parameter: .*")
    public void testInvalidPathParameterType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_invalid.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
    }

    @Test(description = "When given data type not match with ballerina data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Unsupported OAS data type .*")
    public void testInvalidDataType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_invalid02.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
    }

    @Test (description = "Generate Client for path parameter with anyOf, oneOf type")
    public void unionPathParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/union_path_parameter.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/union_path_parameter.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);

        Path expectedTypePath = RESDIR.resolve("ballerina/union_path_types.bal");
        BallerinaTypesGenerator ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree typeSyntaxTree = ballerinaTypesGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedTypePath, typeSyntaxTree);
    }

    @Test(description = "When path parameter has given allOf data type in ballerina",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Path parameter: 'id' is invalid. " +
                    "Ballerina does not support object type path parameters.")
    public void allOfPathParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/allOf_path_parameter.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
    }

    @Test (description = "Generate Client for path parameter with integer int32 and int64 types")
    public void testIntegerPathParameters() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/integer_signed32_path_parameter.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/integer_signed32_path_parameter.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
}
