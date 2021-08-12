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
package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.converter.OpenApiConverterException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class for the covering the unit tests for return type scenarios.
 */
public class APIDocTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Resource function api doc mapped to OAS operation summary")
    public void testsForResourceFunction() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/resource_function_scenario.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/resource_function.yaml");
    }

    @Test(description = "Resource function api doc mapped to OAS operation summary")
    public void testsForPathParameter() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/resource_function_with_pathparam_scenario.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/path_param.yaml");
    }

    @Test(description = "Query parameter api doc mapped to OAS parameter description")
    public void testsForQueryParameter() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/resource_function_with_queryparam_scenario.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/query_param.yaml");
    }

    @Test(description = "Request payload api doc mapped to OAS requestBody description")
    public void testsForRequestPayload() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/resource_function_with_requestbody_scenario.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/path_param.yaml");
    }

    @Test(description = "Record api doc mapped to OAS record description")
    public void testsForRecord() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/record.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/record.yaml");
    }

    @Test(description = "TypeInclusion record api doc mapped to OAS description")
    public void testsForRecordHasTypeInclusion() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/typeInclusion.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/typeInclusion.yaml");
    }

    @Test(description = "Reference scenarios", enabled = false)
    public void testsForReferenceScenario() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/reference_scenario.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/reference.yaml");
    }

    @Test(description = "Return description scenarios")
    public void testReturnDescription() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("apidoc/return_type.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "apidoc/path_param.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        TestUtils.deleteDirectory(this.tempDir);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
