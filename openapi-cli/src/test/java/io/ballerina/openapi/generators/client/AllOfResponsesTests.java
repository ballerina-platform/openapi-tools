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

import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.FunctionReturnTypeGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode  Return type tests in
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator} util.
 */
public class AllOfResponsesTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Tests for returnType")
    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_allof_reference.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(response,
                ballerinaSchemaGenerator, new ArrayList<>());
        Assert.assertEquals(functionReturnType.getReturnType(response.getPaths().get("/products").getGet(),
                true), "CompoundTestsProductsResponse|error");
    }
    @Test(description = "Tests for returnType")
    public void getReturnTypeForAllOf() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/inline_all_of_response.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(response,
                ballerinaSchemaGenerator, new ArrayList<>());
        Assert.assertEquals(functionReturnType.getReturnType(response.getPaths().get("/users/{userId}/meetings")
                        .getPost(), true), "CompoundCreateMeetingResponse|error");
    }
    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForObjectSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_without_properties_with_additional.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(response,
                ballerinaSchemaGenerator, new ArrayList<>());

        String returnType = functionReturnType.getReturnType(response.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForMapSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_with_additional" +
                ".yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(response,
                ballerinaSchemaGenerator, new ArrayList<>());

        String returnType = functionReturnType.getReturnType(response.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "TestsProductsResponse|error");
    }

    @Test(description = "Tests for the object response without property and without additional properties")
    public void getReturnTypeForObjectSchemaWithOutAdditional() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
                "/response_without_properties_without_additional.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(response,
                ballerinaSchemaGenerator, new ArrayList<>());

        String returnType = functionReturnType.getReturnType(response.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the map response with property without additional properties")
    public void getReturnTypeForMapSchemaWithOutAdditionalProperties() throws IOException, BallerinaOpenApiException {
        OpenAPI response = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_properties_without_additional.yaml"));
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(response);
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(
                response, ballerinaSchemaGenerator, new ArrayList<>());

        String returnType = functionReturnType.getReturnType(response.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "TestsProductsResponse|error");
    }
    // 1. nested allof
    // 2. allof with reference
    // 3. allof inline record
    // 4. nested reference
}
