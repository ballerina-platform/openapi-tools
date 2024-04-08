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

import io.ballerina.cli.launcher.BLauncherException;
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
        Assert.assertTrue(output.contains("NAME\n" +
                "       ballerina-openapi - Generate a Ballerina service"));
    }

    @Test(description = "Test openapi command with help flag")
    public void testOpenAPICmdHelpWithBalTools() throws IOException {
        String[] args = {"help", "openapi"};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n" +
                "       ballerina-openapi - Generate a Ballerina service"));
    }

    @Test(description = "Test openapi command without help flag")
    public void testOpenAPICmdHelpWithoutFlag() throws IOException {
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream, tmpDir, false);
        new CommandLine(openApiCommand);
        openApiCommand.execute();
        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n" +
                "       ballerina-openapi - Generate a Ballerina service"));
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
        try {
            cmd.execute();
        } catch (BLauncherException e) {
        }
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
                "/payroll/v1/workers/{associateoid}/organizational-pay-statements/{payStatementId}/images/" +
                "{imageId}.{imageExtension}" + System.lineSeparator() +
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
                "--client-methods", "resource"};
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
                "options.licensePath = \"license.txt\"" + newLine;
        Assert.assertTrue(tomlContent.contains(generatedTool));
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(standardOut);
    }
}
