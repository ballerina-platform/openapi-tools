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

import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree;

/**
 * Tests for Special scenarios in schema handling.
 */
public class AdvanceRecordTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;
    CodeGenerator codeGenerator = new CodeGenerator();


    // Enable after adding `not` data bind support
    @Test(description = "Generate record for schema has not type", enabled = false)
    public void generateSchemaHasNotType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario10.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema10.bal", syntaxTree);
    }

    @Test(description = "Generate record for schema has inline record in fields reference")
    public void generateSchemaHasInlineRecord() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario11.yaml");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema11.bal", syntaxTree);
    }

    @Test(description = "Generate record for openapi weather api", enabled = false)
    public void generateOpenAPIWeatherAPI() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_weather_api.yaml");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/openapi_weather_api_schema.bal", syntaxTree);
    }

    @Test(description = "Generate record for schema has object type only")
    public void generateForSchemaHasObjectTypeOnly() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario14.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        Schema schema = openAPI.getComponents().getSchemas().get("Error");
        ObjectSchema objectSchema = (ObjectSchema) schema;
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        TypeDefinitionNode recordNode =
                ballerinaSchemaGenerator.getTypeDefinitionNode(objectSchema, "Error", new ArrayList<>());
        Assert.assertTrue(((RecordTypeDescriptorNode) recordNode.typeDescriptor()).fields().isEmpty());
    }
}
