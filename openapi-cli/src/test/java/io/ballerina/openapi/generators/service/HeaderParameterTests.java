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
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This test class contains the all the tests related to headers.
 */
public class HeaderParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "01. Required header parameter.")
    public void requiredHeader() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_01.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_01.bal", syntaxTree);
    }

    @Test(description = "02. Required header parameter without header data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Header 'x-request-id' with no header type can not be mapped to the.*")
    public void requiredHeaderWithNoHeaderType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_02.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header.bal", syntaxTree);
    }

    @Test(description = "03. Header parameter with unsupported header data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Header 'x-request-id' with type 'integer' can not be mapped" +
                    " as a valid Ballerina.*")
    public void headerWithNoSupportType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_03.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header.bal", syntaxTree);
    }

    @Test(description = "04. Header parameter with array type with no item type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Header 'x-request-id' with no array item type can not be mapped " +
                    "as a valid Ballerina header.*")
    public void headerNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_04.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header.bal", syntaxTree);
    }

    @Test(description = "05. Header parameter has array type with not support item type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Header 'x-request-id' with array item type: 'integer' is not" +
                    " supported.*")
    public void headerNoSupportArrayItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_05.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header.bal", syntaxTree);
    }

    @Test(description = "06. Optional header parameter")
    public void optionalHeaders() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_06.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_06.bal", syntaxTree);
    }

    @Test(description = "07. Optional header parameter with array type")
    public void optionalHeadersWithArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_07.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_07.bal", syntaxTree);
    }

    @Test(description = "08. Optional header parameter with array type default type value")
    public void optionalHeadersWithArrayDefaultValue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_08.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_08.bal", syntaxTree);
    }

    @Test(description = "09. Header parameter with default type value")
    public void optionalHeadersWithDefaultValue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_09.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_09.bal", syntaxTree);
    }

    @Test(description = "10. Header parameter with required nullabe true attribute")
    public void requiredNullableTrue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_10.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_10.bal", syntaxTree);
    }

    @Test(description = "11. Optional header parameter has nullable true property")
    public void optionalHeaderWithNullableTrue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_11.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_11.bal", syntaxTree);
    }

    @Test(description = "12. Header parameter order")
    public void paramterOrder() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_12.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_12.bal", syntaxTree);
    }
}
