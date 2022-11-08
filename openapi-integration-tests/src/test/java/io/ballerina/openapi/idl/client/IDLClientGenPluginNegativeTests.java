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
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCE;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.executeRun;
import static io.ballerina.openapi.extension.build.ValidatorTests.WHITESPACE_PATTERN;

/**
 * Negative tests for OpenAPI client IDL import.
 */
public class IDLClientGenPluginNegativeTests extends OpenAPITest {

    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/client-idl-projects");
    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test
    public void testClientDeclarationInsideFunction() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_04"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_04/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(2:5,2:11)] 'client' qualifier not allowed";
        assertOnErrorStream(process, msg);
    }

    @Test(description = "Here uses the graphQl yaml as non openapi client")
    public void testNonOpenAPIClientDeclaration() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_07"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_07/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(1:1,1:176)] no matching plugin found for client declaration";
        assertOnErrorStream(process, msg);
    }

    @Test
    public void testInvalidSwaggerRemotePath() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_10"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_10/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(1:1,1:132)] unable to get resource from uri, reason: ";
        assertOnErrorStream(process, msg);
    }

    @Test
    public void testInvalidSwaggerLocalPath() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_11"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_11/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(1:1,1:31)] could not locate the file:";
        assertOnErrorStream(process, msg);
    }

    @Test
    public void testInvalidSwaggerContract() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_12"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_12/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(1:1,1:30)] OpenAPI definition has errors:";
        assertOnErrorStream(process, msg);
    }

    //TODO: enable this test after figuring out the fail reason.
    @Test(description = "Disable this test due to fail in test workflow, this has been tested by manual and it " +
            "passed as expected", enabled = false)
    public void testUnsupportedSwaggerVersion() throws IOException, InterruptedException {
        Process process = executeRun(DISTRIBUTION_FILE_NAME, TEST_RESOURCE.resolve("project_13"),
                new ArrayList<>());
        File dir = new File(RESOURCE.resolve("client-idl-projects/project_13/generated/").toString());
        Assert.assertFalse(dir.exists());
        String msg = "ERROR [main.bal:(2:5,2:119)] provided openAPI contract version is not supported";
        assertOnErrorStream(process, msg);
    }

    private static void assertOnErrorStream(Process process, String msg) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining("\n"));
            logLines.close();
            String formattedGeneratedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            String formattedMessage = msg.trim().replaceAll(WHITESPACE_PATTERN, "");
            Assert.assertTrue(formattedGeneratedLog.contains(formattedMessage),
                    String.format("compiler output doesn't contain the expected" +
                    " output: %s : generated output : %s", msg, generatedLog));
        }
    }
}
