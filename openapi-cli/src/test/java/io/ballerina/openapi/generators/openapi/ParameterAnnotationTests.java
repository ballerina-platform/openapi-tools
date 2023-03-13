/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.cmd.OASContractGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class is for capturing the scenarios with parameter annotations in resource methods.
 */
public class ParameterAnnotationTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test(description = "Test OpenAPI generation when payload is not annotated")
    public void testPayloadNotAnnotated() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/unannotated_payload.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "parameter_annotation/unannotated_payload.yaml");
    }

    @Test(description = "Test OpenAPI generation for complex scenarios with annotated payload")
    public void testComplexAnnotatedPayload() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/annotated_payload_complex.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath,
                "parameter_annotation/annotated_payload_complex.yaml");
    }

    @Test(description = "Test OpenAPI generation when query parameter is annotated")
    public void testQueryParameterAnnotated() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/annotated_query.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath,
                "parameter_annotation/annotated_query.yaml");
    }

    @Test(description = "Test OpenAPI generation when invalid parameters")
    public void testInvalidParameters() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("parameter_annotation/invalid_payload.bal");
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);
        Assert.assertTrue(Files.notExists(tempDir.resolve("payloadV_openapi.yaml")),
                "OpenAPI file is generated for a bal file with errors due to invalid " +
                        "resource function parameters.");
    }
}
