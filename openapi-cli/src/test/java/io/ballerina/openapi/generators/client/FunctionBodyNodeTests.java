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
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaAuthConfigGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.client.FunctionBodyGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
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
import java.util.Set;

import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

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
    public void getFunctionBodyNodes(String yamlFile, String path, String content) throws IOException,
            BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI display = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> operation =
                display.getPaths().get(path).readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = operation.iterator();
        FunctionBodyGenerator functionBodyGenerator = new FunctionBodyGenerator(new ArrayList<>(),
                new ArrayList<>(), display, new BallerinaTypesGenerator(display),
                new BallerinaAuthConfigGenerator(false, false), new BallerinaUtilGenerator(),
                false);
        FunctionBodyNode bodyNode = functionBodyGenerator.getFunctionBodyNode(path, iterator.next());
        content = content.trim().replaceAll("\n", "").replaceAll("\\s+", "");
        String bodyNodeContent = bodyNode.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(bodyNodeContent, content);
    }
    @DataProvider(name = "dataProviderForFunctionBody")
    public Object[][] dataProviderForFunctionBody() {
        return new Object[][]{
                {"diagnostic_files/header_parameter.yaml", "/pets", "{string resourcePath=string`/pets`;" +
                        "map<any>headerValues=" +
                        "{\"X-Request-ID\":xRequestId,\"X-Request-Client\":xRequestClient};map<string|string[]>" +
                        "httpHeaders=getMapForHeaders(headerValues);http:Response response =checkself.clientEp->get" +
                        "(resourcePath,httpHeaders); return response;}"},
                {"diagnostic_files/head_operation.yaml", "/{filesystem}",
                        "{string resourcePath=string`/${getEncodedUri(filesystem)}`;" +
                        "map<anydata>queryParam={\"resource\":'resource,\"timeout\":timeout};" +
                        "resourcePath = resourcePath + check getPathForQueryParam(queryParam);" +
                        "map<any>headerValues={\"x-ms-client-request-id\":xMsClientRequestId," +
                        "\"x-ms-date\":xMsDate,\"x-ms-version\":xMsVersion};map<string|string[]> " +
                        "httpHeaders = getMapForHeaders(headerValues);" +
                        "http:Responseresponse=check self.clientEp-> head(resourcePath, httpHeaders);returnresponse;}"},
                {"diagnostic_files/operation_delete.yaml", "/pets/{petId}", "{string resourcePath = " +
                        "string `/pets/${getEncodedUri(petId)}`;" +
                        "http:Response response = check self.clientEp-> delete(resourcePath);" +
                        "return response;}"},
                {"diagnostic_files/json_payload.yaml", "/pets", "{string resourcePath = string `/pets`;" +
                        "http:Request request = new; request.setPayload(payload, \"application/json\"); " +
                        "http:Response response = check self.clientEp->" +
                        "post(resourcePath, request); " +
                        "return response;}"},
                {"diagnostic_files/xml_payload.yaml", "/pets", "{string resourcePath = string `/pets`; " +
                        "http:Request request = new;" +
                        "request.setPayload(payload, \"application/xml\"); " +
                        "http:Response response = check self.clientEp->post(resourcePath, request);" +
                        "return response;}"},
                {"diagnostic_files/xml_payload_with_ref.yaml", "/pets", "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "json jsonBody = payload.toJson();" +
                        "xml? xmlBody = check xmldata:fromJson(jsonBody);" +
                        "request.setPayload(xmlBody, \"application/xml\");" +
                        "http:Response response = check self.clientEp->post(resourcePath, request);" +
                        "return response;}"},
                {"client/swagger/response_type_order.yaml", "/pet/{petId}",
                        "{string resourcePath = string `/pet/${getEncodedUri(petId)}`;" +
                        "Pet response = check self.clientEp->get(resourcePath);" +
                        "return response;}"},
                {"client/swagger/text_request_payload.yaml", "/pets", "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "json jsonBody = payload.toJson();" +
                        "request.setPayload(jsonBody, \"text/json\");" +
                        "json response = check self.clientEp->post(resourcePath, request);" +
                        "return response;}"},
                {"client/swagger/pdf_payload.yaml", "/pets", "{string resourcePath = string `/pets`;" +
                        "// TODO: Update the request as needed;\n" +
                        "http:Response response = check self.clientEp->post(resourcePath, request);" +
                        "return response;}"},
                {"client/swagger/image_payload.yaml", "/pets", "{string resourcePath = string `/pets`;" +
                        "http:Request request = new;" +
                        "request.setPayload(payload, \"image/png\");" +
                        "http:Response response = check self.clientEp->post(resourcePath, request);" +
                        "return response;}"},
                {"client/swagger/multipart_formdata_custom.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "http:Request request = new;\n" +
                        "map<Encoding> encodingMap = {\"profileImage\": {contentType: \"image/png\", headers: " +
                        "{\"X-Custom-Header\": xCustomHeader}}, \"id\":{headers: {\"X-Custom-Header\": " +
                        "xCustomHeader}}, \"address\": {headers:{\"X-Address-Header\":xAddressHeader}}, \"name\":" +
                        "{contentType:\"text/plain\"}};\n" +
                        "mime:Entity[] bodyParts = check createBodyParts(payload, encodingMap);\n" +
                        "request.setBodyParts(bodyParts);\n" +
                        "http:Response response = check self.clientEp->post(resourcePath, request);\n" +
                        "return response;}"},
                {"client/swagger/empty_object_responnse.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        json response = check self.clientEp->post(resourcePath, request);\n" +
                        "        return response;}"},
                {"client/swagger/map_schema_response.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        json response = check self.clientEp->post(resourcePath, request);\n" +
                        "        return response;}"},
                {"client/swagger/array_response_pdf.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        http:Response response = check self.clientEp->post(resourcePath, request);\n" +
                        "        return response;}"},
                {"client/swagger/any_type_response.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "        // TODO: Update the request as needed;\n" +
                        "        http:Response response = check self.clientEp->post(resourcePath, request);\n" +
                        "        return response;}"},
                {"client/swagger/return_type/no_response.yaml", "/pets", "{string resourcePath = string `/pets`;\n" +
                        "        map<anydata> queryParam = {\"limit\": 'limit};\n" +
                        "        resourcePath = resourcePath + check getPathForQueryParam(queryParam);\n" +
                        "        http:Response response = check self.clientEp->get(resourcePath);\n" +
                        "        return response;}"}
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
