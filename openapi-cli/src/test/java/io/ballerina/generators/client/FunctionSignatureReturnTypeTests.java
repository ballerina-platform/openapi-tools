///*
// * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package io.ballerina.generators.client;
//
//import io.ballerina.openapi.exception.BallerinaOpenApiException;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static io.ballerina.generators.client.BallerinaClientGenerator.getReturnType;
//import static io.ballerina.generators.common.TestUtils.getOpenAPI;
//
///**
// * All the tests related to the functionSignatureNode  Return type tests in
// * {@link BallerinaClientGenerator} util.
// */
//public class FunctionSignatureReturnTypeTests {
//    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
//    @Test(description = "Tests for returnType")
//    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
//        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/all_return_type_operation.yaml"));
//        Assert.assertEquals(getReturnType(array.getPaths().get("/jsonproducts").getGet()), "json|error");
//        Assert.assertEquals(getReturnType(array.getPaths().get("/stringproducts/record").getGet()),
//                "ProductArr|error");
//        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlproducts").getGet()), "XML|error");
//        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlarrayproducts").getGet()), "XMLArr|error");
//    }
//
//    @Test(description = "Tests for the object response without property")
//    public void getReturnTypeForObjectSchema() throws IOException, BallerinaOpenApiException {
//        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_without_properties_with_additional" +
//                ".yaml"));
//        String returnType = getReturnType(array.getPaths().get("/products").getGet());
//        Assert.assertEquals(returnType, "json|error");
//    }
//
//    @Test(description = "Tests for the object response without property")
//    public void getReturnTypeForMapSchema() throws IOException, BallerinaOpenApiException {
//        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_with_additional" +
//                ".yaml"));
//        String returnType = getReturnType(array.getPaths().get("/products").getGet());
//        Assert.assertEquals(returnType, "TestsProductsResponse|error");
//    }
//
//    @Test(description = "Tests for the object response without property and without additional properties")
//    public void getReturnTypeForObjectSchemaWithOutAdditional() throws IOException, BallerinaOpenApiException {
//        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
//                "/response_without_properties_without_additional" +
//                ".yaml"));
//        String returnType = getReturnType(array.getPaths().get("/products").getGet());
//        Assert.assertEquals(returnType, "json|error");
//    }
//
//    @Test(description = "Tests for the map response with property without additional properties")
//    public void getReturnTypeForMapSchemaWithOutAdditionalProperties() throws IOException, BallerinaOpenApiException {
//        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_without_additional" +
//                ".yaml"));
//        String returnType = getReturnType(array.getPaths().get("/products").getGet());
//        Assert.assertEquals(returnType, "TestsProductsResponse|error");
//    }
//}
