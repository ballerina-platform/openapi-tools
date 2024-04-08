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

import io.ballerina.openapi.core.generators.client.FunctionReturnTypeGeneratorImp;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode  Return type tests as oneOf typein
 * {{@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}} util.
 */
public class OneOfResponsesTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Tests for returnType when response has array oneOf")
    public void getReturnTypeOneOfArray() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/inline_oneOf_response.yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(
                openapi.getPaths().get("/pet").getGet(), openapi);
        Assert.assertEquals(functionReturnType.getReturnType().get().type().toString(),
                "Inline_response_2XX|error");
    }

}
