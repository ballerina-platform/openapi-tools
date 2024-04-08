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
import io.ballerina.openapi.TestUtils;
import io.ballerina.openapi.cmd.CmdUtils;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.service.ServiceDeclarationGenerator;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
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
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_01.bal", syntaxTree);
    }

    @Test(description = "02. Required query parameter has array data type")
    public void requiredQueryParameterPrimitiveArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_02.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_02.bal", syntaxTree);
    }

    @Test(description = "03. Required query parameter has nested array data type")
    public void requiredQueryParameterPrimitiveNestedArray()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_03.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_03.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Query parameters with nested array types are not supported in Ballerina.");
    }

    @Test(description = "04. Required query parameter has array data type with no item types")
    public void requiredQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_04.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_04.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Query parameters with no array item type can not be " +
                        "mapped to Ballerina resource query parameters.");
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
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_05.bal", syntaxTree);
    }

    @Test(description = "06. Nullable query parameter with array")
    public void nullableQueryParameterWithArray() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_06.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_06.bal", syntaxTree);
    }

    @Test(description = "07. Optional query parameter has nested array data type")
    public void optionalQueryParameterPrimitiveNestedArray()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_07.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_07.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Query parameters with nested array types are not supported in Ballerina.");
    }

    @Test(description = "08. Optional query parameter has array data type with no item types")
    public void optionalQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_08.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_08.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Query parameters with no array item type can not be mapped to " +
                        "Ballerina resource query parameters.");
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
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_09.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_09.bal", syntaxTree);
    }

    @Test(description = "10. Optional query parameter has array data type with no item types")
    public void defaultQueryParameterArrayHasNoItemType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_10.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_10.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Query parameters with no array item type can not be mapped to Ballerina resource " +
                        "query parameters.");
    }

    @Test(description = "11. Required query parameter has nullable true")
    public void requiredNullableQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_11.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_11.bal", syntaxTree);
    }

    @Test(description = "12. Required query parameter has map<json> type")
    public void mapJsonQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_12.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_12.bal", syntaxTree);
    }

    @Test(description = "13. Default query parameter has string type")
    public void stringDefaultQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_13.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_13.bal", syntaxTree);
    }

    @Test(description = "14. Fix the query parameter order")
    public void queryParameterOrder() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_14.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_14.bal", syntaxTree);
    }

    @Test(description = "15. Optional query parameter")
    public void optionalQueryParameter() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/query_15.yaml");
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("query/query_15.bal", syntaxTree);
    }

    @Test(description = "16. Query parameter(s) having a referenced schema")
    public void generateParamsWithRefSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_ref_schema.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, false);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "parameters_with_object_ref_schema.bal", syntaxTree);
    }

    @Test(description = "17. Query parameter(s) having a referenced schema of unsupported type")
    public void generateParamsWithInvalidRefSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_invalid_ref_schema.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        TestUtils.compareDiagnosticWarnings(ballerinaServiceGenerator.getDiagnostics(),
                "Type 'xml' is not a valid query parameter type in Ballerina. The supported " +
                        "types are string, int, float, boolean, decimal, array types of the aforementioned " +
                        "types and map<json>.");
    }

    @Test(description = "18. Query parameter(s) having a referenced schema of array of unsupported type",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: \n" +
                    "attribute components.schemas.Room.content is unexpected.*")
    public void generateParamsWithInvalidArrayRefSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameter_with_ref_array_invalid_schema.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
    }

    @Test(description = "19. Query parameter(s) having a referenced schema type")
    public void generateParamsWithObjectRefSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_object_ref_schema.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "query/parameters_with_object_ref_schema.bal", syntaxTree);
    }

    @Test(description = "20. Query parameter(s) having a referenced schema of array type")
    public void generateParamsWithObjectArrayRefSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/parameter_with_ref_array_object_schema.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "query/parameter_with_ref_array_object_schema.bal", syntaxTree);
    }

    @Test(description = "21. Query parameter(s) having a object schema")
    public void generateParamsWithObjectType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/query/object_query.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, false);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        TypeHandler.createInstance(openAPI, false);
        ServiceDeclarationGenerator ballerinaServiceGenerator = new ServiceDeclarationGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "query/object_query.bal", syntaxTree);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "query/object_query_type.bal", syntaxTree);
    }
}
