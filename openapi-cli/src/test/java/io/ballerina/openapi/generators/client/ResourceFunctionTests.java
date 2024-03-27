///*
// * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.ballerina.openapi.generators.client;
//
//import io.ballerina.compiler.syntax.tree.SyntaxTree;
//import io.ballerina.openapi.core.generators.common.GeneratorUtils;
//import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
//import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
//import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
//import io.ballerina.openapi.core.generators.common.model.Filter;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
//
///**
// * Test for the resource function generation.
// */
//public class ResourceFunctionTests {
//    private static final Path RESDIR = Paths.get("src/test/resources/generators/client/resource").toAbsolutePath();
//    private SyntaxTree syntaxTree;
//    List<String> list1 = new ArrayList<>();
//    List<String> list2 = new ArrayList<>();
//    Filter filter = new Filter(list1, list2);
//    @Test(description = "Generate Client for all methods with resource function")
//    public void generateForAllMethods() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RESDIR.resolve("swagger/all_methods.yaml");
//        Path expectedPath = RESDIR.resolve("ballerina/all_methods.bal");
//        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
//        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
//        OASClientConfig oasClientConfig = clientMetaDataBuilder
//                .withFilters(filter)
//                .withOpenAPI(openAPI).build();
//        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
//        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
//    }
//
//    @Test(description = "Generate Client for headers")
//    public void generateForHeaders() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RESDIR.resolve("swagger/header.yaml");
//        Path expectedPath = RESDIR.resolve("ballerina/header.bal");
//        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
//        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
//        OASClientConfig oasClientConfig = clientMetaDataBuilder
//                .withFilters(filter)
//                .withOpenAPI(openAPI).build();
//        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
//        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
//    }
//
//    @Test(description = "Generate Client for pathParameters")
//    public void generateForPathParameters() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RESDIR.resolve("swagger/pathParameters.yaml");
//        Path expectedPath = RESDIR.resolve("ballerina/pathParameters.bal");
//        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
//        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
//        OASClientConfig oasClientConfig = clientMetaDataBuilder
//                .withFilters(filter)
//                .withOpenAPI(openAPI).build();
//        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
//        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
//    }
//
//    @Test(description = "Generate Client for request body")
//    public void generateForRequestBody() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RESDIR.resolve("swagger/request_body.yaml");
//        Path expectedPath = RESDIR.resolve("ballerina/request_body.bal");
//        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
//        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
//        OASClientConfig oasClientConfig = clientMetaDataBuilder
//                .withFilters(filter)
//                .withOpenAPI(openAPI).build();
//        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
//        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
//    }
//
//    @Test(description = "Generate Client for reference path parameters")
//    public void generateReferenceResolvePath() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RESDIR.resolve("swagger/reference_path.yaml");
//        Path expectedPath = RESDIR.resolve("ballerina/reference_path.bal");
//        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
//        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
//        OASClientConfig oasClientConfig = clientMetaDataBuilder
//                .withFilters(filter)
//                .withOpenAPI(openAPI).build();
//        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
//        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
//    }
//}
