/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;

/**
 * This class contains all the OpenAPI to ballerina command related negative tests.
 */
public class OpenAPIToBallerinaCLINegativeTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/oas-resources");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "when the user has given invalid mode option")
    public void testForInvalidModeOption() throws IOException, InterruptedException {
        String balFilePath = "openapi.yaml";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "openapi");
        buildArgs.add("-i");
        buildArgs.add(balFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        buildArgs.add("--mode");
        buildArgs.add("server");

        Process process = getProcess(buildArgs, TEST_RESOURCE);
        //Thread for wait out put generate
        Thread.sleep(5000);
        int exitCode = process.waitFor();
        Assert.assertEquals(exitCode, 1);
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtil.cleanDistribution();
    }
}
