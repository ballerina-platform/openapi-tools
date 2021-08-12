package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * Test client generation when server url is not given in the open-api definition.
 */
public class NoServerURLTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for no server url")
    public void getClientForNoServerURL() throws IOException, BallerinaOpenApiException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/missing_server_url.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/missing_server_url.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for no server url with HTTP authentication mechanism")
    public void getClientForNoServerURLWithHTTPAuth() throws IOException, BallerinaOpenApiException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/missing_server_url_http.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/missing_server_url_http.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
}
