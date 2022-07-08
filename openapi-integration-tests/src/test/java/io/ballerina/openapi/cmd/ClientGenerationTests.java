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
 * Integration tests for client resource functions.
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
        buildArgs.add("--resource-functions");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("client.bal")));
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
        buildArgs.add("--resource-functions");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
    }

    @Test(description = "`--resource-functions` option without any mode")
    public void commonWithResourceFunction() throws IOException, InterruptedException {
        String openapiFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(openapiFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--resource-functions");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("client.bal")));
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("openapi_service.bal")));
    }
}
