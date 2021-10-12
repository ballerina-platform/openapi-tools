package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.converter.OpenApiConverterException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CacheConfigTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec for service configuration annotation in resource without fields")
    public void cacheConfigTests01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/configuration_rs01.yaml");
    }

    @Test(description = "When cache-config has custom value without ETag and Last-Modified.")
    public void cacheConfigTests02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs02.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_02.yaml");
    }

    @Test(description = "When cache-config has custom value with private field and no cache field enable")
    public void cacheConfigTests03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs03.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_03.yaml");
    }

    @Test(description = "When cache-config has custom value with negative max age")
    public void cacheConfigTests04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs04.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_04.yaml");
    }

    @Test(description = "When cache-config has custom value with negative max age")
    public void cacheConfigTests05() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs05.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_05.yaml");
    }

    @Test(description = "When cache-config has union type response with error")
    public void cacheConfigTests06() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs06.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_05.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        TestUtils.deleteDirectory(this.tempDir);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }

}
