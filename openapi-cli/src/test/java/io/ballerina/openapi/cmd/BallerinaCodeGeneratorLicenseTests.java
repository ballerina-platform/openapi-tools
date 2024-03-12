/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OpenAPI to Ballerina command tests for check license headers.
 */
public class BallerinaCodeGeneratorLicenseTests extends OpenAPICommandTest {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        super.setup();
    }

    @Test(description = "Test openapi to ballerina client generation with default file headers")
    public void testClientGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--mode", "client"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();

        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            try {
                compareFiles("schema.bal", "types.bal");
                compareFiles("client.bal", "client.bal");
                compareFiles("utils_for_client_generation.bal", "utils.bal");
                deleteGeneratedFiles(false);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina service type generation with default file headers")
    public void testServiceTypeGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--with-service-type"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();

        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("service_type.bal"))) {
            try {
                compareFiles("schema_for_both_service_client.bal", "types.bal");
                compareFiles("client.bal", "client.bal");
                compareFiles("utils_for_both_service_client_generation.bal", "utils.bal");
                compareFiles("service_type.bal", "service_type.bal");
                deleteGeneratedFiles(false);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina service generation with default file headers")
    public void testServiceGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--with-service-type",
                "--mode", "io/ballerina/openapi/corenew/service"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();

        if (Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("service_type.bal"))) {
            try {
                compareFiles("schema_for_service.bal", "types.bal");
                compareFiles("service_with_service_type.bal", "petstore_service.bal");
                compareFiles("service_type.bal", "service_type.bal");
                deleteGeneratedFiles(false);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina code generation with default file headers")
    public void testBothClientServiceGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();

        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal"))) {
            try {
                compareFiles("types_for_both_service_client_generations.bal", "types.bal");
                compareFiles("client.bal", "client.bal");
                compareFiles("utils_for_both_service_client_generation.bal", "utils.bal");
                compareFiles("service.bal", "petstore_service.bal");
                deleteGeneratedFiles(false);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina code generation with user provided license headers")
    public void testUserGivenLicenseHeader() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("expected_gen/licenses/license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString(), "--with-service-type"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("service_type.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal"))) {
            try {
                compareFiles("schema_with_user_given_license.bal", "types.bal");
                compareFiles("client_with_user_given_license.bal", "client.bal");
                compareFiles("utils_for_with_user_given_license.bal", "utils.bal");
                compareFiles("service_type_with_user_given_license.bal", "service_type.bal");
                compareFiles("service_with_user_given_license.bal", "petstore_service.bal");
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            } finally {
                deleteGeneratedFiles(true);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    /**
     * Compare two files.
     */
    private void compareFiles(String expectedFileName, String generatedFileName) throws IOException {
        Stream<String> expectedFile = Files.lines(resourceDir.resolve(Paths.get("expected_gen/licenses",
                expectedFileName)));
        String expectedContent = expectedFile.collect(Collectors.joining(LINE_SEPARATOR));
        Stream<String> generatedFile = Files.lines(this.tmpDir.resolve(generatedFileName));
        String generatedContent = generatedFile.collect(Collectors.joining(LINE_SEPARATOR));
        generatedContent = generatedContent.trim().replaceAll("\\s+", "");
        expectedContent = expectedContent.trim().replaceAll("\\s+", "");
        Assert.assertEquals(generatedContent, expectedContent);
    }

    // Delete the generated files
    private void deleteGeneratedFiles(boolean isConfigGenerated) throws IOException {
        File serviceFile = new File(this.tmpDir.resolve("petstore_service.bal").toString());
        File clientFile = new File(this.tmpDir.resolve("client.bal").toString());
        File schemaFile = new File(this.tmpDir.resolve("types.bal").toString());
        File testFile = new File(this.tmpDir.resolve("tests/test.bal").toString());
        File testDir = new File(this.tmpDir.resolve("tests").toString());
        serviceFile.delete();
        clientFile.delete();
        schemaFile.delete();
        testFile.delete();
        if (isConfigGenerated) {
            File configFile = new File(this.tmpDir.resolve("tests/Config.toml").toString());
            configFile.delete();
        }
        FileUtils.deleteDirectory(testDir);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
