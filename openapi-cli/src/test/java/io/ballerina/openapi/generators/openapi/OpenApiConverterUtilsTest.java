/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;
import static io.ballerina.openapi.generators.common.TestUtils.getStringFromGivenBalFile;
import static io.ballerina.openapi.generators.openapi.TestUtils.compareWithGeneratedFile;
import static io.ballerina.openapi.generators.openapi.TestUtils.deleteDirectory;
import static io.ballerina.openapi.generators.openapi.TestUtils.deleteGeneratedFiles;

/**
 * Ballerina conversion to OpenApi will test in this class.
 */
public class OpenApiConverterUtilsTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test(description = "Generate OpenAPI spec")
    public void testBasicServices() {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec by filtering non existing service")
    public void testBasicServicesWithInvalidServiceName() {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, "/abc",
                false);
        Assert.assertTrue(outputStreamCaptor.toString().contains(
                "ERROR [:(1:1,1:1)] No Ballerina HTTP services found with name /abc to generate" +
                        " an OpenAPI specification. These services are available in ballerina file." +
                        " [/hello, /hello02]"));
    }

    @Test(description = "Test if invalid 'exampleSetFlag' attribute is coming it the generated spec")
    public void testIfExampleSetFlagContains() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertFalse(Files.readString(this.tempDir.resolve("hello_openapi.yaml")).contains("exampleSetFlag"));
    }

    @Test(description = "Generate OpenAPI spec by filtering service name")
    public void testBasicServicesByFiltering() {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir,
                "/hello02", false);

        Assert.assertFalse(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with complex base paths")
    public void testComplexBasePathServices() {
        Path ballerinaFilePath = RES_DIR.resolve("complex_base_path.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_foo_bar_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_bar_baz_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePath() {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePathWithFilterina() {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, "/",
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for resource has .")
    public void testPathscenario01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec multiple resource")
    public void testPathscenario02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with multipath including .")
    public void testPathscenario03() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with path parameter including .")
    public void testPathscenario04() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testRecordFieldPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("project_bal/record_payload_service.bal");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response02.yaml");
    }

    @Test(description = "Generate OpenAPI spec for given ballerina file has only compiler warning")
    public void testForCompilerWarning() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("compiler_warning.bal");
        compareWithGeneratedFile(ballerinaFilePath, "compiler_warning.yaml");
    }

    @Test(description = "Test for non http services")
    public void testForNonHttpServices() {
        Path ballerinaFilePath = RES_DIR.resolve("non_service.bal");
        new OASContractGenerator().generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null
                , false);
        Assert.assertTrue(!Files.exists(tempDir.resolve("query_openapi.yaml")));
    }

    @Test(description = "Given ballerina service has escape character")
    public void testForRemovingEscapeIdentifier() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("escape_identifier.bal");
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        try {
            OASContractGenerator openApiConverter = new OASContractGenerator();
            openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null
                    , false);
            if (Files.exists(tempDir.resolve("v1_abc_hello_openapi.yaml"))) {
                String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"),
                        "escape_identifier.yaml");
                String generatedYaml = getStringFromGivenBalFile(tempDir, "v1_abc_hello_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
            if (Files.exists(tempDir.resolve("limit_openapi.yaml"))) {
                String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"),
                        "escape_identifier_02.yaml");
                String generatedYaml = getStringFromGivenBalFile(tempDir, "limit_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("v1_abc-hello_openapi.yaml", tempDir);
            deleteDirectory(tempDir);
            System.gc();
        }
    }

    @Test
    public void testUnescapeIdentifiers() {
        Assert.assertEquals(unescapeIdentifier("'limit"), "limit");
        Assert.assertEquals(unescapeIdentifier("x\\-client"), "x-client");
        Assert.assertEquals(unescapeIdentifier("/'limit"), "/limit");
        Assert.assertEquals(unescapeIdentifier("/'limit/x\\-cl"), "/limit/x-cl");
        Assert.assertEquals(unescapeIdentifier("'พิมพ์ชื่อ"), "พิมพ์ชื่อ");
    }

    @Test
    public void testDecodeIdentifier() {
        Assert.assertEquals(unescapeIdentifier("ชื่\\u{E2D}"), "ชื่อ");
    }

    @AfterMethod
    public void cleanUp() {
        deleteDirectory(this.tempDir);
    }

    @AfterTest
    public void clean() {

        System.setErr(null);
        System.setOut(standardOut);
    }
}
