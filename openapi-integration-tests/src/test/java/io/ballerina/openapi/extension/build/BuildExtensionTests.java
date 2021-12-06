/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCE;
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
        Assert.assertTrue(Files.exists(RESOURCE.resolve("project_2/target/openapi/greeting_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with graphQl service", enabled = false)
    public void withNonHttpServiceWithBuildOption() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_3"), buildArgs);
        Assert.assertTrue(Files.exists(RESOURCE.resolve("project_3/target/openapi/greeting_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with package has service on module")
    public void buildOptionWithSeparateModule() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_4"), buildArgs);
        Assert.assertTrue(Files.exists(RESOURCE.resolve("project_4/target/openapi/greeting_openapi.yaml")));
        Assert.assertTrue(Files.exists(RESOURCE.resolve("project_4/target/openapi/mod_openapi.yaml")));
    }

    @Test(description = "Check --export-openapi flag with single service file build", enabled = false)
    public void buildOptionWithSingleFile() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_5/service.bal"), buildArgs);
    }

    @Test(description = "Check --export-openapi flag with grpc service", enabled = false)
    public void buildOptionWithGrpcService() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("--export-openapi");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME,
                TEST_RESOURCE.resolve("project_6"), buildArgs);
    }

    @Test(description = "Check --export-openapi flag with webHub service", enabled = false)
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
