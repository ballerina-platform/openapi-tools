///*
// * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
//package io.ballerina.openapi.validator.tests;
//
//import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
//import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
//import io.ballerina.openapi.validator.Filters;
//import io.ballerina.openapi.validator.OpenAPIPathSummary;
//import io.ballerina.openapi.validator.OpenApiValidatorException;
//import io.ballerina.openapi.validator.ResourcePathSummary;
//import io.ballerina.openapi.validator.ResourceWithOperation;
//import io.ballerina.openapi.validator.ValidatorUtils;
//import io.ballerina.openapi.validator.error.OpenapiServiceValidationError;
//import io.ballerina.projects.Project;
//import io.ballerina.tools.diagnostics.DiagnosticSeverity;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//import java.util.Map;
//
//import static io.ballerina.openapi.validator.tests.ValidatorTest.getFunctionDefinitionNodes;
//
///**
// * Test for resources and operations summaries.
// */
//public class ResourceTests {
//    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/modules/resourceHandle/")
//            .toAbsolutePath();
//
//    @Test(description = "Test for operation has path '/' and resource path has '.'")
//    public void rootPathTest() throws IOException, OpenApiValidatorException {
//        Project project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/root_path.bal"));
//        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
//        assert serviceDeclarationNode != null;
//        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
//        Filters filters = new Filters(DiagnosticSeverity.ERROR);
//        Path contractPath = RES_DIR.resolve("swagger/root_path.yaml");
//        OpenAPI api = ValidatorUtils.parseOpenAPIFile(contractPath.toString());
//        // Make resourcePath summary
//        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
//        //  Filter openApi operation according to given filters
//        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
//        List<OpenapiServiceValidationError> openApiMissingServiceMethod =
//                ResourceWithOperation.checkOperationsHasFunctions(openAPIPathSummaries, resourcePathMap);
//        Assert.assertTrue(openApiMissingServiceMethod.isEmpty());
//    }
//}
