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

package io.ballerina.generators;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.generators.BallerinaClientGenerator.generatePathWithPathParameter;
import static io.ballerina.generators.BallerinaClientGenerator.getFunctionSignatureNode;
import static io.ballerina.generators.BallerinaClientGenerator.getReturnType;
import static io.ballerina.generators.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.generators.TestUtils.getDiagnostics;
import static io.ballerina.generators.TestUtils.getOpenAPI;

/**
 * All the tests related to the BallerinaClientGenerator util.
 */
public class BallerinaClientGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);


    @Test(description = "Generate Server URL")
    public void generateServerURL() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_server_with_base_path.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("client_template.bal", syntaxTree);
    }

    @Test(description = "Generate Operation Id")
    public void generateOperationId()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_without_operation_id.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_id.bal", syntaxTree);
    }

    @Test(description = "Generate Client for GET method")
    public void generateClientForGet()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_get.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_get.bal", syntaxTree);
    }

    @Test(description = "Generate Client for POST method")
    public void generateClientForPOST()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_post.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_post.bal", syntaxTree);
    }

    @Test(description = "Generate Client for header Parameter")
    public void generateClientForHeader()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/header_parameter.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("header_parameter.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi_weather_api method")
    public void generateClientForResponse()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_weather_api.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_weather_api.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi_weather_api method")
    public void generateClientForWeatherAPI()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_weather_api.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_weather_api.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi spec have display annotation method")
    public void generateClientForDisplayAnnotation()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_display_annotation.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_display_annotation.bal");
    }

    @Test(description = "Generate Client for openapi spec UBER")
    public void generateClientForUberAPI()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/uber_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("uber_openapi.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi spec COVID19")
    public void generateClientForCovid19API()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/covid19_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("covid19_openapi.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi spec JIRA", enabled = false)
    public void generateClientForJIRA()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/jira_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("jira_openapi.bal", syntaxTree);
    }

    @Test(description = "Generate Client for openapi spec world bank")
    public void generateClientForWorldBank()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/world_bank_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("world_bank_openapi.bal", syntaxTree);
    }

    @Test(description = "Generate Client for path parameter has parameter name as key word")
    public void generateClientForPathParameter()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/multiple_pathparam.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("multiple_pathparam.bal", syntaxTree);
    }

    @Test(description = "Generate Client for path parameter has parameter name as key word - unit tests for method")
    public void generatePathWithPathParameterTests() {
        Assert.assertEquals(generatePathWithPathParameter("/v1/v2"), "/v1/v2");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{name}"),
                 "/v1/${'version}/v2/${name}");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{limit}"),
                 "/v1/${'version}/v2/${'limit}");
        Assert.assertEquals(generatePathWithPathParameter("/v1/{age}/v2/{name}"), "/v1/${age}/v2/${name}");
    }

    @Test(description = "Test for generate function signature for given operations")
    public void getFunctionSignatureNodeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RES_DIR.resolve("swagger/valid_operation.yaml"));
        FunctionSignatureNode signature = getFunctionSignatureNode(openAPI.getPaths()
                .get("/products/{country}").getGet());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        RequiredParameterNode param02 = (RequiredParameterNode) parameters.get(1);
        RequiredParameterNode param03 = (RequiredParameterNode) parameters.get(2);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "latitude");
        Assert.assertEquals(param01.typeName().toString(), "float");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "longitude");
        Assert.assertEquals(param02.typeName().toString(), "float");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "country");
        Assert.assertEquals(param03.typeName().toString(), "string");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "ProductArr|error");
    }

    @Test(description = "Tests for returnType")
    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/all_return_type_operation.yaml"));
        Assert.assertEquals(getReturnType(array.getPaths().get("/jsonproducts").getGet()), "json|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/stringproducts/record").getGet()), "ProductArr|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlproducts").getGet()), "XML|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlarrayproducts").getGet()), "XMLArr|error");
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
