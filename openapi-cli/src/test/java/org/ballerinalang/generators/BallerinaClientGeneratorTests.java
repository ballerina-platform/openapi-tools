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

/**
 * All the tests related to the BallerinaClientGenerator util.
 */
public class BallerinaClientGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    SyntaxTree syntaxTree;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);


    @Test(description = "Generate Server URL")
    public void generateServerURL() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_server_with_base_path.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("client_template.bal");
    }

    @Test(description = "Generate Operation Id")
    public void generateOperationId() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_without_operation_id.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_id.bal");
    }

    @Test(description = "Generate Client for GET method")
    public void generateClientForGet() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_get.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_get.bal");
    }

    @Test(description = "Generate Client for POST method")
    public void generateClientForPOST() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_post.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_post.bal");
    }

    //Get string as a content of ballerina file
    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void compareGeneratedSyntaxTreeWithExpectedSyntaxTree(String s) throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("ballerina"),
                s);
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }
}
