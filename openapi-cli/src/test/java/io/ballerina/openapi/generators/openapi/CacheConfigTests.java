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
 * This test class is for capturing all the cache configuration related tests.
 */
public class CacheConfigTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec for service configuration annotation in resource without fields")
    public void cacheConfigTests01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/configuration_rs01.yaml");
    }

    @Test(description = "When cache-config has custom value without ETag and Last-Modified.")
    public void cacheConfigTests02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs02.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_02.yaml");
    }

    @Test(description = "When cache-config has custom value with private field and no cache field enable")
    public void cacheConfigTests03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs03.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_03.yaml");
    }

    @Test(description = "When cache-config has custom value with negative max age")
    public void cacheConfigTests04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs04.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_04.yaml");
    }

    @Test(description = "When cache-config has custom value with negative max age")
    public void cacheConfigTests05() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs05.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_05.yaml");
    }

    @Test(description = "When cache-config has union type response with error")
    public void cacheConfigTests06() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/configuration_rs06.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/cache_config_06.yaml");
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
