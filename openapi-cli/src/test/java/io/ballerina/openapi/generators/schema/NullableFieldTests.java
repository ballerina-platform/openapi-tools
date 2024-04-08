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
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.ServiceGenerationHandler;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.generators.common.GeneratorTestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.TestUtils.FILTER;

/**
 * Tests for SwaggerParser.
 */
public class NullableFieldTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree = null;

    @Test(description = "Test for nullable primitive fields")
    public void testNullablePrimitive() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_primitive_schema.yaml"), true);
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
                "schema/ballerina/nullable_primitive.bal", syntaxTree);
    }

    @Test(description = "Test for nullable array fields")
    public void testNullableArray() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_array_schema.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/nullable_array.bal",
                syntaxTree);
    }

    @Test(description = "Test for nullable array referenced schemas")
    public void testNullableArrayRefSchemas() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_ref_array.yaml"), true);
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
                "schema/ballerina/nullable_ref_array.bal", syntaxTree);
        // todo : failing due to doc issue
    }

    @Test(description = "Test nullable for primitive referenced type")
    public void testPrimitiveReferencedTypes() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_string_type.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        String syntaxTreeContent = syntaxTree.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(syntaxTreeContent, "publictypeLatitudestring?;");
    }

    @Test(description = "Test for referenced schema with no type given")
    public void testNullTypeReference() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_null_type.yaml"), true);
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
                "schema/ballerina/nullable_option_null_type.bal", syntaxTree);
    }

    @Test(description = "Test for nullable record fields")
    public void testNullableRecord() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_record_schema.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/nullable_record.bal",
                syntaxTree);
    }

    @Test(description = "Test for union type generation for nullable anyOf schema")
    public void testNullableUnionType() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_anyof_schema.yaml"), true);
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
                "schema/ballerina/nullable_anyof_schema.bal", syntaxTree);
    }

    @Test(description = "Test for union type generation for nullable anyOf schema with array schema")
    public void testNullableArrayUnionType() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_anyof_array_schema.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/" +
                        "nullable_anyof_array_schema.bal", syntaxTree);
    }

    @Test(description = "Test for type generation for object schema with no properties")
    public void testNullableEmptyObjectSchema() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/null_empty_record.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/" +
                "null_empty_record.bal", syntaxTree);
    }

    @Test(description = "Test for type generation for OpenAPI 3.1 schemas with `null` type")
    public void testNullTypePropertyGeneration() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/null_type_3_1.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/" +
                "null_type_3_1.bal", syntaxTree);
    }
}
