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

package io.ballerina.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.generators.common.TestUtils;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.generators.BallerinaSchemaGenerator.generateSyntaxTree;
import static io.ballerina.generators.GeneratorUtils.getOneOfUnionType;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to OneOF data binding handling the {@link io.ballerina.generators.BallerinaSchemaGenerator}
 * util.
 */
public class OneOfDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test(description = "Generate record for schema has oneOF")
    public void generateForSchemaHasOneOf() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario12.yaml");
        OpenAPI openAPI = getOpenAPI(definitionPath);
        Schema schema = openAPI.getComponents().getSchemas().get("Error");
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Schema> oneOf = composedSchema.getOneOf();
        String oneOfUnionType = getOneOfUnionType(oneOf);
        Assert.assertEquals(oneOfUnionType, "Activity|Profile");
    }

    @Test(description = "Generate record for schema has object type with OneOf")
    public void generateForSchemaObjectType() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario13.yaml");
        OpenAPI openAPI = getOpenAPI(definitionPath);
        Schema schema = openAPI.getComponents().getSchemas().get("Error");
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Schema> oneOf = composedSchema.getOneOf();
        String oneOfUnionType = getOneOfUnionType(oneOf);
        Assert.assertEquals(oneOfUnionType, "Activity|Profile01");
    }

    @Test(description = "Tests full schema genrations with oneOf type")
    public void generateOneOFTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/oneOf.yaml");
        SyntaxTree syntaxTree = generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/oneOf.bal", syntaxTree);
    }

}
