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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * All the tests related to the Ballerina Service Generator.
 */
public class ParameterGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service/").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "Generate serviceDeclaration")
    public void generateService() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_service.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("service_gen.bal", syntaxTree);
    }

    @Test(description = "Generate service with path having special characters")
    public void generateServiceWithPathSpecialCharacters() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_service_with_special_characters.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("service_gen_special_characters.bal",
                syntaxTree);
    }
    @Test(description = "Generate functionDefinitionNode for multiple operations")
    public void generateMultipleOperations() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/multiOperations.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("multi_operations.bal", syntaxTree);
    }

    @Test(description = "Generate functionDefinitionNode for multiple paths")
    public void generateMultiplePath() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/multiPaths.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("multi_paths.bal", syntaxTree);
    }

    //Scenario 01 - Path parameters.
    @Test(description = "Generate functionDefinitionNode for Path parameters")
    public void generatePathparameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/multiPathParam.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("path_parameters.bal", syntaxTree);
    }

    //Scenario 02 - Path parameters.
    @Test(description = "Generate functionDefinitionNode for only Path parameters")
    public void generatePathparameter02() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/multiPathParam02.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("path_parameters02.bal", syntaxTree);
    }

    //Scenario 02 - Query parameters.
    @Test(description = "Generate functionDefinitionNode for Query parameters",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Ballerina resource functions are not support to query parameters" +
                    " with nested array .*")
    public void generateQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/multiQueryParam.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query_parameters.bal", syntaxTree);

    }

    @Test(description = "Generate functionDefinitionNode for paramter for content instead of schema",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Given OpenAPI query parameter type 'filter' can not be" +
                    " mapped to the Ballerina query .*")
    public void generateParameterHasContent() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameterTypehasContent.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("param_type_with_content.bal", syntaxTree);
    }
}
