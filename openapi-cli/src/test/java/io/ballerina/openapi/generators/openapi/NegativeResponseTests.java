/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class is covering the negative unit tests for return type scenarios.
 */
public class NegativeResponseTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "When the resource has nil type return")
    public void testNilReturnType() {
        Path ballerinaFilePath = RES_DIR.resolve("response/negative/nil_return_type.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        //TODO: Uncomment after merging this PR https://github.com/ballerina-platform/openapi-tools/pull/1370.
//        Assert.assertFalse(openApiConverterUtils.getErrors().isEmpty());
        Assert.assertFalse(Files.exists(tempDir.resolve("payloadV_openapi.yaml")));
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
