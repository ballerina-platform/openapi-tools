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
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.FunctionReturnTypeGenerator;
import io.ballerina.openapi.core.generators.client.FunctionReturnTypeGeneratorImp;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.schemaOld.BallerinaTypesGenerator;
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
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp();
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/all_return_type_operation.yaml"));
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/jsonproducts").getGet(),
                true), "json|error");
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/stringproducts/record").getGet(),
                true), "Product[]|error");
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/stringproducts/record").getGet(),
                false), "ProductArr|error");
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/xmlproducts").getGet(),
                true), "xml|error");
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/xmlarrayproducts").getGet(),
                true), "xml[]|error");
        Assert.assertEquals(functionReturnType.getReturnType(array.getPaths().get("/xmlarrayproducts").getGet(),
                false), "XMLArr|error");
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/products/nocontent").getGet(),
                true);
        Assert.assertEquals(returnType, "error?");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForObjectSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_without_properties_with_additional" +
                ".yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp();
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the object response without property")
    public void getReturnTypeForMapSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_properties_with_additional.yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(array,
                new BallerinaTypesGenerator(array), new ArrayList<>());
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }

    @Test(description = "Tests for the object response without property and without additional properties")
    public void getReturnTypeForObjectSchemaWithOutAdditional() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type" +
                "/response_without_properties_without_additional" +
                ".yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp();
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the map response with property without additional properties")
    public void getReturnTypeForMapSchemaWithOutAdditionalProperties() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_with_properties_without_additional" +
                ".yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(array,
                new BallerinaTypesGenerator(array), new ArrayList<>());
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/products").getGet(),
                true);
        Assert.assertEquals(returnType, "Inline_response_200|error");
    }

    @Test(description = "Tests for the response with no schema")
    public void getReturnTypeForResponseWithoutSchema() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/response_no_schema.yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(array,
                new BallerinaTypesGenerator(array), new ArrayList<>());
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/path01").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the empty response")
    public void getReturnTypeForEmptyResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/no_response.yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(array,
                new BallerinaTypesGenerator(array), new ArrayList<>());
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/pets").getGet(),
                true);
        Assert.assertEquals(returnType, "http:Response|error");
    }

    @Test(description = "Tests for the response with additional properties in OpenAPI 3.1 spec")
    public void getReturnTypeForAdditionalPropertySchema() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/" +
                "response_with_only_additional_schema.yaml"));
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(array,
                new BallerinaTypesGenerator(array), new ArrayList<>());
        String returnType = functionReturnType.getReturnType(array.getPaths().get("/store/inventory").getGet(),
                true);
        Assert.assertEquals(returnType, "json|error");
    }

    @Test(description = "Tests for the response without content type")
    public void getReturnTypeForNoContentType() throws IOException, BallerinaOpenApiException {
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
