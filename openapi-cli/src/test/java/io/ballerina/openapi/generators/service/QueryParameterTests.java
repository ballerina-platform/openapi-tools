package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QueryParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service").toAbsolutePath();
    BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "01. Required query parameter has primitive data type")
    public void requiredQueryParameterPrimitive() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_01.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_01.bal", syntaxTree);
    }

    @Test(description = "02. Required query parameter has array data type")
    public void requiredQueryParameterPrimitiveArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_02.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_02.bal", syntaxTree);
    }

    @Test(description = "03. Required query parameter has nested array data type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Ballerina resource functions are not support to query parameters with " +
                    "nested array.*")
    public void requiredQueryParameterPrimitiveNestedArray()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_03.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query.bal", syntaxTree);
    }

    @Test(description = "04. Required query parameter has array data type with no item types",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Query parameter with no array item type can not be mapped to .*")
    public void requiredQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_04.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
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
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_05.bal", syntaxTree);
    }

    @Test(description = "06. Nullable query parameter with array")
    public void nullableQueryParameterWithArray() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_06.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        System.out.println(Formatter.format(syntaxTree));
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_06.bal", syntaxTree);
    }
}
