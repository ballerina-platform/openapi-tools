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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.FunctionReturnTypeGeneratorImp;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode  Return type tests in
 * {{@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}} util.
 */
public class FunctionSignatureReturnTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    @Test(description = "Tests for returnType")
    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RES_DIR.resolve("swagger/return_type/all_return_type_operation.yaml"));
        TypeHandler.createInstance(openAPI, false);
        FunctionReturnTypeGeneratorImp returnType = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/jsonproducts").getGet(), openAPI);
        Assert.assertEquals(returnType.getReturnType().get().type().toString(), "json|error");
        FunctionReturnTypeGeneratorImp returnType2 = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/stringproducts/record").getGet(), openAPI);
        //todo check the- this should be product array
        Assert.assertEquals(returnType2.getReturnType().get().type().toString(), "Product[]|error");
//        Assert.assertEquals(functionReturnType.getReturnType(openAPI.getPaths().get("/stringproducts/record").getGet(),
//                false), "ProductArr|error");
        FunctionReturnTypeGeneratorImp returnType3 = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/xmlproducts").getGet(), openAPI);
        Assert.assertEquals(returnType3.getReturnType().get().type().toString(), "xml|error");
        FunctionReturnTypeGeneratorImp returnType4 = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/xmlarrayproducts").getGet(), openAPI);
        //todo need to check convention this should be xml
//        Assert.assertEquals(returnType4.getReturnType().get().type().toString(), "xml[]|error");
        FunctionReturnTypeGeneratorImp returnType5 = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/xmlarrayproducts").getGet(), openAPI);
        //todo need to check convention
//        Assert.assertEquals(returnType5.getReturnType().get().type().toString(), "XMLArr|error");
        FunctionReturnTypeGeneratorImp returnType6 = new FunctionReturnTypeGeneratorImp(openAPI.getPaths().get("/products/nocontent").getGet(), openAPI);
        String returnTypeV = returnType6.getReturnType().get().type().toString();
        Assert.assertEquals(returnTypeV, "error?");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForObjectSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_without_properties_with_additional" +
                ".yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/products").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "record{|string...;|}|error");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForMapSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_properties_with_additional.yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/products").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }

    @Test(description = "Tests for the object response without property and without additional properties")
    public void getReturnTypeForObjectSchemaWithOutAdditional() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
                "/response_without_properties_without_additional" +
                ".yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/products").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "record{}|error");
    }

    @Test(description = "Tests for the map response with property without additional properties")
    public void getReturnTypeForMapSchemaWithOutAdditionalProperties() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_without_additional" +
                ".yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/products").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }

    @Test(description = "Tests for the response with no schema")
    public void getReturnTypeForResponseWithoutSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_no_schema.yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/path01").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the empty response")
    public void getReturnTypeForEmptyResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/no_response.yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/pets").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "http:Response|error");
    }

    @Test(description = "Tests for the response with additional properties in OpenAPI 3.1 spec")
    public void getReturnTypeForAdditionalPropertySchema() throws IOException, BallerinaOpenApiException {
        OpenAPI openapi = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_only_additional_schema.yaml"));
        TypeHandler.createInstance(openapi, false);
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(openapi.getPaths().get("/store/inventory").getGet(), openapi);
        String returnType = functionReturnType.getReturnType().get().type().toString();
        Assert.assertEquals(returnType, "record{|int:Signed32...;|}|error");
    }

    @Test(description = "Tests for the response without content type")
    public void getReturnTypeForNoContentType() throws IOException, BallerinaOpenApiException, ClientException {
        OpenAPI openAPI = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
                "/no_content_type.yaml"));
        Path expectedPath = RES_DIR.resolve("ballerina/return/no_content_type.bal");

        List<String> list1 = new ArrayList<>();
        Filter filter = new Filter(list1, list1);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
}
