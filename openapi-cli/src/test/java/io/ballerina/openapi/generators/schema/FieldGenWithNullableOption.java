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
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for code generation when nullable command line option given.
 */
public class FieldGenWithNullableOption {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree = null;
    @Test(description = "Test for nullable primitive fields")
    public void testNullablePrimitive() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_primitive_schema.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_primitive_fields.bal", syntaxTree);
    }

    @Test(description = "Test for nullable array fields")
    public void testNullableArray() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_array_schema.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullale_option_array_schema.bal", syntaxTree);
    }

    @Test(description = "Test for nullable record fields")
    public void testNullableRecord() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_record_schema.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_record_schema.bal", syntaxTree);
    }

    @Test(description = "Test for primitive referenced type")
    public void testPrimitiveReferencedTypes() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_string_type.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        String syntaxTreeContent = syntaxTree.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(syntaxTreeContent, "publictypeLatitudestring?;");
    }

    @Test(description = "Test for referenced schema with no type given")
    public void testNullTypeReference() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_null_type.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_null_type.bal", syntaxTree);
    }

    @Test(description = "Test field generation when nullable false")
    public void testNullableFalse() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_false.yaml"), true);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
//        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_false.bal", syntaxTree);
    }
}
