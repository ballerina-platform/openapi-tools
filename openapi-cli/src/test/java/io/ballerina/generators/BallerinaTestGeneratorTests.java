package io.ballerina.generators;

import io.ballerina.openapi.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.generators.GeneratorConstants.USER_DIR;

/**
 * Unit tests for {@link BallerinaTestGenerator}.
 */
public class BallerinaTestGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("expected_gen"));
    Path testFilePath = resourcePath.resolve("tests/test.bal");
    Path testDirPath = resourcePath.resolve("tests");

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test Ballerina test case skeletons generation")
    public void generateTests() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_test.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "generated_test.bal");
            generator.generateClient(definitionPath, definitionPath, clientName, resourcePath.toString(), filter);

            if (Files.exists(testFilePath)) {
                String generatedClient = getStringFromGivenBalFile(testDirPath, "test.bal");
                Files.deleteIfExists(testFilePath);
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_client.bal");
        }
    }

    @Test(description = "Test Ballerina test case skeletons generation for null operation Ids")
    public void generateTestsForNoOperationIds() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("noOperationId.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "generated_test_noid.bal");
            generator.generateClient(definitionPath, definitionPath, clientName, resourcePath.toString(), filter);

            if (Files.exists(testFilePath)) {
                String generatedClient = getStringFromGivenBalFile(testDirPath, "test.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_client.bal");
        }
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void deleteGeneratedFiles(String filename) {
        try {
            Files.deleteIfExists(resourcePath.resolve(filename));
            Files.deleteIfExists(resourcePath.resolve("schema.bal"));
            Files.deleteIfExists(testFilePath);
            FileUtils.deleteDirectory(new File(testDirPath.toString()));
        } catch (IOException e) {
            //Ignore the exception
        }
    }
}
