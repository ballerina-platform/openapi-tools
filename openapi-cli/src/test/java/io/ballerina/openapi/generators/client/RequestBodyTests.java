/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils
        .compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * All the tests related to the functionSignatureNode in
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator} util when have diffrent
 * scenarios in Request Body.
 */
public class RequestBodyTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for generate request body payload when operation has request body")
    public void testForRequestBody() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_basic_scenarios.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_basic_scenarios.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generate request body payload when operation has request body with AllOf scenarios")
    public void testForRequestBodyWithAllOf() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_allOf_scenarios.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_allOf_scenarios.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generate request body payload when operation has request body OneOf scenarios")
    public void testForRequestBodyWithOneOf() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_oneOf_scenarios.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_oneOf_scenarios.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generate request body payload with array schema")
    public void testForRequestBodyWithArraySchema() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_array.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_array.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generate request body payload with empty array schema")
    public void testForRequestBodyWithEmptyArraySchema() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_empty_array.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_empty_array.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test client generation for unsupported request body media type")
    public void testRequestBodyWithUnsupportedMediaType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/unsupported_request_body.bal");
        Path definitionPath = RES_DIR.resolve("swagger/unsupported_request_body.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test requestBody validation in GET/DELETE/HEAD operations",
            expectedExceptions = BallerinaOpenApiException.class, expectedExceptionsMessageRegExp =
                                    ".*GET operation cannot have a requestBody.*", enabled = false)
    public void testGetOrDeleteOrHeadContainRequestBody() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/request_body_in_get_delete_head.yaml");
        GeneratorUtils.normalizeOpenAPI(definitionPath, true);
    }

    @Test(description = "Test for generating request body when operation has form url encoded media type")
    public void testRequestBodyWithURLEncodedType() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/url_encoded_payload.bal");
        Path definitionPath = RES_DIR.resolve("utils/swagger/url_encoded.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has binary octet-stream media type")
    public void testRequestBodyWithBinaryOctetStreamMediaType()
            throws IOException, BallerinaOpenApiException, ClientException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/binary_format_octet_stream_payload.bal");
        Path definitionPath = RES_DIR.resolve("swagger/binary_format_octet_stream_payload.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has byte octet-stream media type")
    public void testRequestBodyWithByteOctetStreamMediaType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/byte_format_octet_stream_payload.bal");
        Path definitionPath = RES_DIR.resolve("swagger/byte_format_octet_stream_payload.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has */* media type")
    public void testRequestBodyWithWildCardeMediaType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/any_types_payload.bal");
        Path definitionPath = RES_DIR.resolve("swagger/any_types_payload.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has vendor specific media type")
    public void testRequestBodyWithVendorSpecificMimeType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/vendor_specific_payload.bal");
        Path definitionPath = RES_DIR.resolve("swagger/vendor_specific_payload.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has vendor specific media type " +
            "which is a subtype of JSON")
    public void testRequestBodyWithVendorSpecificMimeTypeWithJSON() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/vendor_specific_with_subtype.bal");
        Path definitionPath = RES_DIR.resolve("swagger/vendor_specific_with_subtype.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has unsupported text/xx media type")
    public void testRequestBodyWithUnsupportedTextMediaType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/unsupported_text_media_type.bal");
        Path definitionPath = RES_DIR.resolve("swagger/unsupported_text_media_type.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }


    @Test(description = "Test for generating request body when operation has multipart form-data media type")
    public void testRequestBodyWithMultipartMediaType() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/multipart_formdata.bal");
        Path definitionPath = RES_DIR.resolve("utils/swagger/multipart_formdata.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has multipart form-data media type " +
            "with no schema")
    public void testRequestBodyWithMultipartMediaTypeAndNoSchema()
            throws IOException, BallerinaOpenApiException, ClientException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/multipart_formdata_empty.bal");
        Path definitionPath = RES_DIR.resolve("swagger/mutipart_formdata_empty.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when operation has multipart form-data media type " +
            "with invalid schema")
    public void testRequestBodyWithMultipartMediaTypeInvalidSchema()
            throws IOException, BallerinaOpenApiException, ClientException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/multipart_binary.bal");
        Path definitionPath = RES_DIR.resolve("swagger/multipart_binary.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body when schema is empty")
    public void testRequestBodyWithoutSchema() throws IOException, BallerinaOpenApiException,
            ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_without_schema.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_without_schema.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body with reference")
    public void testRequestBodyWithReference() throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_with_ref.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_with_ref.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body with octet-stream media type")
    public void testWithOctetStreamInRequestBody()
            throws IOException, BallerinaOpenApiException, ClientException, FormatterException {
        Path expectedPath = RES_DIR.resolve("ballerina/octet_stream_request_payload.bal");
        Path definitionPath = RES_DIR.resolve("swagger/octet_stream_request_payload.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for generating request body with object schema content without properties")
    public void testWithRBContentHasObjectSchemaWithoutProperties()
            throws IOException, BallerinaOpenApiException, ClientException {
        Path expectedPath = RES_DIR.resolve("ballerina/request_body_has_object_content_without_property.bal");
        Path definitionPath = RES_DIR.resolve("swagger/request_body_has_object_content_without_property.yaml");
        BallerinaClientGenerator ballerinaClientGenerator = getBallerinaClientGenerator(definitionPath);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
        } catch (IOException ignored) {
        }
    }

    private BallerinaClientGenerator getBallerinaClientGenerator(Path definitionPath) throws IOException,
            BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        return ballerinaClientGenerator;
    }
}
