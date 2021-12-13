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
 * This test class contains all the query parameter tests.
 */
public class QueryParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "01. Required query parameter has primitive data type")
    public void requiredQueryParameterPrimitive() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_01.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_01.bal", syntaxTree);
    }

    @Test(description = "02. Required query parameter has array data type")
    public void requiredQueryParameterPrimitiveArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_02.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_02.bal", syntaxTree);
    }

    @Test(description = "03. Required query parameter has nested array data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameters with nested array types are not supported in.*")
    public void requiredQueryParameterPrimitiveNestedArray()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_03.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }

    @Test(description = "04. Required query parameter has array data type with no item types",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameters with no array item type can not be mapped to.*")
    public void requiredQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_04.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }
    /*
    ex: 05
       parameters:
        - name: offset
          in: query
          schema:
            type: integer
            nullable: true
     */
    @Test(description = "05. Nullable optional query parameter")
    public void nullableQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_05.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_05.bal", syntaxTree);
    }

    @Test(description = "06. Nullable query parameter with array")
    public void nullableQueryParameterWithArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_06.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_06.bal", syntaxTree);
    }

    @Test(description = "07. Optional query parameter has nested array data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameters with nested array types are not supported in.*")
    public void optionalQueryParameterPrimitiveNestedArray()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_07.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }

    @Test(description = "08. Optional query parameter has array data type with no item types",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameters with no array item type can not be mapped to .*")
    public void optionalQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_08.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }
/*
      parameters:
        - name: limit
          in: query
          schema:
            type: integer
            default: 10
            format: int32
      ballerina -> int limit = 10;
 */
    @Test(description = "09. Default query parameter has primitive data type and array")
    public void defaultQueryParameter()
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_09.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_09.bal", syntaxTree);
    }

    @Test(description = "10. Optional query parameter has array data type with no item types",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameters with no array item type can not be mapped to" +
                    " Ballerina resource query .*")
    public void defaultQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_10.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }

    @Test(description = "11. Required query parameter has nullable true")
    public void requiredNullableQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_11.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_11.bal", syntaxTree);
    }

    @Test(description = "12. Required query parameter has map<json> type")
    public void mapJsonQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_12.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_12.bal", syntaxTree);
    }

    @Test(description = "13. Default query parameter has string type")
    public void stringDefaultQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_13.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_13.bal", syntaxTree);
    }

    @Test(description = "14. Fix the query parameter order")
    public void queryParameterOrder() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_14.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_14.bal", syntaxTree);
    }

    @Test(description = "14.Optional query parameter")
    public void optionalQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_15.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_15.bal", syntaxTree);
    }
}
