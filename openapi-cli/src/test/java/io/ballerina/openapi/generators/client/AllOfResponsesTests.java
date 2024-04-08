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
import io.swagger.v3.oas.models.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;
import static org.testng.Assert.assertEquals;

/**
 * All the tests related to the functionSignatureNode  Return type tests in
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator} util.
 */
public class AllOfResponsesTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Tests for returnType")
    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_allof_reference.yaml"));
        TypeHandler.createInstance(response, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(
                response.getPaths().get("/products").getGet(), response);
        assertEquals(functionReturnType.getReturnType().get().type().toString(), "Inline_response_200|error");
    }

    @Test(description = "Tests for returnType")
    public void getReturnTypeForAllOf() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/inline_all_of_response.yaml"));
        TypeHandler.createInstance(response, false);
        Operation post = response.getPaths().get("/users/{userId}/meetings").getPost();
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(post, response);
        assertEquals(functionReturnType.getReturnType().get().type().toString(), "Inline_response_201|error");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForObjectSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_without_properties_with_additional.yaml"));
        TypeHandler.createInstance(response, false);
        Operation get = response.getPaths().get("/products").getGet();
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(get, response);

        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "record{|string...;|}|error");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForMapSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_with_additional" +
                ".yaml"));
        TypeHandler.createInstance(response, false);
        Operation get = response.getPaths().get("/products").getGet();
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(get, response);

        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }

    @Test(description = "Tests for the object response without property and without additional properties")
    public void getReturnTypeForObjectSchemaWithOutAdditional() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
                "/response_without_properties_without_additional.yaml"));
        TypeHandler.createInstance(response, false);
        Operation get = response.getPaths().get("/products").getGet();
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(get, response);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "record{}|error");
    }

    @Test(description = "Tests for the map response with property without additional properties")
    public void getReturnTypeForMapSchemaWithOutAdditionalProperties() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_properties_without_additional.yaml"));
        TypeHandler.createInstance(response, false);
        Operation get = response.getPaths().get("/products").getGet();
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(get, response);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }
    // 1. nested allof
    // 2. allof with reference
    // 3. allof inline record
    // 4. nested reference
}
