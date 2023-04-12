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

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * This test class is for capturing special tests regarding resource methods.
 */
public class HttpMethodTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test(description = "Compiler warning for 'default' resource methods.")
    public void testForCompilerWarningForDefault() {
        Path ballerinaFilePath = RES_DIR.resolve("default_bal.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);
        List<OpenAPIConverterDiagnostic> errors = openApiConverter.getErrors();
        Assert.assertFalse(errors.isEmpty());
        Assert.assertEquals(errors.get(0).getMessage(), "Generated OpenAPI definition does not " +
                "contain details for the `default` resource method in the Ballerina service.");
    }

    @Test
    public void testForPostMethodStatusCodeMapping() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("post_method.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "post_method.yaml");
    }

    @Test
    public void testForResourceWithCatchAllPath() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("catch_all_path_service.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "catch_all_path_service.yaml");
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
