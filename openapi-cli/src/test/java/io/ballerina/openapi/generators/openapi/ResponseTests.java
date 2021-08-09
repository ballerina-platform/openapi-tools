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
public class ResponseTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Response scenario01 without return type")
    public void testResponse01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario01.yaml");
    }

    @Test(description = "Response scenario02 without return type")
    public void testResponse02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario02.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario02.yaml");
    }

    @Test(description = "Response scenario03 - return type with Record")
    public void testResponse03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario03.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario03.yaml");
    }

    @Test(description = "Response scenario 04 - Response has multiple responses without content type")
    public void testResponse04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario04.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario04.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema", enabled = false)
    public void testResponse05() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario05.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "rs_scenario05.yaml");
    }

    @Test(description = "Response scenario 06 - Error response with a schema", enabled = false)
    public void testResponse06() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario06.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario06.yaml");
    }

    @Test(description = "Response scenario 09 - return has record, error, basic types")
    public void testResponse09() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario09.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario09.yaml");
    }

    @Test(description = "Response scenario 10 - Array type response with a schema")
    public void testResponse10() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario10.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario10.yaml");
    }

    @Test(description = "When the return type is record with typeInclusion field of http code ")
    public void testTypeInclusion() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/typeInclusion_01.bal");
        //Compare generated yaml file with expected yaml content
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/typeInclusion_01.yaml");
    }

    @Test(description = "When the return type is string")
    public void testStringReturn() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario11.bal");
        //Compare generated yaml file with expected yaml content
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario11.yaml");
    }

    @Test(description = "When the return type is inline record")
    public void testInlineRecordReturn() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario12.bal");
        //Compare generated yaml file with expected yaml content
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario12.yaml");
    }

    @Test(description = "When the return type is inline record")
    public void testInlineRecordHasHttpTypeInclusion() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario13.bal");
        //Compare generated yaml file with expected yaml content
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario13.yaml");
    }

    @Test(description = "When the return type is inline record", enabled = false)
    public void testInlineRecordHasReference() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario14.bal");
        //Compare generated yaml file with expected yaml content
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario14.yaml");
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
