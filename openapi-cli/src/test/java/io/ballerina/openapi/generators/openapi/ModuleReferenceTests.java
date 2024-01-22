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

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class for the resolve reference in the other module in ballerina package.
 */
public class ModuleReferenceTests {
    private static final Path RES_DIR = Paths.get(
            "src/test/resources/ballerina-to-openapi/ballerina-project/service").toAbsolutePath();

    @Test(description = "Response with separate modules")
    public void testResponse01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("snowpeak.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "snowpeak.yaml");
    }

    @Test(description = "Response in separate modules with cache config")
    public void testResponse02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("snowpeak_cache_config.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "snowpeak_cache.yaml");
    }

    @Test(description = "Request Body with separate modules")
    public void testResponse03() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("snowpeak_request_body.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "snowpeak_request_body_ref.yaml");
    }

    @Test(description = "Request Body with separate modules when serivce configuration enable")
    public void testResponse04() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("snowpeak_service_config.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "snowpeak_service_config.yaml");
    }

    @Test(description = "Response has array type the array item type in separate module")
    public void testResponse05() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayTypeResponse.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "arrayTypeResponse.yaml");
    }

    @Test(description = "Response has payload annotation with media type defined in a separate module.")
    public void testResponse06() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("common_type_tests.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "response/common_types.yaml");
    }

    @Test
    public void testRecordReferenceWithReadOnly() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("readonly.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "readonly.yaml");
    }

    //To-Do: This test is disabled due to an Ubuntu build failure.
    @Test (enabled = false)
    public void testListenersInSeparateModule() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners_in_separate_module.bal");
        String osName = System.getProperty("os.name");
        String yamlFile = osName.toLowerCase().contains("windows") ? "windows_listeners_in_separate_module.yaml"
                : "listeners_in_separate_module.yaml";
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, yamlFile);
    }

    //To-Do: This test is disabled due to an Ubuntu build failure.
    @Test (enabled = false)
    public void testListenersInSeparateFiles() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listeners_in_separate_file.bal");
        String osName = System.getProperty("os.name");
        String yamlFile = osName.toLowerCase().contains("windows") ? "windows_listeners_in_separate_file.yaml"
                : "listeners_in_separate_file.yaml";
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, yamlFile);
    }
}
