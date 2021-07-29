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
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
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

        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_valid.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_parameter_valid.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for path parameter with referenced schema")
    public void generatePathParamWithReferencedSchema() throws IOException, BallerinaOpenApiException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/path_param_with_ref_schemas.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_param_with_ref_schema.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client while handling special characters in path parameter name")
    public void generateFormattedPathParamName() throws IOException, BallerinaOpenApiException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_special_name.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/path_parameter_with_special_name.bal");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "When path parameter has given unmatch data type in ballerina",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Invalid path parameter data type for the parameter: .*")
    public void testInvalidPathParameterType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_invalid.yaml");
        // int, string, boolean, decimal, float,
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        ballerinaClientGenerator.generateSyntaxTree();
    }

    @Test(description = "When given data type not match with ballerina data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Unsupported OAS data type .*")
    public void testInvalidDataType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/path_parameter_invalid02.yaml");
        // int, string, boolean, decimal, float,
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        ballerinaClientGenerator.generateSyntaxTree();
    }
}
