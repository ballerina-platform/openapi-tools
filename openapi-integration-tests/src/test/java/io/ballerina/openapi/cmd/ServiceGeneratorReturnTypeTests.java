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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;

/**
 * This test class created for contain the OpenAPI to Ballerina service generator return type checks.
 */
public class ServiceGeneratorReturnTypeTests extends OpenAPITest {
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

    @DataProvider(name = "returnSampleProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"content_schema_null.yaml", "service/return/ballerina/content_schema_null.bal"}, // When the
                // response content payload schema type is null
                {"content_schema_has_one_of_type.yaml", "service/return/ballerina/" +
                        "content_schema_has_one_of_type.bal"}, // content type has one of type schemas
                {"multiple_media_types_for_one_response_code.yaml", "service/return/ballerina/" +
                        "multiple_mediatype_for_one_response_code.bal"}, //
                // one response code has multiple media types (content types)
                {"response_has_inline_record.yaml", "service/return/ballerina/" +
                        "response_has_inline_record.bal"}, // Response has inline object
                // schema for content schema.
                {"same_response.yaml", "service/return/ballerina/same_response.bal"} // Two resources have
                // same error code with same body type for return type.
        };
    }
}
