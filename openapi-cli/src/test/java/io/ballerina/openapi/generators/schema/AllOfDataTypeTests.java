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

package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.TestUtils;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.ServiceGenerationHandler;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.generators.common.GeneratorTestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.TestUtils.FILTER;

/**
 * The tests are related to the allOF data type in the swagger.
 */
public class AllOfDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;

    @Test(description = "Generate record for schema has allOf reference")
    public void generateAllOf() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario09.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/schema09.bal",
                syntaxTree);
        // todo : delete by doc comments
    }

    @Test(description = "Generate record for schema has allOf reference in record field")
    public void generateAllOfInRecordField() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/allOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/allOf.bal",
                syntaxTree);
    }

    @Test(description = "Generate record when allOf schema has only one references schema")
    public void generateTypeForSingleAllOfSchema() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/allOf_with_one_ref.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/allOf_with_one_ref.bal", syntaxTree);
    }

    @Test(description = "Generate record when allOf schema has only one references schema with cyclic dependency " +
            "schema")
    public void generateCyclicSchemaAllOfSchema() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/allOf_with_cyclic.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/allOf_with_cyclic.bal", syntaxTree);
    }

    @Test(description = "Generate record for allOf schema with array schema")
    public void generateAllOfWithTypeUnSpecifiedObjectSchema() throws IOException,
            BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/allOfWithNoType.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/allOfWithNoType.bal", syntaxTree);
    }

    @Test(description = "Generate record for allOf type array schemas with inline object schemas")
    public void generateArrayAllOfInlineObjects() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/array_with_inline_allOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/array_with_inline_allOf.bal", syntaxTree);
        // todo : failing due do comment issue
    }

    @Test(description = "Generate record for allOf schema with empty object schema")
    public void generateAllOfWithEmptyObjectSchema() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/allOfWithEmptyObject.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/allOfWithEmptyObject.bal", syntaxTree);
        // todo : failing due do comment issue
    }

    @Test(description = "Generate record for nested allOf schemas")
    public void generateNestedAllOfSchema() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/nested_allOf_with_allOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/nested_all_of.bal",
                syntaxTree);
        // todo : failing due doc issue
    }

    @Test(description = "Generate type definition from allOf schema with valid single item")
    public void generateAllOfwithValidSingleItem() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/single_item_allOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/single_item_allOf.bal", syntaxTree);
    }

    @Test(description = "Tests record generation for nested OneOf schema inside AllOf schema")
    public void generateAllOfWithOneOf() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/nested_allOf_with_oneOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/nested_allOf_with_oneOf.bal", syntaxTree);
        TestUtils.compareDiagnosticWarnings(TypeHandler.getInstance().getDiagnostics(),
                "Unsupported nested AllOf schema is found inside a AllOf schema.");
    }
}
