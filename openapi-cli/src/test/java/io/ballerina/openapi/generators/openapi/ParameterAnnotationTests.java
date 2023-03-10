package io.ballerina.openapi.generators.openapi;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class is for capturing the scenarios with parameter annotations in resource methods.
 */
public class ParameterAnnotationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test(description = "Test OpenAPI generation when payload is not annotated")
    public void testPayloadNotAnnotated() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/unannotated_payload.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "parameter_annotation/unannotated_payload.yaml");
    }

    @Test(description = "Test OpenAPI generation for complex scenarios with annotated payload")
    public void testComplexAnnotatedPayload() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/annotated_payload_complex.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath,
                "parameter_annotation/annotated_payload_complex.yaml");
    }

    @Test(description = "Test OpenAPI generation when query parameter is annotated")
    public void testQueryParameterAnnotated() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/annotated_query.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath,
                "parameter_annotation/annotated_query.yaml");
    }
}
