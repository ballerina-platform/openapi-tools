/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.openapi.validator.tests;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.validator.Constants;
import io.ballerina.openapi.validator.Filters;
import io.ballerina.openapi.validator.OpenAPIPathSummary;
import io.ballerina.openapi.validator.OpenApiValidatorException;
import io.ballerina.openapi.validator.ResourcePathSummary;
import io.ballerina.openapi.validator.ResourceValidator;
import io.ballerina.openapi.validator.ResourceWithOperation;
import io.ballerina.openapi.validator.ServiceValidator;
import io.ballerina.openapi.validator.error.MissingFieldInJsonSchema;
import io.ballerina.openapi.validator.error.OpenapiServiceValidationError;
import io.ballerina.openapi.validator.error.TypeMismatch;
import io.ballerina.openapi.validator.error.ValidationError;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.validator.tests.ValidatorTest.getFunctionDefinitionNodes;

/**
 * Test for serviceValidation.
 */
public class ServiceValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/modules/serviceValidator/")
            .toAbsolutePath();
    private OpenAPI api;
    private Project project;
    private List<String> tag = new ArrayList<>();
    private List<String> operation = new ArrayList<>();
    private List<String> excludeTag = new ArrayList<>();
    private List<String> excludeOperation = new ArrayList<>();
    private List<Diagnostic> diagnostics;

    @Test(description = "test for undocumented Path in contract not in service")
    public void testUndocumentedPath() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore.bal"));
        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, DiagnosticSeverity.ERROR);
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        // Make resourcePath summary
        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
        //  Filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
        List<OpenapiServiceValidationError> openApiMissingServiceMethod =
                ResourceWithOperation.checkOperationsHasFunctions(openAPIPathSummaries, resourcePathMap);
        Assert.assertFalse(openApiMissingServiceMethod.isEmpty());
        Assert.assertTrue(openApiMissingServiceMethod.get(0) instanceof OpenapiServiceValidationError);
        Assert.assertEquals(openApiMissingServiceMethod.get(0).getServicePath(), "/user");
    }

    @Test(description = "test for undocumented Method in contract missing method in bal service")
    public void testUndocumentedMethod() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstoreMethod.bal"));
        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, DiagnosticSeverity.ERROR);
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreMethod.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        // Make resourcePath summary
        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
        //  Filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
        List<OpenapiServiceValidationError> openApiMissingServiceMethod =
                ResourceWithOperation.checkOperationsHasFunctions(openAPIPathSummaries, resourcePathMap);
        Assert.assertFalse(openApiMissingServiceMethod.isEmpty());
        Assert.assertTrue(openApiMissingServiceMethod.get(0) instanceof OpenapiServiceValidationError);
        Assert.assertEquals(openApiMissingServiceMethod.get(0).getServicePath(), "/pets");
        Assert.assertEquals(openApiMissingServiceMethod.get(0).getServiceOperation(), "post");
    }

    @Test(description = "Test for all Paths and methods documented")
    public void testPathandMethodsCorrectlyDocumented() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/valid/petstore.bal"));
        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, DiagnosticSeverity.ERROR);
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        // Make resourcePath summary
        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
        //  Filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
        List<OpenapiServiceValidationError> openApiMissingServiceMethod =
                ResourceWithOperation.checkOperationsHasFunctions(openAPIPathSummaries, resourcePathMap);
        Assert.assertTrue(openApiMissingServiceMethod.isEmpty());
    }

    @Test(description = "test for undocumented TypeMisMatch in Path parameter")
    public void testParameterTypeMismatch() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstoreParameterTM.bal"));
        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, DiagnosticSeverity.ERROR);
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreParameterTM.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        // Make resourcePath summary
        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
        //  Filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
        List<ValidationError> error =
                ResourceValidator.validateResourceAgainstOperation(openAPIPathSummaries.get(0).getOperations().get(
                        "get"), resourcePathMap.get("/pets/{petId}").getMethods().get("get"),
                        ValidatorTest.getSemanticModel(project), ValidatorTest.getSyntaxTree(project));
        Assert.assertTrue(!error.isEmpty());
        Assert.assertTrue(error.get(0) instanceof TypeMismatch);
        Assert.assertEquals(error.get(0).getFieldName(), "petId");
        Assert.assertEquals(((TypeMismatch) error.get(0)).getTypeBallerinaType(), Constants.Type.INT);
        Assert.assertEquals(((TypeMismatch) error.get(0)).getTypeJsonSchema(), Constants.Type.STRING);
    }

    @Test(description = "test for all the Path , Query, Payload scenarios", enabled = false)
    public void testRecordTypeMismatch() {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/all_petstore.bal"));
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Type mismatch with parameter ''id'' for " +
                "the method ''delete'' of the path ''/pets/{id}''.In OpenAPI contract its type is ''integer'' and " +
                "resources type is ''string''. ");
        Assert.assertEquals(diagnostics.get(1).message(), "Type mismatching ''name'' field in the record " +
                "type of the parameter ''NewPet'' for the method ''post'' of the path ''/pets''.In OpenAPI " +
                "contract its type is ''string'' and resources type is ''int''. ");
        Assert.assertEquals(diagnostics.get(2).message(), "'''limit1'' parameter for the method ''get'' of " +
                "the resource associated with the path ''/pets'' is not documented in the OpenAPI contract");
        diagnostics.clear();
    }

    @Test(description = "test for undocumented record field  in contract")
    public void testRecordFieldMiss() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstoreRecordFieldMiss.bal"));
        ServiceDeclarationNode serviceDeclarationNode = ValidatorTest.getServiceDeclarationNode(project);
        List<FunctionDefinitionNode> functions = getFunctionDefinitionNodes(serviceDeclarationNode);
        Filters filters = new Filters(tag, excludeTag, operation, excludeOperation, DiagnosticSeverity.ERROR);
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordFieldMiss.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        // Make resourcePath summary
        Map<String, ResourcePathSummary> resourcePathMap = ResourceWithOperation.summarizeResources(functions);
        //  Filter openApi operation according to given filters
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.filterOpenapi(api, filters);
        List<ValidationError> error =
                ResourceValidator.validateResourceAgainstOperation(openAPIPathSummaries.get(0).getOperations()
                                .get("post"), resourcePathMap.get("/pets").getMethods().get("post"),
                        ValidatorTest.getSemanticModel(project), ValidatorTest.getSyntaxTree(project));
        Assert.assertTrue(!error.isEmpty());
        Assert.assertTrue(error.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(error.get(0).getFieldName(), "name02");

    }


    /**
     * OneOf - Invalid Scenario examples
     */
    //      Scenario-01         (record)   cat - place02, mealType        | (schema) cat - place, mealType
    //      Scenario-02         (record)   cat - place, mealType, canFly  | (schema) cat - place, mealType
    //      Scenario-03         (record)   cat - place, mealType          | (schema) cat - place, mealType , mealTime
}
