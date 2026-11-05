/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.client.FunctionBodyGeneratorImp;
import io.ballerina.openapi.core.generators.client.MimeFactory;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.mime.CustomType;
import io.ballerina.openapi.core.generators.client.mime.JsonType;
import io.ballerina.openapi.core.generators.client.mime.MimeType;
import io.ballerina.openapi.core.generators.client.mime.MultipartFormData;
import io.ballerina.openapi.core.generators.client.mime.OctetStreamType;
import io.ballerina.openapi.core.generators.client.mime.UrlEncodedType;
import io.ballerina.openapi.core.generators.client.mime.XmlType;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADERS;

/**
 * Tests for handling media types with parameters such as `application/json; charset=utf-8`.
 */
public class MediaTypeParameterTests {

    @Test(description = "Media type parameters are removed from the media type",
            dataProvider = "mediaTypesWithParameters")
    public void testGetMediaTypeWithoutParameters(String mediaType, String expected) {
        Assert.assertEquals(GeneratorUtils.getMediaTypeWithoutParameters(mediaType), expected);
    }

    @DataProvider(name = "mediaTypesWithParameters")
    public Object[][] mediaTypesWithParameters() {
        return new Object[][]{
                {"application/json", "application/json"},
                {"application/json; charset=utf-8", "application/json"},
                {"application/json;charset=utf-8", "application/json"},
                {"application/json ; charset=utf-8", "application/json"},
                {"multipart/form-data; boundary=item-boundary", "multipart/form-data"},
                {"text/plain; charset=utf-8; format=flowed", "text/plain"},
                {"application/json ;", "application/json"},
                {";", ""}
        };
    }

    @Test(description = "Response media types with parameters are mapped to the media type without parameters",
            dataProvider = "responseMediaTypes")
    public void testSelectMediaTypeWithParameters(String mediaType, String expected) {
        Assert.assertEquals(GeneratorUtils.selectMediaType(mediaType), expected);
    }

    @DataProvider(name = "responseMediaTypes")
    public Object[][] responseMediaTypes() {
        return new Object[][]{
                {"application/json; charset=utf-8", "application/json"},
                {"application/json ; charset=utf-8", "application/json"},
                {"application/vnd.api+json;charset=utf-8", "application/json"},
                {"application/xml ; charset=utf-8", "application/xml"}
        };
    }

    @Test(description = "The media type is escaped when it is written as a string literal in the generated client")
    public void testPayloadMediaTypeIsEscaped() {
        List<StatementNode> statements = new ArrayList<>();
        new JsonType(new ArrayList<>()).setPayload(statements,
                Map.entry("application/json; profile=\"urn:example:item\"", new MediaType()));
        Assert.assertEquals(statements.get(statements.size() - 1).toSourceCode().trim(),
                "request.setPayload(jsonBody, \"application/json; profile=\\\"urn:example:item\\\"\");");
    }

    @Test(description = "Client is generated for media types with quoted parameters and spaces before parameters")
    public void testClientGenerationWithQuotedMediaTypeParameter() throws IOException, BallerinaOpenApiException,
            ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(Path.of("src/test/resources/generators/client/swagger/" +
                "request_body_media_type_with_quoted_parameter.yaml").toAbsolutePath(), true, false);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig oasClientConfig = new OASClientConfig.Builder()
                .withFilters(new Filter(new ArrayList<>(), new ArrayList<>()))
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        String client = new BallerinaClientGenerator(oasClientConfig).generateSyntaxTree().toSourceCode()
                .replaceAll("\\s+", "");
        Assert.assertTrue(client.contains("createItem(Itempayload,map<string|string[]>headers={})returnsItem|error"),
                client);
        Assert.assertTrue(client.contains(
                "request.setPayload(jsonBody,\"application/json;profile=\\\"urn:example:item\\\"\");"), client);
    }

    @Test(description = "Media types with parameters are recognized as supported request media types",
            dataProvider = "supportedMediaTypesWithParameters")
    public void testSupportedMediaTypeWithParameters(String mediaType, String ballerinaType) {
        Assert.assertTrue(GeneratorUtils.isSupportedMediaType(
                Map.entry(mediaType, new MediaType().schema(new ObjectSchema()))));
        Assert.assertEquals(GeneratorUtils.getBallerinaMediaType(mediaType, true), ballerinaType);
    }

    @DataProvider(name = "supportedMediaTypesWithParameters")
    public Object[][] supportedMediaTypesWithParameters() {
        return new Object[][]{
                {"application/json; charset=utf-8", "json"},
                {"application/json ; charset=utf-8", "json"},
                {"application/xml ; charset=utf-8", "xml"},
                {"text/plain; charset=utf-8", "string"},
                {"application/octet-stream; qs=0.9", "byte[]"}
        };
    }

    @Test(description = "Request body mime type is selected for media types with parameters",
            dataProvider = "requestMediaTypes")
    public void testMimeTypeForMediaTypeWithParameters(String mediaType, Class<? extends MimeType> expectedType)
            throws BallerinaOpenApiException {
        MimeType mimeType = new MimeFactory().getMimeType(
                Map.entry(mediaType, new MediaType().schema(new ObjectSchema())), new BallerinaUtilGenerator(),
                new ArrayList<>());
        Assert.assertEquals(mimeType.getClass(), expectedType);
    }

    @DataProvider(name = "requestMediaTypes")
    public Object[][] requestMediaTypes() {
        return new Object[][]{
                {"application/json; charset=utf-8", JsonType.class},
                {"application/vnd.api+json;charset=utf-8", JsonType.class},
                {"application/xml; charset=utf-8", XmlType.class},
                {"application/atom+xml; charset=utf-8", XmlType.class},
                {"application/x-www-form-urlencoded; charset=utf-8", UrlEncodedType.class},
                {"application/octet-stream; qs=0.9", OctetStreamType.class},
                {"multipart/form-data; boundary=item-boundary", MultipartFormData.class},
                {"text/plain; charset=utf-8", CustomType.class}
        };
    }

    @Test(description = "Unsupported request body media types are still rejected",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Unsupported media type 'audio/mpeg; rate=44100' is given in the " +
                    "request body")
    public void testMimeTypeForUnsupportedMediaType() throws BallerinaOpenApiException {
        new MimeFactory().getMimeType(
                Map.entry("audio/mpeg; rate=44100", new MediaType().schema(new ObjectSchema())),
                new BallerinaUtilGenerator(), new ArrayList<>());
    }

    @Test(description = "A diagnostic is reported when the function body of an operation cannot be generated")
    public void testDiagnosticWhenFunctionBodyGenerationFails() {
        Operation operation = new Operation()
                .operationId("createItem")
                .requestBody(new RequestBody().$ref("./external.yaml#/Item"))
                .responses(new ApiResponses().addApiResponse("200", new ApiResponse().description("Ok")));
        OpenAPI openAPI = new OpenAPI()
                .paths(new Paths().addPathItem("/items", new PathItem().post(operation)))
                .components(new Components());
        TypeHandler.createInstance(openAPI, false);
        FunctionBodyGeneratorImp functionBodyGenerator = new FunctionBodyGeneratorImp("/items",
                Map.entry(PathItem.HttpMethod.POST, operation), openAPI, new AuthConfigGeneratorImp(false, false),
                new BallerinaUtilGenerator(), new ArrayList<>(), false, false, false, HEADERS);

        Optional<FunctionBodyNode> functionBody = functionBodyGenerator.getFunctionBodyNode();

        Assert.assertTrue(functionBody.isEmpty());
        List<ClientDiagnostic> diagnostics = functionBodyGenerator.getDiagnostics();
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).getCode(), "OAS_CLIENT_118");
        Assert.assertTrue(diagnostics.get(0).getMessage().startsWith("the operation for given path `/items` , " +
                "method `post` is skipped in the client generation: "), diagnostics.get(0).getMessage());
        Assert.assertTrue(diagnostics.get(0).getMessage().contains("./external.yaml#/Item"),
                diagnostics.get(0).getMessage());
    }
}
