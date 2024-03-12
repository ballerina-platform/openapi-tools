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

package io.ballerina.openapi.generators.schema;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.FunctionSignatureGenerator;
import io.ballerina.openapi.core.typegenerator.BallerinaTypesGenerator;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for Schema Reference resolve.
 */
public class ReferenceResolveTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    @Test(description = "Tests with object type include reference")
    public void testReferenceIncludeWithObjectType() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/world_bank.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/world_bank.bal", syntaxTree);
    }

    @Test(description = "Test for object data type when absent reference and properties fields")
    public void testWorldBank() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/object_without_fields_reference.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/object_without_fields_reference.bal", syntaxTree);
    }
    @Test(description = "Test for type generation for query parameters with referenced schemas")
    public void testParameterSchemaReferences() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/schema_referenced_in_parameters.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);

        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/parameter_schema_refs.bal", syntaxTree);
    }

    @Test(description = "Test Ballerina types generation when referred by another record with no additional fields")
    public void testReferredTypesWithoutAdditionalFields() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/referred_inclusion.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/referred_inclusion.bal", syntaxTree);
    }

    @Test(description = "Test doc comment generation of record fields when property is reffered to another schema")
    public void testDocCommentResolvingForRefferedSchemas() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/resolve_reference_docs.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/resolve_reference_docs.bal", syntaxTree);
    }

    @Test(description = "Test for type generation for request body with reference")
    public void testRequestBodyReferences() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/request_body_with_ref.yaml"), true);
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator(openAPI), new ArrayList<>(), false);
        FunctionSignatureNode signature1 = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets").getPost(), new ArrayList<>());
        FunctionSignatureNode signature2 = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/pets/dogs").getPost(), new ArrayList<>());
        List<TypeDefinitionNode> preGeneratedTypeDefNodes = new ArrayList<>(
                functionSignatureGenerator.getTypeDefinitionNodeList());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI,
                false, preGeneratedTypeDefNodes);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/schema_with_request_body_ref.bal", syntaxTree);
    }
}
