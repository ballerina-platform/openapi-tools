/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
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

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.extension.build.ValidatorTests.WHITESPACE_PATTERN;

/**
 * This test class created for contain the OpenAPI to Ballerina service generator return type checks.
 */
public class ServiceGenerationTests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/service/return");

    @Test(dataProvider = "returnSampleProvider")
    public void testResponseStatusCodeRecord(String openapiFilePath, String expectedFilePath)
            throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("swagger/" + openapiFilePath);
        buildArgs.add("--mode");
        buildArgs.add("service");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("types.bal", expectedFilePath);
    }

    @Test(description = "Negative test to assert warnings on unsupported OAS type formats")
    public void testUnsupportedOASFormatTests() throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("swagger/" + "unsupported_oas_format.yaml");
        buildArgs.add("--mode");
        buildArgs.add("service");
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        InputStream successful = TestUtil.executeOpenAPIToTestWarnings(
                DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        String msg = "WARNING: unsupported format `date` will be skipped when generating the counterpart " +
                "Ballerina type for openAPI schema type: `string`";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(successful))) {
            Stream<String> logLines = br.lines();
            String generatedLog = logLines.collect(Collectors.joining(System.lineSeparator()));
            logLines.close();
            generatedLog = (generatedLog.trim()).replaceAll(WHITESPACE_PATTERN, "");
            msg = (msg.trim()).replaceAll(WHITESPACE_PATTERN, "");
            if (generatedLog.contains(msg)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Unexpected service generation output.");
            }
        }
    }

    @DataProvider(name = "returnSampleProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"content_schema_null.yaml", "service/return/ballerina/content_schema_null.bal"},
                {"content_schema_has_one_of_type.yaml", "service/return/ballerina/" +
                        "content_schema_has_one_of_type.bal"},
                {"multiple_media_types_for_one_response_code.yaml", "service/return/ballerina/" +
                        "multiple_mediatype_for_one_response_code.bal"}, //
                // one response code has multiple media types (content types)
                {"response_has_inline_record.yaml", "service/return/ballerina/" +
                        "response_has_inline_record.bal"},
                {"same_response.yaml", "service/return/ballerina/same_response.bal"}, // Two resources have
                // same error code with same body type for return type.
                {"unsupported_payload_type.yaml", "service/return/ballerina/unsupported_payload_type.bal"},
                {"response_has_inline_additional_properties.yaml",
                        "service/return/ballerina/response_has_inline_additional_properties.bal"},
                {"post_method.yaml", "service/return/ballerina/post_method.bal"}
        };
    }
}
