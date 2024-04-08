/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.openapi.TestUtils.FILTER;
import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getDiagnostics;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for generating ballerina types for enum schemas.
 */
public class EnumGenerationTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree = null;
    @BeforeTest
    public void setUp() throws IOException {
        GeneratorTestUtils.deleteGeneratedFiles();
    }

    @Test(description = "Tests for all the enum scenarios in schema generation:" +
            "Use case 01 : Reusable enum" +
            "Use case 02 : Enums in schema properties" +
            "Use case 03 : Enum with null value" +
            "Use case 04 : Enum as array items")
    public void testForEnums() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/schema_with_enums.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/schema_with_enums.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for all the enum scenarios in schema generation:" +
            "Use case 01 : Nullable reusable enum" +
            "Use case 02 : Nullable integer enum" +
            "Use case 03 : Nullable boolean enum" +
            "Use case 04 : Nullable float enum" +
            "Use case 05 : Nullable enum in arrays")
    public void testForNullableEnums() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve(
                "swagger/schema_with_nullable_enums.yaml"), true);
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
                "schema/ballerina/schema_with_nullable_enums.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Test enum with no values")
    public void testForEmptyEnums() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve(
                "swagger/empty_enum.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/empty_enum.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }
    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            GeneratorTestUtils.deleteGeneratedFiles();
        } catch (IOException ignored) {
        }
    }

    @AfterClass
    public void cleanUp() throws IOException {
        GeneratorTestUtils.deleteGeneratedFiles();
    }
}
