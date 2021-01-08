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

package org.ballerinalang.ballerina;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;

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
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello-openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec by filtering non existing service",
          expectedExceptions = OpenApiConverterException.class,
          expectedExceptionsMessageRegExp = "No Ballerina services found with name '/abc' to generate an OpenAPI " +
                                            "specification.")
    public void testBasicServicesWithInvalidServiceName() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.of("/abc"));
    }
    
    @Test(description = "Test if invalid 'exampleSetFlag' attribute is coming it the generated spec")
    public void testIfExampleSetFlagContains() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello-openapi.yaml")));
        Assert.assertFalse(Files.readString(this.tempDir.resolve("hello-openapi.yaml")).contains("exampleSetFlag"));
    }

    @Test(description = "Generate OpenAPI spec by filtering service name")
    public void testBasicServicesByFiltering() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("basic_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir,
                Optional.of("/hello02"));

        Assert.assertFalse(Files.exists(this.tempDir.resolve("hello-openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with complex base paths")
    public void testComplexBasePathServices() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("complex_base_path.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());

        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello-foo-bar-openapi.yaml")));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("hello02-bar-baz-openapi.yaml")));
    }
    
    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePath() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with no base path")
    public void testServicesWithNoBasePathWithFilterina() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("no_base_path_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.of("/"));
        Assert.assertTrue(Files.exists(this.tempDir.resolve("no_base_path_service-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with json payload")
    public void testJsonPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("json_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }
    @Test(description = "Generate OpenAPI spec with xml payload")
    public void testXmlPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("xml_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }
    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testMultiplePayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testRecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("record_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testNestedRecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nestedRecord_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testNested2RecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nested2Record_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayRecord_payload_service.bal");
        OpenApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty());
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadV-openapi.yaml")));
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
}
