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
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.assertOnErrorStream;

/**
 * This {@code BallerinaToOpenAPITests} contains all the ballerina to openapi command negative.
 */
public class BallerinaToOpenAPICLINegativeTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/ballerina_sources");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Test for given bal service file contains compilation issues")
    public void testForGivenBalServiceHasCompilationIssue() throws IOException, InterruptedException {
        String balFilePath = "bal_service_has_compilation_issue/service.bal";
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "openapi");
        buildArgs.add("-i");
        buildArgs.add(balFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());

        Process process = getProcess(buildArgs, TEST_RESOURCE);

        String out = "OpenAPI contract generation failed due to Ballerina code has compilation errors.:\n" +
                "ERROR [service.bal:(11:1,11:2)] invalid token '}'";
        //Thread for wait out put generate
        Thread.sleep(5000);
        // compare generated file has not included constraint annotation for scenario record field.
        Assert.assertFalse(Files.exists(TEST_RESOURCE.resolve("cpi_openapi.yaml")));
        process.waitFor();
        assertOnErrorStream(process, out);
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtil.cleanDistribution();
    }
}
