package io.ballerina.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.generators.common.TestUtils;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for schemas that refer to another schema entirely.
 */
public class NestedRecordInclusionTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;
    CodeGenerator codeGenerator = new CodeGenerator();

    @Test(description = "Generate records for nested referenced schemas")
    public void generateAllOf() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/nested_schema_refs.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree
                ("schema/ballerina/nested_schema_refs.bal", syntaxTree);
    }
}
