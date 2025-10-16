/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.GeneratorTestUtils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtils.skipOnWindows;
import static io.ballerina.openapi.cmd.ErrorMessages.MISSING_CONTRACT_PATH;

/**
 * OpenAPI command test suit.
 */
public class OpenAPICmdTest extends OpenAPICommandTest {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


    @BeforeTest(description = "This will create a new ballerina project for testing below scenarios.")
    public void setupBallerinaProject() throws IOException {
        super.setup();
        System.setOut(new PrintStream(outputStream));
    }

    @Test(description = "Test openapi command with help flag")
    public void testOpenAPICmdHelp() throws IOException {
        String[] args = {"-h"};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME") &&
            output.contains("bal openapi - Generate Ballerina services and clients from OpenAPI") &&
            output.contains("contracts, or export OpenAPI specifications from Ballerina services."));
    }

    @Test(description = "Test openapi command with help flag")
    public void testOpenAPICmdHelpWithBalTools() throws IOException {
        String[] args = {"help", "openapi"};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains(MISSING_CONTRACT_PATH));
    }

    @Test(description = "Test openapi command without help flag")
    public void testOpenAPICmdHelpWithoutFlag() throws IOException {
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME") &&
                output.contains("bal openapi - Generate Ballerina services and clients from OpenAPI") &&
                output.contains("contracts, or export OpenAPI specifications from Ballerina services."));
    }

    @Test(description = "Test openapi gen-service without openapi contract file",
    expectedExceptions = {CommandLine.MissingParameterException.class})
    public void testWithoutOpenApiContract() throws IOException {
        String[] args = {"--input"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Missing required parameter for option '--input' (<inputPath>)"));
    }

    @Test(description = "Test openapi gen-service for successful service generation")
    public void testSuccessfulServiceGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen", "petstore_schema.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Check the type content in openapi-to-ballerina command when using to generate both " +
            "client and service")
    public void testSuccessfulTypeBalGeneration() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_type.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen", "petstore_schema_type.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_type_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
            }
            deleteGeneratedFiles(false);
        } else {
            Assert.fail("Type generation failed. : " + readOutput(true));
        }
    }

    @Test
    public void testForRemovedUnusedConstraintImport() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("non_constraint_import.yaml"));
        String[] args = {"--input", petstoreYaml.toString(),
                "--mode", "client", "--tags", "pets", "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen",
                "non_constraint_import_types.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedSchema, expectedSchemaContent,
                    "Expected content and actual generated content is mismatched for: " + petstoreYaml);
            deleteGeneratedFiles(false);
        } else {
            Assert.fail("Type generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "This test for checking the constraint import when the type uses the constraint")
    public void testWithConstraintImport() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("constraint_import.yaml"));
        String[] args = {"--input", petstoreYaml.toString(),
                "--mode", "client", "--tags", "pets", "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen",
                "constraint_import_types.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedSchema, expectedSchemaContent,
                    "Expected content and actual generated content is mismatched for: " + petstoreYaml);
            deleteGeneratedFiles(false);
        } else {
            Assert.fail("Type generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina generation with license headers")
    public void testGenerationWithLicenseHeaders() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen",
                "petstore_schema_with_license.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina generation with no new line license headers")
    public void testGenerationWithLicenseHeadersWithOneNewLine() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license_with_new_line.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen",
                "generated_client_with_license.bal"));
        String expectedClientContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedClientContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedClientContent = "";
            try (Stream<String> generatedClientLines = Files.lines(this.tmpDir.resolve("client.bal"))) {
                generatedClientContent = generatedClientLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedClientContent = (generatedClientContent.trim()).replaceAll("\\s+", "");
            expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
            if (expectedClientContent.equals(generatedClientContent)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina connector generation filtering by tags")
    public void testConnectorGenerationFilteringByTags() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_tags.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--tags",
                "pets,dogs", "--mode", "client", "--client-methods", "remote"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedClientFile = resourceDir.resolve(Paths.get("expected_gen",
                "client_filtered_by_tags.bal"));
        String expectedClientContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedClientFile)) {
            expectedClientContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("utils.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare client contents
            String generatedClientContent = "";
            try (Stream<String> generatedClientLines = Files.lines(this.tmpDir.resolve("client.bal"))) {
                generatedClientContent = generatedClientLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedClientContent = (generatedClientContent.trim()).replaceAll("\\s+", "");
            expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
            if (expectedClientContent.equals(generatedClientContent)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi to ballerina generation with license headers and test suit")
    public void testGenerationOfTestSuiteWithLicenseHeaders() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_with_oauth.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString(), "--with-tests"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedConfigFilePath = resourceDir.resolve(Paths.get("expected_gen",
                "bearer_config.toml"));
        String expectedConfig = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedConfigFilePath)) {
            expectedConfig = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_with_oauth_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                Files.exists(this.tmpDir.resolve("tests/Config.toml")) &&
                Files.exists(this.tmpDir.resolve("tests/test.bal"))) {
            //Compare schema contents
            String generatedConfig = "";
            try (Stream<String> configContent = Files.lines(this.tmpDir.resolve("tests/Config.toml"))) {
                generatedConfig = configContent.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedConfig = (generatedConfig.trim()).replaceAll("\\s+", "");
            expectedConfig = (expectedConfig.trim()).replaceAll("\\s+", "");
            if (expectedConfig.equals(generatedConfig)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(true);
            }
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test exception when invalid prefix file given")
    public void testInvalidPrefixFile() throws IOException, BallerinaOpenApiException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("licence.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("Invalid license file path : "));
    }

    @Test(description = "Test generation without including test files")
    public void testClientGenerationWithoutIncludeTestFilesOption() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore_with_oauth.yaml"));
        Path licenseHeader = resourceDir.resolve(Paths.get("license.txt"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString(), "--license",
                licenseHeader.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedConfigFilePath = resourceDir.resolve(Paths.get("expected_gen",
                "bearer_config.toml"));
        String expectedConfig = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedConfigFilePath)) {
            expectedConfig = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_with_oauth_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal")) &&
                !Files.exists(this.tmpDir.resolve("tests/Config.toml")) &&
                !Files.exists(this.tmpDir.resolve("tests/test.bal"))) {

            Assert.assertTrue(true);
            deleteGeneratedFiles(true);
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi gen-service for .yml file service generation")
    public void testSuccessfulServiceGenerationForYML() throws IOException {
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedSchemaFile = resourceDir.resolve(Paths.get("expected_gen", "petstore_schema_2.bal"));
        String expectedSchemaContent = "";
        try (Stream<String> expectedSchemaLines = Files.lines(expectedSchemaFile)) {
            expectedSchemaContent = expectedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal")) &&
                Files.exists(this.tmpDir.resolve("petstore_service.bal")) &&
                Files.exists(this.tmpDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedSchema = "";
            try (Stream<String> generatedSchemaLines = Files.lines(this.tmpDir.resolve("types.bal"))) {
                generatedSchema = generatedSchemaLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
            expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
            if (expectedSchemaContent.equals(generatedSchema)) {
                Assert.assertTrue(true);
                deleteGeneratedFiles(false);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi service generation for single file option")
    public void testSingleFileServiceGeneration() throws IOException {
        Path jiraYaml = resourceDir.resolve(Paths.get("jira_openapi.yaml"));
        String[] args = {"--input", jiraYaml.toString(), "-o", this.tmpDir.toString(), "--mode",
                "service", "--single-file"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedFile = resourceDir.resolve(Paths.get("expected_gen", "jira_openapi_service.bal"));
        String expectedFileContent = "";
        try (Stream<String> expectedLines = Files.lines(expectedFile)) {
            expectedFileContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("jira_openapi_service.bal"))) {
            String generatedFile = "";
            try (Stream<String> generatedLines = Files.lines(this.tmpDir.resolve("jira_openapi_service.bal"))) {
                generatedFile = generatedLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
            expectedFileContent = (expectedFileContent.trim()).replaceAll("\\s+", "");
            deleteGeneratedFiles("jira_openapi_service.bal", false);
            if (!expectedFileContent.equals(generatedFile)) {
                Assert.fail("Expected content and actual generated content is mismatched for: " + jiraYaml);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi service generation for single file option with service type")
    public void testSingleFileServiceGenerationWithServiceType() throws IOException {
        Path jiraYaml = resourceDir.resolve(Paths.get("jira_openapi.yaml"));
        String[] args = {"--input", jiraYaml.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--single-file", "--with-service-type"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedFile = resourceDir.resolve(Paths.get("expected_gen", "jira_openapi_service_with_type.bal"));
        String expectedFileContent = "";
        try (Stream<String> expectedLines = Files.lines(expectedFile)) {
            expectedFileContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("jira_openapi_service.bal"))) {
            String generatedFile = "";
            try (Stream<String> generatedLines = Files.lines(this.tmpDir.resolve("jira_openapi_service.bal"))) {
                generatedFile = generatedLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
            expectedFileContent = (expectedFileContent.trim()).replaceAll("\\s+", "");
            deleteGeneratedFiles("jira_openapi_service.bal", false);
            if (!expectedFileContent.equals(generatedFile)) {
                Assert.fail("Expected content and actual generated content is mismatched for: " + jiraYaml);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi service generation for single file option with service type without data binding")
    public void testSingleFileServiceGenerationWithoutDatabinding() throws IOException {
        Path jiraYaml = resourceDir.resolve(Paths.get("jira_openapi.yaml"));
        String[] args = {"--input", jiraYaml.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--single-file", "--with-service-type", "--without-data-binding"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedFile = resourceDir.resolve(Paths.get("expected_gen",
                "jira_openapi_service_with_type_without_data_binding.bal"));
        String expectedFileContent = "";
        try (Stream<String> expectedLines = Files.lines(expectedFile)) {
            expectedFileContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("jira_openapi_service.bal"))) {
            String generatedFile = "";
            try (Stream<String> generatedLines = Files.lines(this.tmpDir.resolve("jira_openapi_service.bal"))) {
                generatedFile = generatedLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
            expectedFileContent = (expectedFileContent.trim()).replaceAll("\\s+", "");
            deleteGeneratedFiles("jira_openapi_service.bal", false);
            if (!expectedFileContent.equals(generatedFile)) {
                Assert.fail("Expected content and actual generated content is mismatched for: " + jiraYaml);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test openapi client generation for single file option")
    public void testSingleFileClientGeneration() throws IOException {
        Path jiraYaml = resourceDir.resolve(Paths.get("jira_openapi.yaml"));
        String[] args = {"--input", jiraYaml.toString(), "-o", this.tmpDir.toString(), "--mode", "client",
                "--single-file"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedFile = resourceDir.resolve(Paths.get("expected_gen", "jira_openapi_client.bal"));
        String expectedFileContent = "";
        try (Stream<String> expectedLines = Files.lines(expectedFile)) {
            expectedFileContent = expectedLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(this.tmpDir.resolve("client.bal"))) {
            String generatedFile = "";
            try (Stream<String> generatedLines = Files.lines(this.tmpDir.resolve("client.bal"))) {
                generatedFile = generatedLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedFile = (generatedFile.trim()).replaceAll("\\s+", "");
            expectedFileContent = (expectedFileContent.trim()).replaceAll("\\s+", "");
            deleteGeneratedFiles("", false);
            if (!expectedFileContent.equals(generatedFile)) {
                Assert.fail("Expected content and actual generated content is mismatched for: " + jiraYaml);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test Ballerina service generation with service type")
    public void testServiceTypeGeneration() throws IOException {
        Path projectDir = resourceDir.resolve("expected_gen").resolve("ballerina_project");
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", projectDir.toString(), "--with-service-type"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedServiceTypeFile = resourceDir.resolve(Paths.get("expected_gen", "pestore_service_type.bal"));
        String expectedServiceTypeContent = "";
        try (Stream<String> expectedServiceTypeLines = Files.lines(expectedServiceTypeFile)) {
            expectedServiceTypeContent = expectedServiceTypeLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(projectDir.resolve("client.bal")) &&
                Files.exists(projectDir.resolve("petstore_service.bal")) &&
                Files.exists(projectDir.resolve("types.bal")) &&
                Files.exists(projectDir.resolve("service_type.bal"))) {
            //Compare schema contents
            String generatedServiceType = "";
            try (Stream<String> generatedServiceTypeLines = Files.lines(projectDir.resolve("service_type.bal"))) {
                generatedServiceType = generatedServiceTypeLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedServiceType = (generatedServiceType.trim()).replaceAll("\\s+", "");
            expectedServiceTypeContent = (expectedServiceTypeContent.trim()).replaceAll("\\s+", "");
            deleteGeneratedFiles(false, projectDir, true);
            Assert.assertEquals(generatedServiceType, expectedServiceTypeContent,
                    "Expected content and actual generated content is mismatched for: " + petstoreYaml.toString());
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }
    }

    @Test(description = "Test Ballerina service generation without data binding")
    public void testServiceGenerationWithoutDataBinding() throws IOException {
        Path projectDir = resourceDir.resolve("expected_gen").resolve("ballerina_project");
        Path petstoreYaml = resourceDir.resolve(Paths.get("petstore.yaml"));
        String[] args = {"--input", petstoreYaml.toString(), "-o", projectDir.toString(), "--without-data-binding"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Path expectedService = resourceDir.resolve(Paths.get("expected_gen", "generic_service_petstore.bal"));
        String expectedServiceContent = "";
        try (Stream<String> expectedServiceTypeLines = Files.lines(expectedService)) {
            expectedServiceContent = expectedServiceTypeLines.collect(Collectors.joining(LINE_SEPARATOR));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        if (Files.exists(projectDir.resolve("client.bal")) &&
                Files.exists(projectDir.resolve("petstore_service.bal")) &&
                Files.exists(projectDir.resolve("types.bal"))) {
            //Compare schema contents
            String generatedService = "";
            try (Stream<String> generatedServiceLines = Files.lines(projectDir.resolve("petstore_service.bal"))) {
                generatedService = generatedServiceLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            generatedService = (generatedService.trim()).replaceAll("\\s+", "");
            expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");
            if (expectedServiceContent.equals(generatedService)) {
                SemanticModel semanticModel = GeneratorTestUtils.getSemanticModel(
                        projectDir.resolve("petstore_service.bal"));
                boolean hasErrors = semanticModel.diagnostics().stream()
                        .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
                Assert.assertFalse(hasErrors, "Errors found in generated service");
                deleteGeneratedFiles(false, projectDir, true);
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + petstoreYaml.toString());
                deleteGeneratedFiles(false, projectDir, true);
            }
        } else {
            Assert.fail("Service generation failed. : " + readOutput(true));
        }

    }

    @Test(description = "Test for service generation with yaml contract without operationID")
    public void testForYamlContractWithoutOperationID() throws IOException {
        Path yamlContract = resourceDir.resolve(Paths.get("without_operationID.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString(), "--mode",
                "service"};
        OpenApiCmd cmd = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        if (Files.exists(this.tmpDir.resolve("without_operationid_service.bal"))) {
            Assert.assertTrue(true);
            File schemaFile = new File(this.tmpDir.resolve("types.bal").toString());
            File serviceFile = new File(this.tmpDir.resolve("without_operationid_service.bal").toString());
            serviceFile.delete();
            schemaFile.delete();
        } else {
            Assert.fail("Code generation failed. : " + readOutput(true));
        }
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

    private void deleteGeneratedFiles(String filename, boolean isConfigGenerated) throws IOException {
        File serviceFile = new File(this.tmpDir.resolve(filename).toString());
        File clientFile = new File(this.tmpDir.resolve("client.bal").toString());
        File testFile = new File(this.tmpDir.resolve("tests/test.bal").toString());
        File testDir = new File(this.tmpDir.resolve("tests").toString());
        if (serviceFile.exists()) {
            serviceFile.delete();
        }
        if (clientFile.exists()) {
            clientFile.delete();
        }
        if (testFile.exists()) {
            testFile.delete();
        }
        if (isConfigGenerated) {
            File configFile = new File(this.tmpDir.resolve("tests/Config.toml").toString());
            configFile.delete();
        }
        FileUtils.deleteDirectory(testDir);
    }

    private void deleteGeneratedFiles(boolean isConfigGenerated, Path folderPath, boolean isServiceTypeGenerated)
            throws IOException {
        File serviceFile = new File(folderPath.resolve("petstore_service.bal").toString());
        File clientFile = new File(folderPath.resolve("client.bal").toString());
        File schemaFile = new File(folderPath.resolve("types.bal").toString());
        File testFile = new File(folderPath.resolve("tests/test.bal").toString());
        File testDir = new File(folderPath.resolve("tests").toString());
        serviceFile.delete();
        clientFile.delete();
        schemaFile.delete();
        testFile.delete();
        if (isConfigGenerated) {
            File configFile = new File(folderPath.resolve("tests/Config.toml").toString());
            configFile.delete();
        }
        if (isServiceTypeGenerated) {
            File serviceTypeFile = new File(folderPath.resolve("service_type.bal").toString());
            File utilFile = new File(folderPath.resolve("utils.bal").toString());
            utilFile.delete();
        }
        FileUtils.deleteDirectory(testDir);
    }

    @Test(description = "getRelative path")
    public void getRelativePath() {
        OpenApiCmd cmd = new OpenApiCmd();
        File resource01 = new File("dir1/test.txt");
        String target01 = "dir1/dir2";
        File resource02 = new File("dir1/dir2/dir3/test.txt");
        String target02 = "dir1/dir2";
        File resource03 = new File("dir2/dir3/dir4/test.txt");
        String target03 = "dir/dir1";
        Assert.assertTrue((cmd.getRelativePath(resource01, target01).toString()).equals("../test.txt") ||
                (cmd.getRelativePath(resource01, target01).toString()).equals("..\\test.txt"));
        Assert.assertTrue((cmd.getRelativePath(resource02, target02).toString()).equals("dir3/test.txt") ||
                (cmd.getRelativePath(resource02, target02).toString()).equals("dir3\\test.txt"));
        Assert.assertTrue((cmd.getRelativePath(resource03, target03).toString()).
                equals("../../dir2/dir3/dir4/test.txt") || (cmd.getRelativePath(resource03, target03).toString()).
                equals("..\\..\\dir2\\dir3\\dir4\\test.txt"));
    }

    @Test(description = "service generation for the parameterized path in OAS")
    public void testForComplexPathInService() {
        Path yamlContract = resourceDir.resolve(Paths.get("complexPath.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString(), "--mode", "service"};
        OpenApiCmd cmd = new OpenApiCmd(standardOut, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        String expectedOutput = "service generation can not be done as the" +
                " openapi definition contain following complex path(s):" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyFrom" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}.{sheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/payroll/v1/workers/{associateoid}/organizational-pay-statements/{payStatementId}/images/" +
                "{imageId}.{imageExtension}" + System.lineSeparator() +
                "/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier=" +
                "'{userDefinedIdentifier}')" + System.lineSeparator() +
                "/companies({company_id})/items({item_id})";

        Assert.assertTrue(outputStream.toString().contains(expectedOutput));
    }

    @Test(description = "service type generation for the parameterized path in OAS")
    public void testForComplexPathInServiceType() {
        Path yamlContract = resourceDir.resolve(Paths.get("complexPath.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString(), "--mode", "service",
                "--with-service-type"};
        OpenApiCmd cmd = new OpenApiCmd(standardOut, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Assert.assertTrue(outputStream.toString().contains("service generation can not be done as the " +
                "openapi definition contain following complex path(s):" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyFrom" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}.{sheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/payroll/v1/workers/{associateoid}/organizational-pay-statements/{payStatementId}/images/" +
                "{imageId}.{imageExtension}" + System.lineSeparator() +
                "/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier='" +
                "{userDefinedIdentifier}')" + System.lineSeparator() +
                "/companies({company_id})/items({item_id})"));
    }

    @Test(description = "client generation for the parameterized path in OAS")
    public void testForComplexPathInClient() {
        Path yamlContract = resourceDir.resolve(Paths.get("complexPath.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString(), "--mode", "client"};
        OpenApiCmd cmd = new OpenApiCmd(standardOut, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Assert.assertTrue(outputStream.toString().contains("WARNING: remote function(s) will be generated for client " +
                "as the given openapi definition contains following complex path(s):" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyFrom" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}.{sheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/payroll/v1/workers/{associateoid}/organizational-pay-statements/{payStatementId}/images/{imageId}." +
                "{imageExtension}" + System.lineSeparator() +
                "/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier=" +
                "'{userDefinedIdentifier}')" + System.lineSeparator() +
                "/companies({company_id})/items({item_id})" + System.lineSeparator() +
                "Client generated successfully."));
    }

    @Test(description = "both client and service generation for the parameterized path in OAS")
    public void testForComplexPathInBothClientAndService() {
        Path yamlContract = resourceDir.resolve(Paths.get("complexPath.yaml"));
        String[] args = {"--input", yamlContract.toString(), "-o", this.tmpDir.toString()};
        OpenApiCmd cmd = new OpenApiCmd(standardOut, tmpDir, false);
        new CommandLine(cmd).parseArgs(args);
        cmd.execute();
        Assert.assertTrue(outputStream.toString().contains("WARNING: remote function(s) will be generated for client" +
                " and the service generation can not proceed due to the openapi definition contain" +
                " following complex path(s):" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}/sheets/{sheetId}:copyFrom" + System.lineSeparator() +
                "/v4/spreadsheets/{spreadsheetId}.{sheetId}/sheets/{sheetId}:copyTo" + System.lineSeparator() +
                "/payroll/v1/workers/{associateoid}/organizational-pay-statements/{payStatementId}/images/" +
                "{imageId}.{imageExtension}" + System.lineSeparator() +
                "/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier=" +
                "'{userDefinedIdentifier}')" + System.lineSeparator() +
                "/companies({company_id})/items({item_id})" + System.lineSeparator() +
                "Following files were created."));
    }

    @Test(description = "Test openapi add sub command")
    public void testAddCmd() throws IOException {
        Path resourceDir = Paths.get(System.getProperty("user.dir")).resolve("build/resources/test");
        Path packagePath = resourceDir.resolve(Paths.get("cmd/bal-task-client"));
        String[] addArgs = {"--input", "petstore.yaml", "-p", packagePath.toString(),
                "--module", "delivery", "--nullable", "--license", "license.txt", "--mode", "client",
                "--client-methods", "resource", "--status-code-binding", "--single-file"};
        Add add = new Add(printStream,  false);
        new CommandLine(add).parseArgs(addArgs);
        add.execute();
        String newLine = System.lineSeparator();
        String tomlContent = Files.readString(packagePath.resolve("Ballerina.toml"));
        String generatedTool = "[[tool.openapi]]" + newLine +
                "id = \"oas_client_petstore\"" + newLine +
                "filePath = \"petstore.yaml\"" + newLine +
                "targetModule = \"delivery\"" + newLine +
                "options.mode = \"client\"" + newLine +
                "options.nullable = true" + newLine +
                "options.clientMethods = \"resource\"" + newLine +
                "options.licensePath = \"license.txt\"" + newLine +
                "options.statusCodeBinding = true" + newLine +
                "options.singleFile = true";
        Assert.assertTrue(tomlContent.contains(generatedTool));
    }

    @Test(description = "Test openapi flatten sub command with default options with the json file")
    public void testFlattenCmdDefaultJson() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected.json"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.json"));
    }

    @Test(description = "Test openapi flatten sub command with allOf schema with only one inline schema")
    public void testFlattenWithAllOf() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_allof_openapi_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/allof_openapi.yaml", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.yaml"));
    }

    private void compareFiles(Path expectedFilePath, Path generatedFilePath) throws IOException {
        if (Files.exists(generatedFilePath)) {
            String expectedOpenAPIContent = "";
            try (Stream<String> expectedOpenAPIContentLines = Files.lines(expectedFilePath)) {
                expectedOpenAPIContent = expectedOpenAPIContentLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Files.deleteIfExists(generatedFilePath);
                Assert.fail(e.getMessage());
            }

            String generatedOpenAPIContent = "";
            try (Stream<String> generatedOpenAPIContentLines =
                         Files.lines(generatedFilePath)) {
                generatedOpenAPIContent = generatedOpenAPIContentLines.collect(Collectors.joining(LINE_SEPARATOR));
            } catch (IOException e) {
                Files.deleteIfExists(generatedFilePath);
                Assert.fail(e.getMessage());
            }

            generatedOpenAPIContent = (generatedOpenAPIContent.trim()).replaceAll("\\s+", "");
            expectedOpenAPIContent = (expectedOpenAPIContent.trim()).replaceAll("\\s+", "");
            Assert.assertEquals(expectedOpenAPIContent, generatedOpenAPIContent);

            Files.deleteIfExists(generatedFilePath);
        } else {
            Assert.fail("Generation failed: " + readOutput(true));
        }
    }

    @Test(description = "Test openapi flatten sub command with default options with the yaml file")
    public void testFlattenCmdDefaultYaml() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.yaml", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.yaml"));
    }

    @Test(description = "Test openapi flatten sub command with the name option")
    public void testFlattenCmdName() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected.json"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-o", tmpDir.toString(), "-n", "flattened"};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened.json"));
    }

    @Test(description = "Test openapi flatten sub command with the json format option")
    public void testFlattenCmdJsonFormat() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected.json"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.yaml", "-o", tmpDir.toString(), "-f", "json"};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.json"));
    }

    @Test(description = "Test openapi flatten sub command with the yaml format option")
    public void testFlattenCmdYamlFormat() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-o", tmpDir.toString(), "-f", "yaml"};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.yaml"));
    }

    @Test(description = "Test openapi flatten sub command with the tags option")
    public void testFlattenCmdTags() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected_pets.json"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-o", tmpDir.toString(), "-t", "pets"};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.json"));
    }

    @Test(description = "Test openapi flatten sub command with the operations option")
    public void testFlattenCmdOperations() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_expected_list_pets.json"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi.json", "-o", tmpDir.toString(), "--operations",
                "listPets"};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.json"));
    }

    @Test(description = "Test openapi flatten sub command with composed schema")
    public void testFlattenCmdWithComposedSchema() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/flatten/flattened_openapi_composed_schema.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/flatten/openapi_composed_schema.yaml", "-o", tmpDir.toString()};
        Flatten flatten = new Flatten();
        new CommandLine(flatten).parseArgs(args);
        flatten.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("flattened_openapi.yaml"));
    }

    @Test(description = "Test openapi align sub command with default options with the json file")
    public void testAlignCmdDefaultJson() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_expected.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.json"));
    }

    @Test(description = "Test openapi align sub command with default options with the yaml file")
    public void testAlignCmdDefaultYaml() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.yaml", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.yaml"));
    }

    @Test(description = "Test openapi align sub command with Swagger 2.0")
    public void testAlignCmdWithSwaggerV2() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_2.0_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_2.0.yaml", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.yaml"));
    }

    @Test(description = "Test openapi align sub command with OpenAPI 3.0.0")
    public void testAlignCmdWithOpenAPIV3_0_0() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_3.0.0_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_3.0.0.yaml", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.yaml"));
    }

    @Test(description = "Test openapi align sub command with the name option")
    public void testAlignCmdName() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_expected.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-o", tmpDir.toString(), "-n", "aligned"};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned.json"));
    }

    @Test(description = "Test openapi align sub command with the json format option")
    public void testAlignCmdJsonFormat() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_expected_1.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.yaml", "-o", tmpDir.toString(), "-f", "json"};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.json"));
    }

    @Test(description = "Test openapi align sub command with the yaml format option")
    public void testAlignCmdYamlFormat() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_expected_1.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-o", tmpDir.toString(), "-f", "yaml"};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.yaml"));
    }

    @Test(description = "Test openapi align sub command with the tags option")
    public void testAlignCmdTags() throws IOException {
        Path expectedFilePath = resourceDir.resolve(
                Paths.get("cmd/align/aligned_openapi_expected_albums.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-o", tmpDir.toString(), "-t", "albums"};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.json"));
    }

    @Test(description = "Test openapi align sub command with the operations option")
    public void testAlignCmdOperations() throws IOException {
        Path expectedFilePath = resourceDir.resolve(
                Paths.get("cmd/align/aligned_openapi_expected_operations.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi.json", "-o", tmpDir.toString(), "--operations",
                "getAlbumById,getAlbums"};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.json"));
    }

    @Test(description = "Test openapi align sub command with composed schema")
    public void testAlignCmdWithComposedSchema() throws IOException {
        Path expectedFilePath = resourceDir.resolve(Paths.get("cmd/align/aligned_openapi_composed_schema.json"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_composed_schema.json", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.json"));
    }

    @Test(description = "Test openapi align sub command with unresolved parameter schema")
    public void testAlignCmdWithUnresolvedParameterSchema() throws IOException {
        // Skipping on Windows due to some order mismatch in generated content
        // Issue: https://github.com/ballerina-platform/ballerina-library/issues/8362
        skipOnWindows();
        Path expectedFilePath = resourceDir.resolve(
                Paths.get("cmd/align/aligned_openapi_unresolved_expected.yaml"));
        String[] args = {"-i", resourceDir + "/cmd/align/openapi_unresolved.yaml", "-o", tmpDir.toString()};
        Align align = new Align();
        new CommandLine(align).parseArgs(args);
        align.execute();
        compareFiles(expectedFilePath, tmpDir.resolve("aligned_ballerina_openapi.yaml"));
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(standardOut);
    }
}
