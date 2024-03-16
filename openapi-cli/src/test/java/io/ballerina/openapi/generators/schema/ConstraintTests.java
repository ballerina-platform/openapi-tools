/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This test class is to contain the test related to constraint validation.
 */
public class ConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    @BeforeMethod
    public void setUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }

    @Test(description = "Tests with record field has constraint and record field type can be user defined datatype " +
            "with constraint.")
    public void testRecordFiledConstraint() throws IOException, BallerinaOpenApiException, OASTypeGenException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/record_field.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/record_field.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for all the array type scenarios:" +
            "Use case 01 : User define array (Annotations on a type)" +
            "Use case 02 : For both array type and array items have constraints," +
            "Use case 03 : Reference array" +
            "Use case 04 : Array items have constrained with number format" +
            "Use case 05 : Only array items have constrained with number format")
    public void testForArray() throws IOException, BallerinaOpenApiException, OASTypeGenException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/array.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/array.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for the field has reference type scenarios" +
            "Use case 01 : Annotations on a record field" +
            "Use case 02 : Annotations on a type" +
            "Use case 03 : Annotations on a type used as a record field")
    public void testForReference() throws IOException, BallerinaOpenApiException, OASTypeGenException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/type_def_node.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/type_def_node.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }


    @Test(description = "Tests with record field has constraint value with zero.")
    public void testRecordFiledConstraintWithZeroValue() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/record_field_02.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/record_field_02.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests with nested array field has constraint.")
    public void testNestedArrayWithConstraint() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                        "/nested_array_with_constraint.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/nested_array.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Tests with additional properties field has constraint.")
    public void testAdditionalPropertiesWithConstraint() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/additional_properties_with_constraint.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/additional_properties_with_constraint.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }
    //TODO current tool doesn't handle union type: therefore union type constraint will handle once union type
    // generation available in tool.

    @Test(description = "Test for invalid constraint value")
    public void testInvalidConstraintUses() throws IOException, BallerinaOpenApiException, OASTypeGenException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/invalidConstraintFieldWithDataType.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/invalidConstraintFieldWithDtaType.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for invalid constraint value with valid constraint value")
    public void testInvalidAndValidBothConstraintUses() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/invalidAndValidConstraintFieldWithDataType.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/invalidAndValidConstraintFieldWithDtaType.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for allowing zero value for number and integer type")
    public void testAllowedZeroValuesForNumber() throws IOException, BallerinaOpenApiException, OASTypeGenException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/allow_zero_values_for_number_constraint.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/allow_zero_values_for_number_constraint.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Tests for nullable reference types with constraint.")
    public void testNullableRefTypesWithConstraint() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/constraint_with_nullable.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/constraint_with_nullable.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for schema properties having pattern constraint.")
    public void testStringSchemaPropertyWithPattern() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/pattern_string.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/string_pattern.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for exclusiveMin and exclusiveMax property changes in OpenAPI 3.1")
    public void testExclusiveMinMaxInV31() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/exclusive_min_max_3_1.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/exclusive_min_max_3_1.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for schema properties containing data types with format constraints.")
    public void testDataTypeHasFormatWithConstraint() throws IOException, BallerinaOpenApiException, OASTypeGenException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/format_types_v3_0.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/format_type.bal", syntaxTree);

        //Test for OpenAPI version 3.1
        OpenAPI openAPIV31 = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/format_types_v3_1.yaml"), true);
        BallerinaTypesGenerator schemaGenerator = new BallerinaTypesGenerator(openAPIV31);
        SyntaxTree syntaxTreeV3 = schemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/format_type.bal", syntaxTreeV3);
    }

    @AfterMethod
    private void deleteGeneratedFiles() {
        try {
            TestUtils.deleteGeneratedFiles();
        } catch (IOException ignored) {
        }
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }
}
