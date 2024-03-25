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

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    @Test(description = "Test with response error interceptor")
    public void testResponseErrorInterceptor() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/response_interceptors/interceptors06.yaml");
    }

    @Test(description = "Test with two response interceptors")
    public void testTwoResponseInterceptors() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors07.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/response_interceptors/interceptors07.yaml");

        ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors08.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/response_interceptors/interceptors08.yaml");
    }

    @Test(description = "Test with response and response error interceptors")
    public void testResponseAndResponseErrorInterceptors() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/response_interceptors/interceptors09.yaml");

        ballerinaFilePath = INTERCEPTOR_DIR.resolve("response_interceptors/interceptors10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/response_interceptors/interceptors10.yaml");
    }

    @Test(description = "Test with mixed interceptors")
    public void testMixedInterceptors() throws IOException {
        for (int i = 0; i <= 9; i++) {
            Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("mixed_interceptors/interceptors0" + i + ".bal");
            compareWithGeneratedFile(ballerinaFilePath, "interceptors/mixed_interceptors/interceptors0" + i + ".yaml");
        }

        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("mixed_interceptors/interceptors10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "interceptors/mixed_interceptors/interceptors10.yaml");
    }

    @Test(description = "Negative tests for interceptors")
    public void testNegativeInterceptors() throws IOException {
        Path ballerinaFilePath = INTERCEPTOR_DIR.resolve("negative/interceptors00.bal");
        List<OpenAPIMapperDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "interceptors/negative/interceptors00.yaml");
        Assert.assertEquals(errors.get(0).getMessage(), "Generated OpenAPI definition does not contain the" +
                " response/request parameter information from the interceptor pipeline. Cause: the return type of" +
                " `createInterceptors` function should be defined as a tuple with specific interceptor types as " +
                "members. For example: `[ResponseInterceptor_, RequestInterceptor_, RequestErrorInterceptor_]`");

        ballerinaFilePath = INTERCEPTOR_DIR.resolve("negative/interceptors01.bal");
        errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "interceptors/negative/interceptors00.yaml");
        Assert.assertEquals(errors.get(0).getMessage(), "Generated OpenAPI definition does not contain the" +
                " response/request parameter information from the interceptor pipeline. Cause: no class definition " +
                "found for the interceptor: Interceptor within the package. Make sure that the interceptor return " +
                "type is defined with the specific interceptor class type rather than the generic `http:Interceptor`" +
                " type and the specific interceptor class is defined within the package");

        ballerinaFilePath = INTERCEPTOR_DIR.resolve("negative/interceptors02.bal");
        errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "interceptors/negative/interceptors00.yaml");
        Assert.assertEquals(errors.get(0).getMessage(), "Generated OpenAPI definition does not contain the" +
                " response/request parameter information from the interceptor pipeline. Cause: no class definition " +
                "found for the interceptor: RequestInterceptor. Make sure that the interceptor return type is " +
                "defined with the specific interceptor class type rather than the generic `http:Interceptor` type");
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
