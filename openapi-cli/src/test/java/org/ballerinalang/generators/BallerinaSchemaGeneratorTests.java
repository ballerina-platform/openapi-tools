package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

public class BallerinaSchemaGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("generators"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;


    @Test(description = "Scenario01-Generate single record")
    public void generateScenario01() throws FormatterException,
            OpenApiException, IOException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/schema/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema01.bal");
    }

    @Test(description = "Scenario02- Generate multiple record")
    public void generateScenario02() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/schema/scenario02.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    @Test(description = "Scenario03-Generate record with array filed record")
    public void generateScenario03() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    @Test(description = "Scenario04-Generate record with nested array filed record")
    public void generateScenario04() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    @Test(description = "Scenario05-Generate record with record type filed record")
    public void generateScenario05() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    @Test(description = "Scenario06 -Generate record with nested record type filed record")
    public void generateScenario06() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    @Test(description = "Scenario07-Generate record with record type array filed record")
    public void generateScenario07() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
    }

    // field has object type
    // field has ref







    //Get string as a content of ballerina file
    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String s) throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("generators/ballerina"),
                s);
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }

}
