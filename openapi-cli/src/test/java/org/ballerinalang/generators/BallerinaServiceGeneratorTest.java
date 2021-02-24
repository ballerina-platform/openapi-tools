package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

public class BallerinaServiceGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("generators"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;


    @Test(description = "Generate importors")
    public void generateImports() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate serviceDeclaration")
    public void generateService() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_service.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for Path parameters")
    public void generatePathparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPathParam.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for Query parameters")
    public void generateQueryparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiQueryParam.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple operations")
    public void generateMultipleOperatons() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiOperations.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple paths")
    public void generateMultiplePath() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPaths.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateJsonPayload() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/jsonPayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayload() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responsePayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithRef() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseRefPayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithRefMulti() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseMultipleRefPayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithDifferentStatusCode() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentStatusCode.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponseDifferentStatusCode() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentCodes.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponserecordInline() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseRecordInline.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }
}
