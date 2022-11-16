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
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.UnionTypeGenerator;
import io.ballerina.openapi.core.generators.schema.model.GeneratorMetaData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * All the tests related to AnyDataType handling the
 * {@link io.ballerina.openapi.generators.client.BallerinaClientGenerator} util.
 */
public class AnyOfDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();


    @Test(description = "Test for the schema has anyOf dataType")
    public void testAnyOfInSchema() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario15.yaml");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        Schema schema = openAPI.getComponents().getSchemas().get("AnyOF");
        ComposedSchema composedSchema = (ComposedSchema) schema;
        GeneratorMetaData.createInstance(openAPI, false);
        UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(composedSchema, "AnyOF");
        String anyOfUnionType = unionTypeGenerator.generateTypeDescriptorNode().toString().trim();
        Assert.assertEquals(anyOfUnionType, "User|Activity");
    }

    @Test(description = "Test for the schema generations")
    public void testAnyOfSchema() throws BallerinaOpenApiException, IOException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario15.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/schema15.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
}
