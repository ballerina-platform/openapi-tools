package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
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
    CodeGenerator codeGenerator = new CodeGenerator();

    @Test(description = "Test for nullable primitive fields")
    public void testNullablePrimitive() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_primitive_schema.yaml"));
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_primitive_fields.bal", syntaxTree);
    }

    @Test(description = "Test for nullable array fields")
    public void testNullableArray() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_array_schema.yaml"));
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullale_option_array_schema.bal", syntaxTree);
    }

    @Test(description = "Test for nullable record fields")
    public void testNullableRecord() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_record_schema.yaml"));
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_option_record_schema.bal", syntaxTree);
    }

    @Test(description = "Test for primitive referenced type")
    public void testPrimitiveReferencedTypes() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_option_string_type.yaml"));
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String syntaxTreeContent = syntaxTree.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(syntaxTreeContent, "publictypeLatitudestring?;");
    }

    @Test(description = "Test field generation when nullable false")
    public void testNullableFalse() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/nullable_false.yaml"));
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nullable_false.bal", syntaxTree);
    }
}
