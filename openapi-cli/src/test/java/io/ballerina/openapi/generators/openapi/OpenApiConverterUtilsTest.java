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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ballerina conversion to OpenApi will test in this class.
 */
public class OpenApiConverterUtilsTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec")
    public void testBasicServices() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec by filtering non existing service",
            expectedExceptions = OpenApiConverterException.class,
            expectedExceptionsMessageRegExp = "No Ballerina services found with name '/abc' to generate an OpenAPI " +
                    "specification. These services are available in ballerina file. \\[/hello, /hello02]")
    public void testBasicServicesWithInvalidServiceName() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.of("/abc"),
                false);
    }

    @Test(description = "Test if invalid 'exampleSetFlag' attribute is coming it the generated spec")
    public void testIfExampleSetFlagContains() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertFalse(Files.readString(this.tempDir.resolve("hello_openapi.yaml")).contains("exampleSetFlag"));
    }

    @Test(description = "Generate OpenAPI spec by filtering service name")
    public void testBasicServicesByFiltering() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir,
                Optional.of("/hello02"), false);

        Assert.assertFalse(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with complex base paths")
    public void testComplexBasePathServices() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("complex_base_path.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello-foo-bar_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02-bar-baz_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePath() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePathWithFilterina() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.of("/"),
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for resource has .")
    public void testPathscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec multiple resource")
    public void testPathscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with multipath including .")
    public void testPathscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with path parameter including .")
    public void testPathscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "path_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with simple query parameter")
    public void testQueryscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario01.yaml");
    }

    // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
    //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType[];
    @Test(description = "Generate OpenAPI spec with optional query parameter")
    public void testQueryscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array type query parameter")
    public void testQueryscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with optional type query parameter")
    public void testQueryscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "query_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with header type parameter")
    public void testHeadscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "header_scenario01.yaml");
    }

    @Test(description = "Response scenario01 without return type")
    public void testResponse01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario01.yaml");
    }

    @Test(description = "Response scenario02 without return type")
    public void testResponse02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario02.yaml");
    }

    @Test(description = "Response scenario03 - return type with Record")
    public void testResponse03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario03.yaml");
    }

    @Test(description = "Response scenario 04 - Response has multiple responses without content type", enabled = false)
    public void testResponse04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario04.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema", enabled = false)
    public void testResponse05() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario05.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema", enabled = false)
    public void testResponse06() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario06.yaml");
    }

    @Test(description = "Response scenario 09 - Error response with a schema")
    public void testResponse09() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario09.yaml");
    }

    @Test(description = "Response scenario 10 - Array type response with a schema")
    public void testResponse10() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario10.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testRecordFieldPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("project_bal/record_payload_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response02.yaml");
    }

    //Listeners
    @Test(description = "Generate OpenAPI spec for single listener")
    public void testListeners01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listner only have port")
    public void testListeners02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listners")
    public void testListeners03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec for ExplicitNewExpressionNode listeners")
    public void testListeners04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listeners")
    public void testListeners05() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario05.yaml");
    }

    @Test(description = "When given ballerina file contain some compilation issue.",
            expectedExceptions = OpenApiConverterException.class,
            expectedExceptionsMessageRegExp = "Given ballerina file has syntax/compilation error.")
    public void testListeners06() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "listener_scenario06.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        deleteDirectory(this.tempDir);
    }

    private void deleteDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            //ignore
        }
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void deleteGeneratedFiles(String filename) {
        try {
            Files.deleteIfExists(this.tempDir.resolve(filename));
            Files.deleteIfExists(this.tempDir.resolve("schema.bal"));
        } catch (IOException e) {
            //Ignore the exception
        }
    }

    private void compareWithGeneratedFile(Path ballerinaFilePath, String yamlFile) throws OpenApiConverterException {
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"), yamlFile);

            OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
            openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty()
                    , false);
            if (Files.exists(this.tempDir.resolve("payloadV_openapi.yaml"))) {
                String generatedYaml = getStringFromGivenBalFile(this.tempDir, "payloadV_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("payloadV_openapi.yaml");
            deleteDirectory(this.tempDir);
            System.gc();
        }
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
