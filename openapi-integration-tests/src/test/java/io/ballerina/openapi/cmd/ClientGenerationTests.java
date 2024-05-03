/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;

/**
 * Integration tests for client resource functions.
 */
public class ClientGenerationTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client");
    public static final Path EXPECTED_RESOURCE = Paths.get("src/test/resources/client");
    private static final String LINE_SEPARATOR = System.lineSeparator();

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
        buildArgs.add("--client-methods");
        buildArgs.add("resource");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("client.bal")));
    }

    @Test(description = "`--client-methods` option with service")
    public void serviceWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("service");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--client-methods");
        buildArgs.add("resource");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
    }

    @Test(description = "`--client-methods` option without any mode")
    public void commonWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--client-methods");
        buildArgs.add("resource");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("client.bal")));
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("openapi_service.bal")));
    }

    @Test(description = "`--status-code-binding` option with client")
    public void resourceClientWithStatusCodeBinding() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("--status-code-binding");
        Path projectGenPath = Paths.get(TEST_RESOURCE + "/project-01");
        Path projectExpectedPath = Paths.get(EXPECTED_RESOURCE + "/project-expected");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, projectGenPath, buildArgs);
        Assert.assertTrue(Files.exists(projectGenPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectGenPath.resolve("client.bal")));
        compareFiles(projectGenPath, "client.bal", projectExpectedPath, "client_resource.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("types.bal")));
        compareFiles(projectGenPath, "types.bal", projectExpectedPath, "types.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("utils.bal")));
        compareFiles(projectGenPath, "utils.bal", projectExpectedPath, "utils.bal");
    }

    @Test(description = "`--status-code-binding` and `--client-methods remote` options with client")
    public void remoteClientWithStatusCodeBinding() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("client");
        buildArgs.add("--client-methods");
        buildArgs.add("remote");
        buildArgs.add("--status-code-binding");
        Path projectGenPath = Paths.get(TEST_RESOURCE + "/project-04");
        Path projectExpectedPath = Paths.get(EXPECTED_RESOURCE + "/project-expected");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, projectGenPath, buildArgs);
        Assert.assertTrue(Files.exists(projectGenPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectGenPath.resolve("client.bal")));
        compareFiles(projectGenPath, "client.bal", projectExpectedPath, "client_remote.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("types.bal")));
        compareFiles(projectGenPath, "types.bal", projectExpectedPath, "types.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("utils.bal")));
        compareFiles(projectGenPath, "utils.bal", projectExpectedPath, "utils.bal");
    }

    @Test(description = "`--status-code-binding` option with service")
    public void serviceWithStatusCodeBinding() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("service");
        buildArgs.add("--status-code-binding");
        Path projectGenPath = Paths.get(TEST_RESOURCE + "/project-05");
        Path projectExpectedPath = Paths.get(EXPECTED_RESOURCE + "/project-05");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, projectGenPath, buildArgs);
        Assert.assertFalse(Files.exists(projectGenPath.resolve("service.bal")));
        Assert.assertTrue(Files.exists(projectGenPath.resolve("Ballerina.toml")));
        compareFiles(projectGenPath, "Ballerina.toml", projectExpectedPath, "Ballerina.toml");
    }

    @Test(description = "`--status-code-binding` option without any mode")
    public void commonWithStatusCodeBinding() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--status-code-binding");
        Path projectGenPath = Paths.get(TEST_RESOURCE + "/project-02");
        Path projectExpectedPath = Paths.get(EXPECTED_RESOURCE + "/project-expected");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, projectGenPath, buildArgs);
        Assert.assertTrue(Files.exists(projectGenPath.resolve("Ballerina.toml")));
        Assert.assertTrue(Files.exists(projectGenPath.resolve("client.bal")));
        compareFiles(projectGenPath, "client.bal", projectExpectedPath, "client_resource.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("types.bal")));
        compareFiles(projectGenPath, "types.bal", projectExpectedPath, "types_all.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("utils.bal")));
        compareFiles(projectGenPath, "utils.bal", projectExpectedPath, "utils_all.bal");
        Assert.assertTrue(Files.exists(projectGenPath.resolve("openapi_service.bal")));
        compareFiles(projectGenPath, "openapi_service.bal", projectExpectedPath, "openapi_service.bal");
    }

    @Test(description = "`--status-code-binding` without `Ballerina.toml`")
    public void nonBallerinaPackageWithStatusCodeBinding() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("--status-code-binding");
        Path projectGenPath = Paths.get(TEST_RESOURCE + "/project-03");
        Path projectExpectedPath = Paths.get(EXPECTED_RESOURCE + "/project-expected");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, projectGenPath, buildArgs);
        Assert.assertTrue(Files.exists(projectGenPath.resolve("client.bal")));
        compareFiles(projectGenPath, "client.bal", projectExpectedPath, "client_normal.bal");
    }

    /**
     * Compare two files.
     */
    private void compareFiles(Path genPath, String generatedFileName, Path expectedPath, String expectedFileName)
            throws IOException {
        Stream<String> expectedFile = Files.lines(expectedPath.resolve(expectedFileName));
        String expectedContent = expectedFile.collect(Collectors.joining(LINE_SEPARATOR));
        Stream<String> generatedFile = Files.lines(genPath.resolve(generatedFileName));
        String generatedContent = generatedFile.collect(Collectors.joining(LINE_SEPARATOR));
        generatedContent = generatedContent.trim().replaceAll("\\s+", "");
        expectedContent = expectedContent.trim().replaceAll("\\s+", "");
        Assert.assertEquals(generatedContent, expectedContent);
    }
}
