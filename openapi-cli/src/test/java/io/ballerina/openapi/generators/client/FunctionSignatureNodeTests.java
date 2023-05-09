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
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.FunctionSignatureGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode in {
 * {@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}  util.
 */
public class FunctionSignatureNodeTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RESDIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RESDIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for generate function signature for given operations")
    public void getFunctionSignatureNodeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/valid_operation.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/products/{country}").getGet(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        RequiredParameterNode param02 = (RequiredParameterNode) parameters.get(1);
        RequiredParameterNode param03 = (RequiredParameterNode) parameters.get(2);
        RequiredParameterNode param04 = (RequiredParameterNode) parameters.get(3);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "latitude");
        Assert.assertEquals(param01.typeName().toString(), "decimal");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "longitude");
        Assert.assertEquals(param02.typeName().toString(), "decimal");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "country");
        Assert.assertEquals(param03.typeName().toString(), "string");

        Assert.assertEquals(param04.paramName().orElseThrow().text(), "pages");
        Assert.assertEquals(param04.typeName().toString(), "int[]");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "Product[]|error");
    }

    @Test(description = "Test for generate function signature for xml request body")
    public void testFunctionSignatureNodeForXMLPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/xml_request_payload.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "xml");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "http:Response|error");
    }

    @Test(description = "Test for generate function signature for json request body")
    public void testFunctionSignatureNodeForJSONPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/json_request_payload.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "json");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "http:Response|error");
    }

    @Test(description = "Test for generate function signature for multipart custom header")
    public void testFunctionSignatureNodeForMultipartCustomHeader() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(
                RESDIR.resolve("swagger/multipart_formdata_custom.yaml"), true);
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());

        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "Pets_body");

        RequiredParameterNode param02 = (RequiredParameterNode) parameters.get(1);
        Assert.assertEquals(param02.paramName().orElseThrow().text(), "xAddressHeader");
        Assert.assertEquals(param02.typeName().toString(), "string");

        DefaultableParameterNode param03 = (DefaultableParameterNode) parameters.get(2);
        Assert.assertEquals(param03.paramName().orElseThrow().text(), "xCustomHeader");
        Assert.assertEquals(param03.typeName().toString(), "string?");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "http:Response|error");
    }

    @Test(description = "Test for generate function signature with nested array return type")
    public void getFunctionSignatureForNestedArrayResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/response_nested_array.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/timestags").getGet(), new ArrayList<>());
        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "string[][]|error");
    }

    @Test(description = "Test for generate function signature with string array return type")
    public void getFunctionSignatureForStringArrayResponse() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/response_string_array.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/timestags").getGet(), new ArrayList<>());
        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "string[]|error");
    }

    @Test(description = "Test parameter generation for request body with reference")
    public void getFunctionSignatureForRequestBodyWithRef() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/request_body_with_ref.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "CreatedPet");
    }

    @Test(description = "Test parameter generation for request body with unsupported (application/pdf) media type")
    public void getFunctionSignatureForUnsupportedRequests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/pdf_payload.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        Assert.assertEquals(param01.paramName().orElseThrow().text(), "request");
        Assert.assertEquals(param01.typeName().toString(), "http:Request");
    }

    @Test(description = "Test unsupported nested array type query parameter generation",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Unsupported parameter type is found in the parameter : .*")
    public void testNestedArrayQueryParamGeneration() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/invalid_array_query_params.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getGet(), new ArrayList<>());
    }

    @Test(description = "Test generation of array type query parameter when type of the parameter not given",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Please define the array item type of the parameter : .*")
    public void testArrayQueryParamWithNoTypeGeneration() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/invalid_array_query_params.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/dogs").getGet(), new ArrayList<>());
    }

    @Test(description = "Test for generate function signature for an integer request")
    public void testNumericFunctionSignatureJSONPayload() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/integer_request_payload.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "payload");
        Assert.assertEquals(param01.typeName().toString(), "int:Signed32");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "int:Signed32|error");
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
