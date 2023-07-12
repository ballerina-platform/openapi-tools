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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.service.BallerinaServiceGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * All the tests related to the {@code io.ballerina.openapi.generators.service.RequestBodyGenerator} util.
 */
public class RequestBodyTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;

    @Test(description = "Scenario 01 - Request Body has single content type(application/json)")
    public void generateJsonPayload() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/scenario01_rb.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("requestBody/scenario_01_rb.bal",
                syntaxTree);
    }

    @Test(description = "Scenario 01.02 - Request Body has single content type(application/octet-stream)")
    public void generateOtherPayload() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/scenario01_02_rb.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("requestBody/scenario_0102_rb.bal",
                syntaxTree);
    }

    @Test(description = "Scenario 02 - Request Body has multiple content types with Same dataBind schema type.\n")
    public void generateRBsameDataBindingPayload() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/scenario02_rb.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("requestBody/scenario_02_rb.bal",
                syntaxTree);
    }

    @Test(description = "Scenario 03 - Request Body has multiple content types with Different dataBind schema types.")
    public void generateMultipleContent() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/multiple_content.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("requestBody/scenario_03_rb.bal",
                syntaxTree);
    }

    @Test(description = "Scenario 04 - Request Body has record name with special characters.")
    public void generateRecordName() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/record_name_refactor.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/refactor_record_name.bal", syntaxTree);
    }

    @Test(description = "Scenario 05 - Request Body has text/* mediatype.")
    public void generateForMediaType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/scenario04_rb.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/scenario_04_rb.bal", syntaxTree);
    }

    @Test(description = "RequestBody has oneOf scenarios")
    public void oneOfScenarios() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/oneOf_request_body.yaml");
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(GeneratorUtils.normalizeOpenAPI(definitionPath, false))
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator serviceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = serviceGenerator.generateSyntaxTree();
        //Generate records according to schemas
        List<TypeDefinitionNode> typeInclusionRecords = serviceGenerator.getTypeInclusionRecords();
        BallerinaTypesGenerator recordGenerator = new BallerinaTypesGenerator(
                oasServiceMetadata.getOpenAPI(), oasServiceMetadata.isNullable(), typeInclusionRecords);
        SyntaxTree typeSyntaxTree = recordGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/oneof_requestBody.bal", syntaxTree);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/oneof_request_body_types.bal", typeSyntaxTree);

    }

    @Test(description = "RequestBody has url encode media type  scenarios")
    public void uRLEncode() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/url_form_encode.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/url_encode.bal", syntaxTree);
    }

    @Test(description = "RequestBody has reference to component requestBody sections")
    public void referenceRequestBody() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/reference_rb.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/reference_rb.bal", syntaxTree);
    }

    @Test
    public void testForRequestBodyHasMultipartFormDataPayload()
            throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/multipart_form_data.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/multipart_form_data.bal", syntaxTree);
    }


    @Test(description = "RequestBody has unhandled media type ex: application/zip")
    public void testForUnhandledMediaType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/unhandled_media_type.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/unhandled_media_type.bal", syntaxTree);
    }

    @Test(description = "RequestBody has anyOf type")
    public void testForAnyOf() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/any_of.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/any_of.bal", syntaxTree);
    }

    @Test(description = "RequestBody has object type with additional property")
    public void testForAdditionalProperty() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/requestBody/additional_property.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, false);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        BallerinaTypesGenerator ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI, false,
                ballerinaServiceGenerator.getTypeInclusionRecords());
        SyntaxTree typeSyntaxTree = ballerinaTypesGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/additional_property.bal", syntaxTree);
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "requestBody/additional_prop_types.bal", typeSyntaxTree);
    }
}
