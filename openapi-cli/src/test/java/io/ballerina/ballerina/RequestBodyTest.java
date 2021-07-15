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

package io.ballerina.ballerina;

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
 * Test related to the RequestBody Mapping in Ballerina to OAS.
 */
public class RequestBodyTest {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi/request_body").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec with json payload")
    public void testJsonPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("json_payload_service.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "json_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec with xml payload")
    public void testXmlPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("xml_payload_service.bal");
        OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty(),
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadXml_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testMultiplePayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "multiple_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec with record payload")
    public void testRecordPayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("record_payload_service.bal");
        String yamlFile = "record_payload.yaml";
        compareWithGeneratedFile(ballerinaFilePath, yamlFile);
    }

    @Test(description = "Generate OpenAPI spec with nested record payload")
    public void testNestedRecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nestedRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with nested payload")
    public void testNested2RecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("nested2Record_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_2record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_array.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordFiledPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("arrayRecordfield_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "record_field_array.yaml");
    }

    @Test(description = "RequestBody without mediaType")
    public void testRequestBodyscenario01() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario01.yaml");
    }

    @Test(description = "RequestBody without mediaType, record type has payload")
    public void testRequestBodyscenario02() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario02.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type has payload")
    public void testRequestBodyscenario03() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario03.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with array field has payload")
    public void testRequestBodyscenario04() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario04.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario05() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario05.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario06() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario06.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario07() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario07.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario07.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario08() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario08.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario08.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario09() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario09.yaml");
    }

    // Need to handle further implementation
    @Test(description = "RequestBody without mediaType, payload has inline record type", enabled = false)
    public void testRequestBodyscenario10() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("rb_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario10.yaml");
    }
    @Test(description = "RequestBody without mediaType, payload has has record type")
    public void testRequestBodyscenario11() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("with_payload_annotation.bal");
        compareWithGeneratedFile(ballerinaFilePath, "with_payload_annotation.yaml");
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
//            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen/request_body"),
//                    yamlFile);

            OpenApiConverterUtils openApiConverterUtils = new OpenApiConverterUtils();
            openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, Optional.empty()
                    , false);
            if (Files.exists(this.tempDir.resolve("payloadV_openapi.yaml"))) {
                String generatedYaml = getStringFromGivenBalFile(this.tempDir, "payloadV_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
//                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
//                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
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
