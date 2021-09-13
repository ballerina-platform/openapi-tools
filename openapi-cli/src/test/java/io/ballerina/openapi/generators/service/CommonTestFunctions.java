package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonTestFunctions {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    //Get string as a content of ballerina file
    private static String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    public static void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String balfile, SyntaxTree syntaxTree)
            throws IOException, FormatterException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("generators/service/ballerina"), balfile);
        String generatedSyntaxTree = syntaxTree.toString();
        System.out.println(Formatter.format(generatedSyntaxTree));
        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }

}
