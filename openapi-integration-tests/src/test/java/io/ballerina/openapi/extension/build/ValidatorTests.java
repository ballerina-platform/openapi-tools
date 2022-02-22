/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.extension.build;

import io.ballerina.openapi.cmd.TestUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * This test class is for contain the openapi validator plugin tests.
 */
public class ValidatorTests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/validator");
    public static final String WHITESPACE_PATTERN = "\\s+";

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "OpenAPI validator plugin test for multiple services")
    public void testMultipleServices() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_1");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " ERROR [service.bal:(4:1,19:2)] Missing OpenAPI contract parameter 'q' in the counterpart" +
                " Ballerina service resource (method: 'get', path: '/weather')";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "Type mismatch path parameter")
    public void typeMismatchPathParameter() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_2");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " ERROR [service.bal:(11:49,11:60)] Type mismatch with parameter 'obsId' for the method 'get' " +
                "of the path '/applications/{obsId}/metrics'.In OpenAPI contract its type is 'string' " +
                "and resources type is 'int'.";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "Type mismatch path parameter for complex path")
    public void withComplexPathParameterTypeMisMatch() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_4");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " ERROR [service.bal:(7:1,23:2)] Could not find a Ballerina service resource for the path " +
                "'/applications/'{obsId}'/metrics/'{startTime}'' which is documented in the OpenAPI contract.";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "OpenAPI validator plugin test for multiple diagnostic with path parameter")
    public void withTypeMisMatchPathParameterMissOASQueryParameter() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_5");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = "ERROR [service.bal:(11:72,11:90)] Type mismatch with parameter 'startTime' for the method" +
                " 'get' of the path '/applications/{obsId}/metrics/{startTime}'.In OpenAPI contract its type is" +
                " 'integer' and resources type is 'string'. \n" +
                "    ERROR [service.bal:(7:1,23:2)] Missing OpenAPI contract parameter 'endTime' in the counterpart" +
                " Ballerina service resource (method: 'get', path: '/applications/{obsId}/metrics/{startTime}')";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "OpenAPI validator plugin test for exclude tags filter")
    public void validatorWithExcludeTagFilter() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_6");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = "ERROR [service.bal:(10:115,10:124)] Type mismatch with parameter 'mode' for " +
                "the method 'get' of the path '/weather'.In OpenAPI contract its type is 'string' and" +
                " resources type is 'int'. \n" +
                "    ERROR [service.bal:(4:1,16:2)] Missing OpenAPI contract parameter 'q' in the" +
                " counterpart Ballerina service resource (method: 'get', path: '/weather')";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "OpenAPI validator plugin test for compilation issue")
    public void validatorWithCompilationIssue() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_7");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " ERROR [service.bal:(7:1,7:1)] missing identifier\n" +
                "    ERROR [service.bal:(7:1,7:1)] undefined field '$missingNode$_0' in record" +
                " 'ballerina/openapi:";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "Invalid test for RequestPayload parameter: when request payload parameters", enabled = false)
    public void typeMisMatchingInRecord() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_3");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = " WARNING [service.bal:(5:10,5:18)] Type mismatching 'userName' field in the record type of the" +
                " parameter 'User' for the method 'post' of the path '/pets'.In OpenAPI contract its type " +
                "is 'string' and resources type is 'int'.";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution fail.");
            }
        }
    }

    @Test(description = "Test to assert validator errors for resources having the root path ('.')")
    public void validatorWithRootPath() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("project_8");
        InputStream successful = TestUtil.executeOpenapiBuild(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = "ERROR [service.bal:(12:30,12:36)] Type mismatch with parameter 'id' for the" +
                " method 'get' of the path '/'.In OpenAPI contract its type is 'string' and resources type is 'int'.";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("OpenAPIValidator execution failed.");
            }
        }
    }
}
