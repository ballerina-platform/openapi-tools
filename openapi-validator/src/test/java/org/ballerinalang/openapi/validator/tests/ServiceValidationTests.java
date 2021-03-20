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
package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.validator.Filters;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for serviceValidation.
 */
public class ServiceValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/modules/serviceValidator/")
            .toAbsolutePath();
    private OpenAPI api;
    private Project project;
    private ServiceDeclarationNode serviceDeclarationNode;
    private List<String> tag = new ArrayList<>();
    private List<String> operation = new ArrayList<>();
    private List<String> excludeTag = new ArrayList<>();
    private List<String> excludeOperation = new ArrayList<>();
    private DiagnosticSeverity kind;
    private List<Diagnostic> diagnostics;
    private Filters filters;


    @Test(enabled = true, description = "test for undocumented Path in contract not in service")
    public void testUndocumentedPath() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Couldn't find a Ballerina service resource for " +
                "the path '/user' which is documented in the OpenAPI contract");
        diagnostics.clear();
    }

    @Test(description = "test for undocumented Method in contract missing method in bal service")
    public void testUndocumentedMethod() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstoreMethod.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Couldn't find Ballerina service resource(s) for" +
                " http method(s) 'post' for the path '/pets' which is documented in the OpenAPI contract");
        diagnostics.clear();
    }

    @Test(description = "Test for all Paths and methods documented")
    public void testPathandMethodsCorrectlyDocumented() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/valid/petstore.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        Assert.assertTrue(diagnostics.isEmpty());
        diagnostics.clear();
    }

    @Test(description = "test for undocumented TypeMisMatch in Path parameter")
    public void testParameterTypeMismatch() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstoreParameterTM.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Type mismatch with parameter 'petId' for the " +
                "method 'get' of the path '/pets/{petId}'.In OpenAPI contract its type is 'string' and resources " +
                "type is 'int'. ");
        diagnostics.clear();
    }

    @Test(description = "test for all the Path , Query, Payload scenarios")
    public void testRecordTypeMismatch() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/all_petstore.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Type mismatch with parameter 'id' for the method" +
                " 'delete' of the path '/pets/{id}'.In OpenAPI contract its type is 'integer' and resources type is " +
                "'string'. ");
        Assert.assertEquals(diagnostics.get(1).message(), "Type mismatching 'name' field in the record " +
                "type of the parameter 'NewPet' for the method 'post' of the path '/pets'.In OpenAPI contract its " +
                "type is 'string' and resources type is 'int'. ");
        Assert.assertEquals(diagnostics.get(2).message(), "''limit1' parameter for the method 'get' of " +
                "the resource associated with the path '/pets' is not documented in the OpenAPI contract");
        diagnostics.clear();
    }

    @Test(enabled = false, description = "test for undocumented record field  in contract")
    public void testRecordFieldMiss() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordFieldMiss.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
    }

    @Test(enabled = false, description = "test for undocumented path parameter  in contract")
    public void testPathParameter() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstorePathParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
    }

    @Test(enabled = false, description = "test for undocumented field oneOf type record in contract")
    public void testOneofscenario_01() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
    }

    @Test(enabled = false, description = "test for scenario 02")
    public void testOneofscenario_02() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf-scenario02.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
    }

    @Test(enabled = false, description = "test for scenario 03")
    public void testOneofscenario_03() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf-scenario03.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
    }

    @Test(enabled = true, description = "Test for extracting details from openapi annotaions")
    public void testExtractAnnotation() throws OpenApiValidatorException, IOException {
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/annotation/petstore.bal"));
//        diagnostics = ServiceValidator.validateResourceFunctions(project);
        serviceDeclarationNode
        Assert.assertTrue(!diagnostics.isEmpty());
        Assert.assertEquals(diagnostics.get(0).message(), "Couldn't find a Ballerina service resource for " +
                "the path '/user' which is documented in the OpenAPI contract");
        diagnostics.clear();
    }


    /**
     * OneOf - Invalid Scenario examples
     */
    //      Scenario-01         (record)   cat - place02, mealType        | (schema) cat - place, mealType
    //      Scenario-02         (record)   cat - place, mealType, canFly  | (schema) cat - place, mealType
    //      Scenario-03         (record)   cat - place, mealType          | (schema) cat - place, mealType , mealTime
}
