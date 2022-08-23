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
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;
import static org.testng.Assert.assertEquals;

/**
 * This test class is to contain the test related to constraint validation.
 */
public class ConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    CodeGenerator codeGenerator = new CodeGenerator();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    @BeforeTest
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }
    @Test(description = "Tests with record field has constraint and record field type can be user defined datatype " +
            "with constraint.")
    public void testRecordFiledConstraint() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/record_field.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/record_field.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for all the array type scenarios:" +
            "Use case 01 : User define array (Annotations on a type)" +
            "Use case 02 : For both array type and array items have constraints," +
            "Use case 03 : Reference array" +
            "Use case 04 : Array items have constrained with number format" +
            "Use case 05 : Only array items have constrained with number format")
    public void testForArray() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/array.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/array.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests for the field has reference type scenarios" +
            "Use case 01 : Annotations on a record field" +
            "Use case 02 : Annotations on a type" +
            "Use case 03 : Annotations on a type used as a record field")
    public void testForReference() throws IOException, BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/type_def_node.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/type_def_node.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @AfterTest
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }

    @Test(description = "Tests with record field has constraint value with zero.")
    public void testRecordFiledConstraintWithZeroValue() throws IOException, BallerinaOpenApiException,
            FormatterException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/record_field_02.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/constraint/record_field_02.bal",
                syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Tests with record field has constraint value with union type.")
    public void constraintWithUnionType() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint/union_type.yaml"),
                true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String out = "WARNING: constraints in OpenAPI contract will be ignored for the field `service_class`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates`, " +
                "as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_anyOf`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_oneOF_array`," +
                " as constraints are not supported on Ballerina union types\n" +
                "WARNING: constraints in OpenAPI contract will be ignored for the field `tax_rates_anyOf_array`, as " +
                "constraints are not supported on Ballerina union types";
        assertEquals(out.replaceAll("\\s+", ""),
                outContent.toString().replaceAll("\\s+", ""));
    }

    @AfterTest
    public void restoreStreams() {
        System.setOut(originalOut);
    }


    //TODO current tool doesn't handle union type: therefore union type constraint will handle once union type
    // generation available in tool.
}
