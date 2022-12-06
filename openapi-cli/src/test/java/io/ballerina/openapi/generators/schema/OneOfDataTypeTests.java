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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.schema.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.TypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.UnionTypeGenerator;
import io.ballerina.openapi.core.generators.schema.model.GeneratorMetaData;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test implementation to verify the `anyOf` property related scenarios in openAPI schema generation, handled by
 * the {@link BallerinaTypesGenerator}.
 */
public class OneOfDataTypeTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test(description = "Generate record for schema has oneOF")
    public void generateForSchemaHasOneOf() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario12.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        Schema<?> schema = openAPI.getComponents().getSchemas().get("Error");
        ComposedSchema composedSchema = (ComposedSchema) schema;
        GeneratorMetaData.createInstance(openAPI, false);
        UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(composedSchema, "Error");
        String oneOfUnionType = unionTypeGenerator.generateTypeDescriptorNode().toString().trim();

        Assert.assertEquals(oneOfUnionType, "Activity|Profile");
    }

    @Test(description = "Generate record for schema has object type with OneOf")
    public void generateForSchemaObjectType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario13.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        Schema<?> schema = openAPI.getComponents().getSchemas().get("Error");
        ComposedSchema composedSchema = (ComposedSchema) schema;
        GeneratorMetaData.createInstance(openAPI, false);
        UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(composedSchema, "Error");
        String oneOfUnionType = unionTypeGenerator.generateTypeDescriptorNode().toString().trim();
        Assert.assertEquals(oneOfUnionType, "Activity|Profile01");
    }

    @Test(description = "Generate union type when nullable is true")
    public void generateUnionTypeWhenNullableTrue() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario12.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        Schema<?> schema = openAPI.getComponents().getSchemas().get("Error");
        GeneratorMetaData.createInstance(openAPI, true);
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, "Error", null);
        String oneOfUnionType = typeGenerator.generateTypeDescriptorNode().toString().trim();
        Assert.assertEquals(oneOfUnionType, "Activity|Profile?");
    }

    @Test(description = "Tests full schema genrations with oneOf type")
    public void generateOneOFTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/oneOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/oneOf.bal", syntaxTree);
    }

    @Test(description = "Tests record generation for oneOf schemas with inline object schemas")
    public void oneOfWithInlineObject() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/oneOf_with_inline_schemas.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/oneOf_with_inline_schemas.bal", syntaxTree);
    }

    @Test(description = "Tests record generation for nested OneOf schema inside AllOf schema")
    public void oneOfWithNestedAllOf() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/nested_oneOf_with_allOf.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/nested_oneOf_with_allOf.bal", syntaxTree);
    }
}
