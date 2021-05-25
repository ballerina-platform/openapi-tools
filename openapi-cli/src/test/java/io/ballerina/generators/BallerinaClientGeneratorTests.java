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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.generators.BallerinaClientGenerator.extractDisplayAnnotation;
import static io.ballerina.generators.BallerinaClientGenerator.generatePathWithPathParameter;
import static io.ballerina.generators.BallerinaClientGenerator.getFunctionBodyNode;
import static io.ballerina.generators.BallerinaClientGenerator.getFunctionSignatureNode;
import static io.ballerina.generators.BallerinaClientGenerator.getReturnType;
import static io.ballerina.generators.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.generators.TestUtils.getDiagnostics;
import static io.ballerina.generators.TestUtils.getOpenAPI;

/**
 * All the tests related to the {@link io.ballerina.generators.BallerinaClientGenerator} util.
 */
public class BallerinaClientGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

//
//    {"petstore_server_with_base_path.yaml"},
//    {"petstore_without_operation_id.yaml"},
//    {"petstore_get.yaml"},
//    {"openapi_display_annotation.yaml"},
//    {"header_parameter.yaml"},
//    {"petstore_post.yaml"},
//    {"petstore_with_oneOf_response.yaml"}
    @Test(description = "Generate Client for path parameter has parameter name as key word", enabled = true)
    public void generateClientForJira() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
//        Path definitionPath = RES_DIR.resolve("file_provider/swagger/jira_openapi.yaml");
        Path definitionPath = RES_DIR.resolve("swagger/petstore_without_operation_id.yaml");
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/jira_openapi.bal");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Generate Client for salesforce yaml", enabled = false)
    public void generateClientForSalesForce() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/salesforce.yaml");
        Path expectedPath = RES_DIR.resolve("/ballerina/salesforce.bal");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
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
        Assert.assertEquals(getReturnType(array.getPaths().get("/stringproducts/record").getGet()),
                "ProductArr|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlproducts").getGet()), "XML|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlarrayproducts").getGet()), "XMLArr|error");
    }

    @Test(description = "Display Annotation tests for parameters")
    public void extractDisplayAnnotationTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_display_annotation.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Map<String, Object> param01 =
                display.getPaths().get("/weather").getGet().getParameters().get(0).getExtensions();
        Map<String, Object> param02 =
                display.getPaths().get("/weather").getGet().getParameters().get(1).getExtensions();
        NodeList<AnnotationNode> annotationNodes01 = extractDisplayAnnotation(param01);
        NodeList<AnnotationNode> annotationNodes02 = extractDisplayAnnotation(param02);
        Assert.assertEquals(annotationNodes01.get(0).annotValue().orElseThrow().toString().trim(),
                "{label:\"City name\"}");
        Assert.assertTrue(annotationNodes02.isEmpty());
    }

    @Test(description = "Test for header that comes under the parameter section.")
    public void getHeaderTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/header_parameter.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> operation =
                display.getPaths().get("/pets").readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = operation.iterator();
        FunctionBodyNode bodyNode = getFunctionBodyNode("/pets", iterator.next());
        Assert.assertEquals(bodyNode.toString(), "{string path=string`/pets`;map<string|string[]>accHeaders={" +
                "'X\\-Request\\-ID:'X\\-Request\\-ID,'X\\-Request\\-Client:'X\\-Request\\-Client};http:Response " +
                "response=check self.clientEp-> get(path, accHeaders, targetType = http:Response );returnresponse;}");
    }

    @Test(description = "Tests functionBodyNodes including statements according to the different scenarios",
            dataProvider = "dataProviderForFunctionBody")
    public void getFunctionBodyNodes(String yamlFile, String path, String content) throws IOException,
            BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve(yamlFile);
        OpenAPI display = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> operation =
                display.getPaths().get(path).readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = operation.iterator();
        FunctionBodyNode bodyNode = getFunctionBodyNode(path, iterator.next());
        content = content.trim().replaceAll("\n", "").replaceAll("\\s+", "");
        String bodyNodeContent = bodyNode.toString().trim().replaceAll("\n", "").replaceAll("\\s+", "");
        Assert.assertEquals(bodyNodeContent, content);
    }

    @Test(description = "Test openAPI definition to ballerina client source code generation with diagnostic issue",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssues(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/" + yamlFile);
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @Test(description = "Test openAPI definition to ballerina client source code generation",
            dataProvider = "fileProviderForFilesComparison")
    public void  openApiToBallerinaCodeGenTestForClient(String yamlFile, String expectedFile) throws IOException,
            BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("file_provider/swagger/" + yamlFile);
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/" + expectedFile);
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath, syntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @DataProvider(name = "fileProviderForFilesComparison")
    public Object[][] fileProviderForFilesComparison() {
        return new Object[][]{
                {"openapi_weather_api.yaml", "openapi_weather_api.bal"},
                {"uber_openapi.yaml", "uber_openapi.bal"},
                {"multiple_pathparam.yaml", "multiple_pathparam.bal"},
                {"covid19_openapi.yaml", "covid19_openapi.bal"}
        };
    }

    @DataProvider(name = "singleFileProviderForDiagnosticCheck")
    public Object[][] singleFileProviderForDiagnosticCheck() {
        return new Object[][] {
                {"petstore_server_with_base_path.yaml"},
                {"petstore_without_operation_id.yaml"},
                {"petstore_get.yaml"},
                {"openapi_display_annotation.yaml"},
                {"header_parameter.yaml"},
                {"petstore_post.yaml"},
                {"petstore_with_oneOf_response.yaml"}
        };
    }

    @DataProvider(name = "dataProviderForFunctionBody")
    public Object[][] dataProviderForFunctionBody() {
        return new Object[][]{
                {"swagger/header_parameter.yaml", "/pets", "{string path=string`/pets`;" +
                        "map<string|string[]>accHeaders=" +
                        "{'X\\-Request\\-ID:'X\\-Request\\-ID,'X\\-Request\\-Client:'X\\-Request\\-Client};" +
                        "http:Response response=check self.clientEp-> get(path, accHeaders, targetType = " +
                        "http:Response);returnresponse;}"},
                {"file_provider/swagger/uber_openapi.yaml", "/history", "{string  path = string `/history`;\n" +
                        "        map<anydata> queryParam = {offset: offset, 'limit: 'limit};\n" +
                        "        path = path + getPathForQueryParam(queryParam);\n" +
                        "        Activities response = check self.clientEp-> get(path, targetType = Activities);\n" +
                        "        return response;}"},
        };
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
