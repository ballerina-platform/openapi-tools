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
package io.ballerina.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.validator.Constants;
import io.ballerina.openapi.validator.OpenApiValidatorException;
import io.ballerina.openapi.validator.ResourceMethod;
import io.ballerina.openapi.validator.ResourceValidator;
import io.ballerina.openapi.validator.ServiceValidator;
import io.ballerina.openapi.validator.error.MissingFieldInBallerinaType;
import io.ballerina.openapi.validator.error.MissingFieldInJsonSchema;
import io.ballerina.openapi.validator.error.TypeMismatch;
import io.ballerina.openapi.validator.error.ValidationError;
import io.ballerina.projects.Project;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for resource function validates against to openAPI operation.
 */
public class ResourceToOperationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/modules/resourceHandle/")
            .toAbsolutePath();
    private OpenAPI api;
    private Project project;
    private Operation operation;
    private ResourceMethod resourceMethod;
    private SyntaxTree syntaxTree;
    private SemanticModel semanticModel;
    private List<ValidationError> validationError = new ArrayList<>();

    @Test(description = "valid test for Path parameter")
    public void testPathParameterValid() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore_path_parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/valid/petstore_path_parameter.bal"));
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets/{petId}", "get");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.isEmpty());
    }

    @Test(description = "invalid test for Path parameter: type miss matching")
    public void testPathParameterInvalid() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore_path_parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore_path_parameter.bal"));
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets/{petId}", "get");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationError.get(0).getFieldName(), "petId");
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }
    //paramName change
    @Test(description = "invalid test for Path parameter: parameter miss -  ")
    public void testMissingPathParam() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore_path_parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore_path_parameter.bal"));
        operation = api.getPaths().get("/pets/{petId}").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets/{petId}", "get");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationError.get(0).getFieldName(), "petId");
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(description = "invalid test for Path parameter: when path has two path parameters")
    public void testTypeMissMatchTwoPathParam() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore_path_2parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore_path_2parameter.bal"));
        operation = api.getPaths().get("/pets/{petId}/{petName}").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets/{petId}/{petName}", "get");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationError.get(0).getFieldName(), "petId");
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeJsonSchema(), Constants.Type.STRING);
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeBallerinaType(), Constants.Type.INT);
    }

    @Test(description = "Query param supports only for 'int', 'string', 'float', 'boolean', 'decimal' types")
    public void testTypeMissMatchQueryParam() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore_query_parameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore_query_parameter.bal"));
        operation = api.getPaths().get("/pets").getGet();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets", "get");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.get(0) instanceof TypeMismatch);
        Assert.assertEquals(validationError.get(0).getFieldName(), "'limit");
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeJsonSchema(), Constants.Type.INTEGER);
        Assert.assertEquals(((TypeMismatch) (validationError).get(0)).getTypeBallerinaType(), Constants.Type.STRING);
    }

    @Test(description = "Invalid test for RequestPayload parameter: when request payload parameters has missing field.")
    public void testMissingFieldMatchRequestPayloadParam() throws OpenApiValidatorException, IOException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstore_payload_missfield.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        project = ValidatorTest.getProject(RES_DIR.resolve("ballerina/invalid/petstore_rb_missfieldpayload.bal"));
        operation = api.getPaths().get("/pets").getPost();
        resourceMethod = ValidatorTest.getResourceMethod(project, "/pets", "post");
        syntaxTree = ValidatorTest.getSyntaxTree(project);
        semanticModel = ValidatorTest.getSemanticModel(project);
        validationError = ResourceValidator.validateResourceAgainstOperation(operation, resourceMethod, semanticModel
                , syntaxTree);
        Assert.assertTrue(validationError.get(0) instanceof MissingFieldInJsonSchema);
        Assert.assertEquals(validationError.get(0).getFieldName(), "id");
        Assert.assertEquals(((MissingFieldInJsonSchema) (validationError).get(0)).getType(), Constants.Type.INT);
        Assert.assertTrue(validationError.get(1) instanceof MissingFieldInBallerinaType);
        Assert.assertEquals(validationError.get(1).getFieldName(), "userName");
        Assert.assertEquals(((MissingFieldInBallerinaType) (validationError).get(1)).getType(), Constants.Type.STRING);
    }
}
