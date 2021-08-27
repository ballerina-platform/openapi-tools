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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.BallerinaSchemaGenerator;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the FunctionBodyNode generation in {@link BallerinaClientGenerator}
 * util.
 */
public class FunctionBodyNodeTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RESDIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RESDIR.resolve("ballerina_project/types.bal");
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();

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
                false, false, new ArrayList<>(), display, new BallerinaSchemaGenerator(display),
                new BallerinaAuthConfigGenerator(false, false));
        FunctionBodyNode bodyNode = functionBodyGenerator.getFunctionBodyNode(path, iterator.next());
        content = content.trim().replaceAll("\n", "").replaceAll("\\s+", "");
        String bodyNodeContent = bodyNode.toString().trim().replaceAll("\n", "")
                .replaceAll("\\s+", "");
        Assert.assertEquals(bodyNodeContent, content);
    }
    @DataProvider(name = "dataProviderForFunctionBody")
    public Object[][] dataProviderForFunctionBody() {
        return new Object[][]{
                {"diagnostic_files/header_parameter.yaml", "/pets", "{stringpath=string`/pets`;map<any>headerValues=" +
                        "{\"X-Request-ID\":xRequestId,\"X-Request-Client\":xRequestClient};map<string|string[]>" +
                        "accHeaders=getMapForHeaders(headerValues);http:Response response =checkself.clientEp->get" +
                        "(path,accHeaders, targetType=http:Response); return response;}"},
                {"diagnostic_files/head_operation.yaml", "/{filesystem}", "{string path=string`/${filesystem}`;" +
                        "map<anydata>queryParam={\"resource\":'resource,\"timeout\":timeout};" +
                        "path = path + check getPathForQueryParam(queryParam);" +
                        "map<any>headerValues={\"x-ms-client-request-id\":xMsClientRequestId," +
                        "\"x-ms-date\":xMsDate,\"x-ms-version\":xMsVersion};map<string|string[]> " +
                        "accHeaders = getMapForHeaders(headerValues);" +
                        "http:Responseresponse=check self.clientEp-> head(path, accHeaders);returnresponse;}"},
                {"diagnostic_files/operation_delete.yaml", "/pets/{petId}", "{string  path = string `/pets/${petId}`;" +
                        "http:Response response = check self.clientEp-> delete(path, targetType = http:Response);" +
                        "return response;}"},
                {"diagnostic_files/json_payload.yaml", "/pets", "{string  path = string `/pets`;" +
                        "http:Request request = new; request.setPayload(payload); " +
                        "http:Response response = check self.clientEp->" +
                        "post(path, request, targetType=http:Response); " +
                        "return response;}"},
                {"diagnostic_files/xml_payload.yaml", "/pets", "{string  path = string `/pets`; " +
                        "http:Request request = new;" +
                        "request.setPayload(payload); " +
                        "http:Response response = check self.clientEp->post(path, request, targetType=http:Response);" +
                        "return response;}"},
                {"diagnostic_files/xml_payload_with_ref.yaml", "/pets", "{string  path = string `/pets`;" +
                        "http:Request request = new;" +
                        "json jsonBody = check payload.cloneWithType(json);" +
                        "xml? xmlBody = check xmldata:fromJson(jsonBody);" +
                        "request.setPayload(xmlBody);" +
                        "http:Response response = check self.clientEp->post(path, request, targetType=http:Response);" +
                        "return response;}"}
        };
    }
    //TODO:Different mediaType

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
        } catch (IOException e) {
            //Ignore the exception
        }
    }

}
