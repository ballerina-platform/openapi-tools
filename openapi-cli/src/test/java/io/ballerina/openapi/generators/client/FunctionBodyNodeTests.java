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

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.client.FunctionBodyGeneratorImp;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADERS;
import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

/**
 * All the tests related to the FunctionBodyNode generation in {
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}} util.
 */
public class FunctionBodyNodeTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators").toAbsolutePath();
    private static final Path clientPath = RESDIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RESDIR.resolve("ballerina_project/types.bal");

    @Test(description = "Tests functionBodyNodes including statements according to the different scenarios",
            dataProvider = "dataProviderForFunctionBody")
    public void getFunctionBodyNodes(String yamlFile, String path, boolean hasHeaders, boolean hasDefaultHeaders,
                                     boolean hasQueries, String content) throws IOException,
            BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openapi = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> pathItem =
                openapi.getPaths().get(path).readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = pathItem.iterator();
        Map.Entry<PathItem.HttpMethod, Operation> operation = iterator.next();
        TypeHandler.createInstance(openapi, false);
        FunctionBodyGeneratorImp functionBodyGeneratorImp = new FunctionBodyGeneratorImp(path, operation, openapi,
                new AuthConfigGeneratorImp(false, false),
                new BallerinaUtilGenerator(), new ArrayList<>(), hasHeaders, hasDefaultHeaders, hasQueries, HEADERS);
        Optional<FunctionBodyNode> bodyNode = functionBodyGeneratorImp.getFunctionBodyNode();
        content = content.trim().replaceAll("\n", "").replaceAll("\\s+", "");
        String bodyNodeContent = bodyNode.get().toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(bodyNodeContent, content);
    }
    @DataProvider(name = "dataProviderForFunctionBody")
    public Object[][] dataProviderForFunctionBody() {
        return new Object[][]{
                {"diagnostic_files/header_parameter.yaml", "/pets", true, false, false,
                        "{string resourcePath=string`/pets`;" +
                        "map<string|string[]>" +
                        "httpHeaders=http:getHeaderMap(headers);return self.clientEp->" +
                        "get(resourcePath,httpHeaders);}"},
                {"diagnostic_files/head_operation.yaml", "/{filesystem}", true, false, true,
                        "{string resourcePath=string`/${getEncodedUri(filesystem)}`;" +
                        "resourcePath = resourcePath + check getPathForQueryParam(queries);" +
                        "map<string|string[]> httpHeaders = http:getHeaderMap(headers);" +
                        "return self.clientEp-> head(resourcePath, httpHeaders);}"},
                {"diagnostic_files/operation_delete.yaml", "/pets/{petId}", false, false, false,
                        "{string resourcePath = " +
                        "string `/pets/${getEncodedUri(petId)}`;" +
                        "return  self.clientEp-> delete(resourcePath);}"},
                {"diagnostic_files/json_payload.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;" +
                        "http:Request request = new; request.setPayload(payload, \"application/json\"); " +
                        "return  self.clientEp->post(resourcePath, request);}"},
                {"diagnostic_files/xml_payload.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`; " +
                        "http:Request request = new;" +
                        "request.setPayload(payload, \"application/xml\"); " +
                        "return self.clientEp->post(resourcePath, request);}"},
                {"diagnostic_files/xml_payload_with_ref.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "json jsonBody = payload.toJson();" +
                        "xml? xmlBody = check xmldata:fromJson(jsonBody);" +
                        "request.setPayload(xmlBody, \"application/xml\");" +
                        "return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/response_type_order.yaml", "/pet/{petId}", false, false, false,
                        "{string resourcePath = string `/pet/${getEncodedUri(petId)}`;" +
                        "return self.clientEp->get(resourcePath);}"},
                {"client/swagger/text_request_payload.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "json jsonBody = payload.toJson();" +
                        "request.setPayload(jsonBody, \"text/json\");" +
                        "return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/pdf_payload.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;" +
                        "// TODO: Update the request as needed;\n" +
                        " return  self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/image_payload.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "request.setPayload(payload, \"image/png\");" +
                        " return  self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/multipart_formdata_custom.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;\n" +
                        "http:Request request = new;\n" +
                        "map<Encoding> encodingMap = {\"profileImage\": {contentType: \"image/png\", headers: " +
                        "{\"X-Custom-Header\": X\\-Custom\\-Header}}, \"id\":{headers: {\"X-Custom-Header\": " +
                        "X\\-Custom\\-Header}}, \"address\": {headers:{\"X-Address-Header\":X\\-Address\\-Header}}, " +
                        "\"name\":" +
                        "{contentType:\"text/plain\"}};\n" +
                        "mime:Entity[] bodyParts = check createBodyParts(payload, encodingMap);\n" +
                        "request.setBodyParts(bodyParts);\n" +
                        " return  self.clientEp->post(resourcePath, request);\n}"},
                {"client/swagger/empty_object_responnse.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/map_schema_response.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/array_response_pdf.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/any_type_response.yaml", "/pets", false, false, false,
                        "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        return self.clientEp->post(resourcePath, request);}"},
                {"client/swagger/return_type/no_response.yaml", "/pets", false, false, true,
                        "{string resourcePath = string `/pets`;\n" +
                        "        resourcePath = resourcePath + check getPathForQueryParam(queries);\n" +
                        "        return self.clientEp->get(resourcePath);}"}
        };
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
        } catch (IOException ignored) {
        }
    }

}
