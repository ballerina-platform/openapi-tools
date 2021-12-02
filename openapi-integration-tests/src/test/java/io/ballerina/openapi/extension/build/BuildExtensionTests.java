package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * These tests are for capture the `--export-openapi` flag in distribution.
 */
public class BuildExtensionTests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/build");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Check openapi build plugin in `bal build` command")
    public void annotationWithOutBuildOption() throws IOException {
        executeCommand("project_1");
    }

    @Test(description = "Check openapi build plugin in `bal build` command with `--export-openapi` flag")
    public void annotationWithBuildOption() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_2"), buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("project_2/target/greeting_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with graphQl service")
    public void withNonHttpServiceWithBuildOption() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_3"), buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("project_3/target/greeting_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with package has service on module")
    public void buildOptionWithSeparateModule() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_4"), buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("project_4/target/greeting_openapi.yaml")));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("project_4/target/mod_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with single service file build")
    public void buildOptionWithSingleFile() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_5/service.bal"), buildArgs);
    }

    @Test(description = "Check --export-openapi flag with grpc service")
    public void buildOptionWithGrpcService() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_6"), buildArgs);
    }

    @Test(description = "Check --export-openapi flag with webHub service")
    public void buildOptionWithWebHub() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_7"), buildArgs);
    }

    private void executeCommand(String resourcePath) throws IOException {
        List<String> buildArgs = new LinkedList<>();
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve(resourcePath), buildArgs);
    }
}
