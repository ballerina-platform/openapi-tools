package org.ballerinalang.generators;

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


    @Test(description = "Generate importors")
    public void generateImports() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate serviceDeclaration")
    public void generateService() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/petstore_service.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for Path parameters")
    public void generatePathparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/multiPathParam.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for Query parameters")
    public void generateQueryparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/multiQueryParam.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple operations")
    public void generateMultipleOperatons() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/multiOperations.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple paths")
    public void generateMultiplePath() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/multiPaths.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateJsonPayload() throws IOException, BallerinaOpenApiException, FormatterException {
        String definitionPath = RES_DIR.resolve("generators/swagger/jsonPayload.yaml").toString();
        BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }
}
