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

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.openapi.core.generators.client.RemoteFunctionSignatureGenerator;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode in {
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}  util.
 */
public class FunctionSignatureNodeTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RESDIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RESDIR.resolve("ballerina_project/types.bal");
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();

    @Test(description = "Test for generate function signature for given operations")
    public void getFunctionSignatureNodeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/valid_operation.yaml"));
        Operation operation = openAPI.getPaths().get("/products/{country}").getGet();
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(operation,
                openAPI, "get", "products/{country}");
        Optional<FunctionSignatureNode> signature = functionSignatureGenerator.generateFunctionSignature();
        SeparatedNodeList<ParameterNode> parameters = signature.get().parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        DefaultableParameterNode param02 = (DefaultableParameterNode) parameters.get(1);
        IncludedRecordParameterNode param03 = (IncludedRecordParameterNode) parameters.get(2);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "country");
        Assert.assertEquals(param01.typeName().toString(), "string");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "headers");
        Assert.assertEquals(param02.typeName().toString(), "map<string|string[]>");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "queries");
        Assert.assertEquals(param03.typeName().toString(), "GetProductsCountryQueries");

        ReturnTypeDescriptorNode returnTypeNode = signature.get().returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "Product[]|error");
    }

    @Test(description = "Test for generate function signature for xml request body")
    public void testFunctionSignatureNodeForXMLPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/xml_request_payload.yaml"));
        Operation operation = openAPI.getPaths().get("/pets").getPost();
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(operation,
                 openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "xml");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "error?");
    }

    @Test(description = "Test for generate function signature for json request body")
    public void testFunctionSignatureNodeForJSONPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/json_request_payload.yaml"));
        Operation operation = openAPI.getPaths().get("/pets").getPost();
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(operation,
                openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "json");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "error?");
    }

    @Test(description = "Test for generate function signature for multipart custom header")
    public void testFunctionSignatureNodeForMultipartCustomHeader() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(
                RESDIR.resolve("swagger/multipart_formdata_custom.yaml"), true, false);
        Operation operation = openAPI.getPaths().get("/pets").getPost();
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(operation,
                openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());

        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "pets_body");

        DefaultableParameterNode param02 = (DefaultableParameterNode) parameters.get(1);
        Assert.assertEquals(param02.paramName().orElseThrow().text(), "headers");
        Assert.assertEquals(param02.typeName().toString(), "map<string|string[]>");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "error?");
    }

    @Test(description = "Test for generate function signature with nested array return type")
    public void getFunctionSignatureForNestedArrayResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/response_nested_array.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/timestags").getGet(), openAPI, "get", "/timestags");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "string[][]|error");
    }

    @Test(description = "Test for generate function signature with string array return type")
    public void getFunctionSignatureForStringArrayResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/response_string_array.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/timestags").getGet(), openAPI, "get", "/timestags");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "string[]|error");
    }

    @Test(description = "Test parameter generation for request body with reference")
    public void getFunctionSignatureForRequestBodyWithRef() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/request_body_with_ref.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/pets").getPost(), openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "record{stringpetId?;stringcreatedDate?;}".trim());
    }

    @Test(description = "Test parameter generation for request body with unsupported (application/pdf) media type")
    public void getFunctionSignatureForUnsupportedRequests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/pdf_payload.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/pets").getPost(), openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "request");
        Assert.assertEquals(param01.typeName().toString(), "http:Request");
    }

    @Test(description = "Test unsupported nested array type query parameter generation",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Unsupported parameter type is found in the parameter : .*",
            enabled = false)
    public void testNestedArrayQueryParamGeneration() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/invalid_array_query_params.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/pets").getPost(), openAPI, "post", "/pets");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();

    }

    @Test(description = "Test generation of array type query parameter when type of the parameter not given",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Please define the array item type of the parameter : .*",
            enabled = false)
    public void testArrayQueryParamWithNoTypeGeneration() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/invalid_array_query_params.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator functionSignatureGenerator = new RemoteFunctionSignatureGenerator(
                openAPI.getPaths().get("/dogs").getGet(), openAPI, "get", "/dogs");
        FunctionSignatureNode signature = functionSignatureGenerator.generateFunctionSignature().get();

    }

    @Test(description = "Test for generate function signature for an integer request")
    public void testNumericFunctionSignatureJSONPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/integer_request_payload.yaml"));
        TypeHandler.createInstance(openAPI, false);
        RemoteFunctionSignatureGenerator signature = new RemoteFunctionSignatureGenerator(openAPI.getPaths().
                get("/pets").getPost(), openAPI, "post", "/pets");
        FunctionSignatureNode petSignature = signature.generateFunctionSignature().get();
        RemoteFunctionSignatureGenerator owSignature = new RemoteFunctionSignatureGenerator(openAPI.getPaths().
                get("/owners").getPost(), openAPI, "post", "/owners");
        FunctionSignatureNode ownerSignature = owSignature.generateFunctionSignature().get();
        SeparatedNodeList<ParameterNode> parameters = petSignature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode petParams = (RequiredParameterNode) parameters.get(0);
        RequiredParameterNode ownerParams = (RequiredParameterNode) ownerSignature.parameters().get(0);

        Assert.assertEquals(petParams.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(petParams.typeName().toString(), "int:Signed32");
        Assert.assertEquals(ownerParams.typeName().toString(), "int");

        ReturnTypeDescriptorNode petReturnTypeNode = petSignature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(petReturnTypeNode.type().toString(), "int:Signed32|error");
        ReturnTypeDescriptorNode ownerReturnTypeNode = ownerSignature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(ownerReturnTypeNode.type().toString(), "int|error");
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
