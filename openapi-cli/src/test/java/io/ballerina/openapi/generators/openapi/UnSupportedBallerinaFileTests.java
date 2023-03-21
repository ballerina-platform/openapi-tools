package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class tests the tool behaviour in OpenAPI contract generation when unsupported Ballerina file is given.
 */
public class UnSupportedBallerinaFileTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test(description = "Test the warning message when unsupported bal file is given")
    public void testCompilerWarningForUnsupportedBallerinaFile() {
        Path ballerinaFilePath = RES_DIR.resolve("graphql_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null
                , false);
        List<OpenAPIConverterDiagnostic> errors = openApiConverter.getErrors();
        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(errors.get(0).getMessage(),
                "Given Ballerina file does not contain any HTTP service.");
    }
}
