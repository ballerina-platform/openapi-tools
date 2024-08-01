package io.ballerina.openapi.generators.common;

import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.OASSanitizer;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OASSanitizerTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/sanitizer").toAbsolutePath();
    @Test(description = "Functionality tests for getBallerinaOpenApiType")
    public void testForRecordName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("record.yaml");
        Path expectedPath = RES_DIR.resolve("modified_record.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASSanitizer oasSanitizer = new OASSanitizer(openAPI);
        OpenAPI sanitized = oasSanitizer.sanitized();
        // file comparison
        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
        Assert.assertEquals(sanitized, expectedFileContent);
    }

    public void pathParameter() {

    }

    // no edited
    public void queryParameter() {

    }

    public void duplicateRecordName () {

    }

    public void pathAndRecordHasSameName() {

    }

    public void parameterNameHasBlank() {

    }

    @Test
    public void recursiveRecordName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("recursive_record.yaml");
        Path expectedPath = RES_DIR.resolve("modified_recursive_record.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASSanitizer oasSanitizer = new OASSanitizer(openAPI);
        OpenAPI sanitized = oasSanitizer.sanitized();
        // file comparison
        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
        Assert.assertEquals(sanitized, expectedFileContent);
    }
    // parameter in separate section
    // request section
    // response section
    // recursive section
}
