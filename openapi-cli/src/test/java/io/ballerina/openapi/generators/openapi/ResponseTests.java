/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.common.TestUtils.getStringFromGivenBalFile;
import static io.ballerina.openapi.generators.openapi.TestUtils.compareWithGeneratedFile;

/**
 * This test class for the covering the unit tests for return type scenarios.
 */
public class ResponseTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Response scenario01 without return type")
    public void testResponse01() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario01.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario01.yaml");
    }

    @Test(description = "Response scenario02 without return type")
    public void testResponse02() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario02.yaml");
    }

    @Test(description = "Response scenario03 - return type with Record")
    public void testResponse03() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario03.yaml");
    }

    @Test(description = "Response scenario 04 - Response has multiple responses without content type")
    public void testResponse04() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario04.yaml");
    }

    @Test(description = "Response scenario 05 - Error response with a schema", enabled = false)
    public void testResponse05() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("rs_scenario05.bal");
        compareWithGeneratedFile(ballerinaFilePath, "rs_scenario05.yaml");
    }

    @Test(description = "Response scenario 06 - Error response with a schema", enabled = false)
    public void testResponse06() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario06.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario06.yaml");
    }

    @Test(description = "Response scenario 09 - return has record, error, basic types")
    public void testResponse09() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario09.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario09.yaml");
    }

    @Test(description = "Response scenario 10 - Array type response with a schema")
    public void testResponse10() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario10.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario10.yaml");
    }

    @Test(description = "When the return type is record with typeInclusion field of http code ")
    public void testTypeInclusion() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/typeInclusion_01.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/typeInclusion_01.yaml");
    }

    @Test(description = "When the return type is string")
    public void testStringReturn() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario11.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario11.yaml");
    }

    @Test(description = "When the return type is inline record")
    public void testInlineRecordReturn() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario12.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario12.yaml");
    }

    @Test(description = "When the return type is inline record")
    public void testInlineRecordHasHttpTypeInclusion() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario13.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario13.yaml");
    }

    @Test(description = "When the return type is inline record", enabled = false)
    public void testInlineRecordHasReference() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario14.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario14.yaml");
    }

    @Test(description = "When the return type is inline record with non http typeInclusion fields", enabled = false)
    public void testInlineRecordHasTypeInclusionReference() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario16.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario16.yaml");
    }

    @Test(description = "When the return type is array record ")
    public void testArrayRecord() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/array_record.bal");
        //Compare generated yaml file with expected yaml content
        compareWithGeneratedFile(ballerinaFilePath, "response/array_record.yaml");
    }

    @Test(description = "Generate OpenAPI spec for service configuration annotation in resource")
    public void testWithAnnotation() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario17.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario17.yaml");
    }

    @Test(description = "Generate OpenAPI spec for resource function which has service configuration annotation " +
            "including details with vendor specific media type")
    public void testWithAnnotationForAllType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario18.bal");
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario18.yaml");
    }

    @Test(description = "Test for return type has `http:Response`.")
    public void testHttpResponse() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario19.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario19.yaml");
    }

    @Test(description = "Test for return type has `http:Response|error?`.")
    public void testUnionHttpResponse() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_scenario20.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , true);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_scenario20.yaml");
    }

    @Test(description = "Test for return type having form value content.")
    public void urlEncodeResponse() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_with_url_encode.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_with_url_encode.yaml");
    }

    @Test(description = "When the response has payload annotation")
    public void responseHasPayload() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_with_payload.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_with_payload.yaml");
    }

    @Test(description = "When the response has payload annotation and service config annotation")
    public void responseHasPayloadWithServiceConfig() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_with_payload_service_config.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/rs_with_payload_service_config.yaml");
    }

    @Test(description = "When the response has payload annotation with custom media type and " +
            "service config annotation")
    public void responseWithCustomMediaType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/rs_with_service_and_payload_annotation.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath,
                "response/rs_with_service_and_payload_annotation.yaml");
    }

    @Test(description = "When the response has all the status codes")
    public void testForAllReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/response_code.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/response_code.yaml");
    }

    @Test(description = "When the response has return record without no body field")
    public void testForNoContentReturnCode() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/without_body_field.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/no_body_field.yaml");
    }

    @Test(description = "When the response has float return type")
    public void testResponseWithFloatReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/float.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/float.yaml");
    }

    @Test(description = "When the response has decimal return type")
    public void testResponseWithDecimalReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/decimal.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/decimal.yaml");
    }

    @Test(description = "When the response has byte[] return type")
    public void testResponseWithByteArray() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/byte.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/byte.yaml");
    }

    @Test
    public void testWithMultipleReturnPayloadSameStatusCode() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/same_status_code.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/same_status_code.yaml");
    }

    @Test(description = "Test scenarios where return type is a SimpleNameReference")
    public void testResponseWithSimpleNameReferenceReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/simple_name_ref.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/simple_name_ref.yaml");
    }

    @Test(description = "Test scenarios where return type is a SimpleNameReference with readonly")
    public void testResponseWithReadOnlySimpleNameReferenceReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/readonly.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/readonly.yaml");
    }

    @Test(description = "Test scenarios where return type is a deprecated status code 'NetworkAuthorizationRequired'")
    public void testUsingDeprecatedStatusCode() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/deprecated_status_code.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());

        compareWithGeneratedFile(ballerinaFilePath, "response/deprecated_status_code.yaml");
    }

    @Test(description = "When the service has config without mediaType attribute")
    public void testResponseHasServiceConfigWithCors() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/service_config_with_cors.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/service_config_with_cors.yaml");
    }

    @Test(description = "When the service has `error` and `InternalServerError`")
    public void testForResponseHasSameErrorStatusCode() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/service_with_error_and_internal_server_error.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/service_with_error_and_internal_server_error.yaml");
    }

    @Test(description = "When the service has same error response code")
    public void testForResponseHasSameStatusCode() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/service_with_same_status_codes.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/service_with_same_status_code.yaml");
    }

    @Test(description = "When the service has config without mediaType attribute, and with slash path")
    public void testResponseHasServiceConfigWithCorsWithSlash() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/service_config_with_cors_with_slash.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"),
                "response/service_config_with_cors_with_slash.yaml");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);
        String generatedYaml = getStringFromGivenBalFile(tempDir,
                "service_config_with_cors_with_slash_openapi.yaml");
        generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
        expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
    }

    @Test(description = "When the service has config without mediaType attribute, and without base path")
    public void testResponseHasServiceConfigWithCorsWithoutBasePath() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/service_config_with_cors_without_base_path.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"),
                "response/service_config_with_cors_without_base_path.yaml");
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);
        String generatedYaml = getStringFromGivenBalFile(tempDir,
                "service_config_with_cors_without_base_path_openapi.yaml");
        generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
        expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
    }

    @Test(description = "When the resource has nil type return")
    public void testNilReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/nil_return_type.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/nil_return_type.yaml");
    }

    @Test(description = "When the resource has nil union type return")
    public void testNilUnionReturnType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("response/nil_union_return_type.bal");
        OASContractGenerator openApiConverterUtils = new OASContractGenerator();
        openApiConverterUtils.generateOAS3DefinitionsAllService(ballerinaFilePath, this.tempDir, null
                , false);
        Assert.assertTrue(openApiConverterUtils.getErrors().isEmpty());
        compareWithGeneratedFile(ballerinaFilePath, "response/nil_union_return_type.yaml");
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
