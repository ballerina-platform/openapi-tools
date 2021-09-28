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
 * Ballerina header conversion to OpenAPI will test in this class.
 */
public class HeaderTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/headers").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec with header type parameter")
    public void testHeadscenario01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario01.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec with header type parameter with annotation values")
    public void testHeadersWithAnnotation() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario02.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with header type parameter without curly brace")
    public void testHeadersWithOutCurlyBrace() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario03.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with for multiple headers")
    public void testWithMultipleHeaders() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario04.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with for optional headers")
    public void testOptionalHeaders() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario05.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario05.yaml");
    }

    @Test(description = "Generate OpenAPI spec with when the service config has nullable and optional enable field")
    public void testHeadersWithAnnotations() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario06.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario06.yaml");
    }

    @Test(description = "Generate OpenAPI spec when the header has defaultable parameter")
    public void testHeadersWithDefaultValue() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario07.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario07.yaml");
    }

    @Test(description = "Generate OpenAPI spec when the header has defaultable parameter with nullable enable data " +
            "type")
    public void testHeadersWithDefaultValueWithNullable() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario08.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "header_scenario08.yaml");
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
