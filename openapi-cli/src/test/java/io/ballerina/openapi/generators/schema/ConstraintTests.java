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
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.client.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.generators.common.GeneratorTestUtils;
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

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getDiagnostics;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This test class is to contain the test related to constraint validation.
 */
public class ConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    SyntaxTree syntaxTree = null;
    @BeforeMethod
    public void setUp() throws IOException {
        GeneratorTestUtils.deleteGeneratedFiles();
    }

    @Test(description = "Tests with record field has constraint and record field type can be user defined datatype " +
            "with constraint.")
    public void testRecordFiledConstraint() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/record_field.yaml"),
                true);
        TypeHandler.createInstance(openAPI, false);
        RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(openAPI.getPaths()
                .get("/admin").getPost().getRequestBody(), openAPI);
        requestBodyGenerator.generateParameterNode();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/record_field.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for all the array type scenarios:" +
            "Use case 01 : User define array (Annotations on a type)" +
            "Use case 02 : For both array type and array items have constraints," +
            "Use case 03 : Reference array" +
            "Use case 04 : Array items have constrained with number format" +
            "Use case 05 : Only array items have constrained with number format")
    public void testForArray() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/array.yaml"),
                true);
        TypeHandler.createInstance(openAPI, false);
        RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(openAPI.getPaths()
                .get("/admin").getPost().getRequestBody(), openAPI);
        requestBodyGenerator.generateParameterNode();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree("schema/ballerina/constraint/array.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for the field has reference type scenarios" +
            "Use case 01 : Annotations on a record field" +
            "Use case 02 : Annotations on a type" +
            "Use case 03 : Annotations on a type used as a record field")
    public void testForReference() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/type_def_node.yaml"),
                true);
        TypeHandler.createInstance(openAPI, false);
        RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(openAPI.getPaths().get("/admin")
                .getPost().getRequestBody(), openAPI);
        requestBodyGenerator.generateParameterNode();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/type_def_node.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }


    @Test(description = "Tests with record field has constraint value with zero.", enabled = false)
    public void testRecordFiledConstraintWithZeroValue() throws IOException, BallerinaOpenApiException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve(
                "swagger/constraint/record_field_02.yaml"),
                true);
        TypeHandler.createInstance(openAPI, false);
        RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(openAPI.getPaths()
                .get("/admin").getPost().getRequestBody(), openAPI);
        requestBodyGenerator.generateParameterNode();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        //todo need fix with oneOF anyOf
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/record_field_02.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests with nested array field has constraint.", enabled = false)
    public void testNestedArrayWithConstraint() throws IOException, BallerinaOpenApiException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                        "/nested_array_with_constraint.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        //todo need to fix oneOf any off
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/nested_array.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Tests with additional properties field has constraint.")
    public void testAdditionalPropertiesWithConstraint() throws IOException, BallerinaOpenApiException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/additional_properties_with_constraint.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/additional_properties_with_constraint.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }
    //TODO current tool doesn't handle union type: therefore union type constraint will handle once union type
    // generation available in tool.

    @Test(description = "Test for invalid constraint value")
    public void testInvalidConstraintUses() throws IOException, BallerinaOpenApiException, FormatterException,
            ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/invalidConstraintFieldWithDataType.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/invalidConstraintFieldWithDtaType.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for invalid constraint value with valid constraint value")
    public void testInvalidAndValidBothConstraintUses() throws IOException, BallerinaOpenApiException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/invalidAndValidConstraintFieldWithDataType.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/invalidAndValidConstraintFieldWithDtaType.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for allowing zero value for number and integer type")
    public void testAllowedZeroValuesForNumber() throws IOException, BallerinaOpenApiException, FormatterException,
            ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/allow_zero_values_for_number_constraint.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/allow_zero_values_for_number_constraint.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Tests for nullable reference types with constraint.", enabled = false)
    public void testNullableRefTypesWithConstraint() throws IOException, BallerinaOpenApiException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/constraint_with_nullable.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/constraint_with_nullable.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for schema properties having pattern constraint.")
    public void testStringSchemaPropertyWithPattern() throws IOException, BallerinaOpenApiException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/pattern_string.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/string_pattern.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for exclusiveMin and exclusiveMax property changes in OpenAPI 3.1")
    public void testExclusiveMinMaxInV31() throws IOException, BallerinaOpenApiException, OASTypeGenException,
            FormatterException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/exclusive_min_max_3_1.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/exclusive_min_max_3_1.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }

    @Test(description = "Test for schema properties containing data types with format constraints for OASV3.",
            enabled = false) // todo : need to fix as the resource order changes intermittently
    public void testDataTypeHasFormatWithConstraint() throws IOException, BallerinaOpenApiException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/format_types_v3_0.yaml"), true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        ballerinaClientGenerator.generateSyntaxTree();
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/format_type.bal", syntaxTree);
    }

    @Test(description = "Test for schema properties containing data types with format constraints.", enabled = false)
    // todo : need to fix as the resource order changes intermittently
    public void testDataTypeHasFormatWithConstraintOASV3() throws IOException, BallerinaOpenApiException,
            ClientException {
        //Test for OpenAPI version 3.1
        OpenAPI openAPIV31 = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/format_types_v3_1.yaml"), true);
        TypeHandler.createInstance(openAPIV31, false);
        OASClientConfig.Builder cBuilder = new OASClientConfig.Builder();
        OASClientConfig cBuilderConfig = cBuilder
                .withFilters(new Filter())
                .withOpenAPI(openAPIV31)
                .withResourceMode(false).build();
        BallerinaClientGenerator oasv3 = new BallerinaClientGenerator(cBuilderConfig);
        oasv3.generateSyntaxTree();
        SyntaxTree syntaxTreeV3 = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/constraint/format_type_v3.bal", syntaxTreeV3);
    }

    @AfterMethod
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
