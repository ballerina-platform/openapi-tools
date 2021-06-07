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
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.ballerina.generators.client.BallerinaClientGenerator.getFunctionBodyNode;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the Header sections in the swagger file.
 */
public class HeadersTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Test for header that comes under the parameter section.")
    public void getHeaderTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("diagnostic_files/header_parameter.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> operation =
                display.getPaths().get("/pets").readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = operation.iterator();
        FunctionBodyNode bodyNode = getFunctionBodyNode("/pets", iterator.next());
        Assert.assertEquals(bodyNode.toString(), "{string path=string`/pets`;map<string|string[]>accHeaders=" +
                "{'X\\-Request\\-ID:'X\\-Request\\-ID,'X\\-Request\\-Client:'X\\-Request\\-Client};_=" +
                "check self.clientEp-> get(path, accHeaders, targetType=http:Response);}");
    }
}
