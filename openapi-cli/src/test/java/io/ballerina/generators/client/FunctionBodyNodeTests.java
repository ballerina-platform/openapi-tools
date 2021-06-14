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

package io.ballerina.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
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

import static io.ballerina.generators.BallerinaClientGenerator.getFunctionBodyNode;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the FunctionBodyNode generation in {@link io.ballerina.generators.BallerinaClientGenerator}
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
        FunctionBodyNode bodyNode = getFunctionBodyNode(path, iterator.next());
        content = content.trim().replaceAll("\n", "").replaceAll("\\s+", "");
        String bodyNodeContent = bodyNode.toString().trim().replaceAll("\n", "").replaceAll("\\s+", "");
        Assert.assertEquals(bodyNodeContent, content);
    }
    @DataProvider(name = "dataProviderForFunctionBody")
    public Object[][] dataProviderForFunctionBody() {
        return new Object[][]{
                {"diagnostic_files/header_parameter.yaml", "/pets", "{string path=string`/pets`;" +
                        "map<string|string[]>accHeaders=" +
                        "{'X\\-Request\\-ID:'X\\-Request\\-ID,'X\\-Request\\-Client:'X\\-Request\\-Client};" +
                        "_=check self.clientEp-> get(path, accHeaders, targetType = " +
                        "http:Response);}"},
                {"file_provider/swagger/uber_openapi.yaml", "/history", "{string  path = string `/history`;\n" +
                        "        map<anydata> queryParam = {offset: offset, 'limit: 'limit};\n" +
                        "        path = path + getPathForQueryParam(queryParam);\n" +
                        "        Activities response = check self.clientEp-> get(path, targetType = Activities);\n" +
                        "        return response;}"},
                {"swagger/put_with_header.yaml", "/me/albums", "{string  path = string `/me/albums`;\n" +
                        "        map<anydata> queryParam = {ids: ids};\n" +
                        "        path = path + getPathForQueryParam(queryParam);\n" +
                        "        map<string|string[]> accHeaders = {Authorization: Authorization, 'Content\\-Type:" +
                        " 'Content\\-Type};\n" +
                        "        http:Request request = new;\n" +
                        "        json jsonBody = check payload.cloneWithType(json);\n" +
                        "        request.setPayload(jsonBody);\n" +
                        "         _ = check self.clientEp->put(path, request, headers = accHeaders, " +
                        "targetType=http:Response);}"},
        };
    }

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
