package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HeaderTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;


    //Scenario 03 - Header parameters.
    @Test(description = "Generate functionDefinitionNode for Header parameters")
    public void generateHeaderParameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/headers/multiHeaderParam.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree(openAPI, filter);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("header_parameters.bal", syntaxTree);
    }

}
