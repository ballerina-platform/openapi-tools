/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.idl.client;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCE;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.cmd.TestUtil.executeRun;

/**
 * Client IDL import negative integration tests.
 */
public class IDLClientGenPluginNegativeTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client-idl-projects");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "When client declaration has graphQl yaml")
    public void graphQLYaml() throws IOException, InterruptedException {
        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_07"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_07/generated/").toString());
        Assert.assertFalse(dir.exists());
    }
    @Test(description = "Provide invalid swagger remote path")
    public void invalidSwaggerRemotePath() throws IOException, InterruptedException {
        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_10"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_10/generated/").toString());
        Assert.assertFalse(dir.exists());
    }
    @Test(description = "Provide valid swagger local path")
    public void invalidSwaggerLocalPath() throws IOException, InterruptedException {
        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_11"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_11/generated/").toString());
        Assert.assertFalse(dir.exists());
    }

    @Test(description = "Provide swagger has issue in contain")
    public void invalidSwaggerContract() throws IOException, InterruptedException {
        boolean successful = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_12"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_12/generated/").toString());
        Assert.assertFalse(dir.exists());
    }
}
