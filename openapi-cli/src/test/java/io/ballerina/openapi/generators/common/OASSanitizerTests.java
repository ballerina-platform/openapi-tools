/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.generators.common;

import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.OASSanitizer;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This contains the OAS modification tests.
 */
public class OASSanitizerTests {
    //TODO: enable these tests separately, currently fix was tested by using connectors in manually.
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/sanitizer").toAbsolutePath();
    @Test(description = "Functionality tests for getBallerinaOpenApiType", enabled = false)
    public void testForRecordName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("record.yaml");
        Path expectedPath = RES_DIR.resolve("modified_record.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASSanitizer oasSanitizer = new OASSanitizer(openAPI);
        OpenAPI sanitized = oasSanitizer.sanitized();
        // file comparison
        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
        Assert.assertEquals(sanitized, expectedFileContent);
    }

    public void pathParameter() {

    }

    // expect: not modify the query parameter names
    public void queryParameter() {

    }

    public void duplicateRecordName () {

    }

    public void pathAndRecordHasSameName() {

    }

    public void parameterNameHasBlank() {

    }

    @Test
    public void recursiveRecordName() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RES_DIR.resolve("recursive_record.yaml");
//        Path expectedPath = RES_DIR.resolve("modified_recursive_record.yaml");
//        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
//        OASSanitizer oasSanitizer = new OASSanitizer(openAPI);
//        OpenAPI sanitized = oasSanitizer.sanitized();
        // file comparison
//        OpenAPI expectedFileContent = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(expectedPath);
//        Assert.assertEquals(sanitized, expectedFileContent);
    }

    // parameter in separate section
    // request section
    // response section
    // recursive section
}
