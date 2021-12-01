package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * All the tests related to the display and deprecated annotations in the schema generation.
 */
public class AnnotationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;
    CodeGenerator codeGenerator = new CodeGenerator();

    @Test(description = "Generate records with deprecated annotation")
    public void generateRecordsWithDeprecatedAnnotations() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/deprecated_schemas.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/deprecated_schemas.bal", syntaxTree);
    }
}
