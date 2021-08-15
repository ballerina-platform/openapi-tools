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

import io.ballerina.openapi.converter.OpenApiConverterException;
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
        OpenApiConverter openApiConverterUtils = new OpenApiConverter();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
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
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, "/abc",
                false);
    }

    @Test(description = "Test if invalid 'exampleSetFlag' attribute is coming it the generated spec")
    public void testIfExampleSetFlagContains() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertFalse(Files.readString(this.tempDir.resolve("hello_openapi.yaml")).contains("exampleSetFlag"));
    }

    @Test(description = "Generate OpenAPI spec by filtering service name")
    public void testBasicServicesByFiltering() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir,
                "/hello02", false);

        Assert.assertFalse(Files.exists(this.tempDir.resolve("hello_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with complex base paths")
    public void testComplexBasePathServices() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("complex_base_path.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello_foo_bar_openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02_bar_baz_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePath() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePathWithFilterina() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, "/",
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for resource has .")
    public void testPathscenario01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "path_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec multiple resource")
    public void testPathscenario02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario02.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "path_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with multipath including .")
    public void testPathscenario03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario03.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "path_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with path parameter including .")
    public void testPathscenario04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_scenario04.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "path_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with simple query parameter")
    public void testQueryscenario01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "query_scenario01.yaml");
    }

    // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
    //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType[];
    @Test(description = "Generate OpenAPI spec with optional query parameter")
    public void testQueryscenario02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario02.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "query_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array type query parameter")
    public void testQueryscenario03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario03.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "query_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec with optional type query parameter")
    public void testQueryscenario04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("query_scenario04.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "query_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec with header type parameter")
    public void testHeadscenario01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("header_scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "header_scenario01.yaml");
    }



    @Test(description = "Generate OpenAPI spec for build project")
    public void testRecordFieldPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("project_bal/record_payload_service.bal");
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testForResponse02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("scenario02.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "response02.yaml");
    }

    //Listeners
    @Test(description = "Generate OpenAPI spec for single listener")
    public void testListeners01() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario01.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario01.yaml");
    }

    @Test(description = "Generate OpenAPI spec for listner only have port")
    public void testListeners02() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario02.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario02.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listners")
    public void testListeners03() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario03.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario03.yaml");
    }

    @Test(description = "Generate OpenAPI spec for ExplicitNewExpressionNode listeners")
    public void testListeners04() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario04.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario04.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple listeners")
    public void testListeners05() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario05.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario05.yaml");
    }

    @Test(description = "When given ballerina file contain some compilation issue.",
            expectedExceptions = OpenApiConverterException.class,
            expectedExceptionsMessageRegExp = "Given ballerina file has syntax/compilation error.")
    public void testListeners06() throws OpenApiConverterException, IOException {
        Path ballerinaFilePath = RES_DIR.resolve("listener_scenario06.bal");
        new TestUtils().compareWithGeneratedFile(ballerinaFilePath, "listener_scenario06.yaml");
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
