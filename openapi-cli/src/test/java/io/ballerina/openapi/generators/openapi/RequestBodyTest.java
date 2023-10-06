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
import io.ballerina.openapi.converter.OpenApiConverterException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test related to the RequestBody Mapping in Ballerina to OAS.
 */
public class RequestBodyTest {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec with json payload")
    public void testJsonPayLoad() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/json_payload_service.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "json_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec with xml payload")
    public void testXmlPayLoad()  {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/xml_payload_service.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null,
                false);
        Assert.assertTrue(Files.exists(this.tempDir.resolve("payloadXml_openapi.yaml")));
    }

    @Test(description = "Generate OpenAPI spec with mulitple payload")
    public void testMultiplePayLoad() throws IOException, OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/multiple_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "multiple_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec for build project")
    public void testMIMERecordFiledPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/mime_with_recordpayload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "mime_with_record_payload.yaml");
    }

    @Test(description = "Generate OpenAPI spec with record payload")
    public void testRecordPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/record_payload_service.bal");
        String yamlFile = "record_payload.yaml";
        compareWithGeneratedFile(ballerinaFilePath, yamlFile);
    }

    @Test(description = "Generate OpenAPI spec with nested record payload")
    public void testNestedRecordPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/nestedRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with nested payload")
    public void testNested2RecordPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/nested2Record_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_2record.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/arrayRecord_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "nested_array.yaml");
    }

    @Test(description = "Generate OpenAPI spec with array field payload")
    public void testArrayNestedRecordFiledPayLoad() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/arrayRecordfield_payload_service.bal");
        compareWithGeneratedFile(ballerinaFilePath, "record_field_array.yaml");
    }

    @Test(description = "RequestBody without mediaType")
    public void testRequestBodyscenario01() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario01.yaml");
    }

    @Test(description = "RequestBody without mediaType, record type has payload")
    public void testRequestBodyscenario02() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario02.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type has payload")
    public void testRequestBodyscenario03() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario03.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with array field has payload")
    public void testRequestBodyscenario04() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario04.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario05() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario05.yaml");
    }

    @Test(description = "RequestBody without mediaType, nested record type with nested array field has payload")
    public void testRequestBodyscenario06() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario06.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario07() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario07.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario07.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario08() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario08.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario08.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has xml type")
    public void testRequestBodyscenario09() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario09.yaml");
    }

    // Ballerina didn't support inline payload
    @Test(description = "RequestBody without mediaType, payload has inline record type", enabled = false)
    public void testRequestBodyscenario10() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario10.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has has record type")
    public void testRequestBodyscenario11() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/with_payload_annotation.bal");
        compareWithGeneratedFile(ballerinaFilePath, "with_payload_annotation.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has has record type")
    public void testRequestBodyscenario12() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_hv_array_record.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_hv_array_record.yaml");
    }

    @Test(description = "RequestBody without mediaType, payload has has record type")
    public void testRequestBodyscenario13() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_hv_multi_array_record.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_hv_multi_array_record.yaml");
    }

    @Test(description = "Generate OpenAPI spec for multiple records")
    public void testMultipleRecords() throws OpenApiConverterException {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario11.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario11.yaml");
    }

    @Test(description = "Generate OpenAPI spec for request body with customized media type")
    public void testServiceConfig() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario12.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario12.yaml");
    }

    @Test(description = "Generate OpenAPI spec for request body with http:Request req")
    public void testRequestBodyWithDefault() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario13.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario13.yaml");
    }

    @Test(description = "Generate OpenAPI spec for GET method having a request body")
    public void testRequestBodyWithGETMethod() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario14.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertFalse(openApiConverterUtils.getErrors().isEmpty());
        Assert.assertEquals(openApiConverterUtils.getErrors().get(0).getMessage(), "Generated OpenAPI" +
                " definition does not contain `http:Request` body information of the `GET` method, as it's not " +
                "supported by the OpenAPI specification.");
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario14.yaml");
    }

    @Test(description = "Generate OpenAPI spec for request body having map<string> type")
    public void testRequestBodyWithMapString() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/rb_scenario15.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "rb_scenario15.yaml");
    }
    @Test(description = "Generate OpenAPI spec with json file")
    public void testNestedRecordPayLoadJson() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/nestedRecord_payload_service.bal");
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen/json"),
                    "nestedRecord.json");
            OASContractGenerator openApiConverterUtils = new OASContractGenerator();
            openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                    , true);
            if (Files.exists(this.tempDir.resolve("payloadV_openapi.json"))) {
                String generatedYaml = getStringFromGivenBalFile(this.tempDir, "payloadV_openapi.json");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Json was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.json");
        }
    }

    @Test(description = "When the service has config without mediaType attribute")
    public void testForServiceConfigOnlyWithCors() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/service_config_with_cors.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "service_config_with_cors.yaml");
    }

    @Test(description = "Generate OpenAPI spec for request body with union type")
    public void testForUnionTypeRequestBody() {
        Path ballerinaFilePath = RES_DIR.resolve("request_body/union_type.bal");
        compareWithGeneratedFile(ballerinaFilePath, "union_type.yaml");
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
        } catch (IOException ignored) {
        }
    }

    private void compareWithGeneratedFile(Path ballerinaFilePath, String yamlFile) {
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen/request_body"),
                    yamlFile);

            OASContractGenerator openApiConverterUtils = new OASContractGenerator();
            openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
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
