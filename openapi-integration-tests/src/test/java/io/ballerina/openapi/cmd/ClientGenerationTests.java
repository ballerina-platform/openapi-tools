package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * Integration tests for client resource function.
 */
public class ClientGenerationTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client");

    @Test(description = "Client generation with resource functions")
    public void clientWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--resource-function");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("client.bal")));
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("client.bal", "expected_client.bal");
    }

    @Test(description = "`--resource-functions` option with service")
    public void serviceWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("service");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--resource-function");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertFalse(Files.exists(TEST_RESOURCE.resolve("client.bal")));
    }

    @Test(description = "`--resource-functions` option without any mode")
    public void commonWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--resource-function");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("client.bal")));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("openapi_service.bal")));
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("client.bal", "expected_client.bal");
    }
}
