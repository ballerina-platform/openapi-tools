/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.openapi.TestUtils.compareWithGeneratedFile;

/**
 * This test class for the covering the unit tests for interceptor scenarios.
 */
public class InterceptorTests {

    private static final Path INTERCEPTOR_DIR = Paths.get("src/test/resources/" +
            "ballerina-to-openapi/interceptors").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Test with request interceptor")
    public void testRequestInterceptor() throws IOException {
        for (int i = 0; i <= 9; i++) {
            Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("request_interceptors/interceptors0" + i + ".bal");
            compareWithGeneratedFile(ballerinaFilePath,
                    "interceptors/request_interceptors/interceptors0" + i + ".yaml");
        }
    }

    @Test(description = "Test with two request interceptors")
    public void testTwoRequestInterceptors() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("request_interceptors/interceptors10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/request_interceptors/interceptors10.yaml");
    }

    @Test(description = "Test with request error interceptor")
    public void testRequestErrorInterceptor() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("request_interceptors/interceptors11.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/request_interceptors/interceptors11.yaml");
    }

    @Test(description = "Test with response interceptor")
    public void testResponseInterceptor() throws IOException {
        for (int i = 0; i <= 5; i++) {
            Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors0" + i + ".bal");
            compareWithGeneratedFile(ballerinaFilePath,
                    "interceptors/response_interceptors/interceptors0" + i + ".yaml");
        }
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
