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
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for the primitive data type.
 */
public class PrimitiveDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    private SyntaxTree syntaxTree;
    private ByteArrayOutputStream outContent;
    CodeGenerator codeGenerator = new CodeGenerator();

    @BeforeTest
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));
    }

    @Test(description = "Generate single record")
    public void generateScenario01() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario01.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema01.bal", syntaxTree);
    }

    @Test(description = "Generate multiple record")
    public void generateScenario02() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario02.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema02.bal", syntaxTree);
    }

    @Test(description = "Scenario for missing DataType")
    public void generateMissingDatatype() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/missDataType.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String expected = "public type Pet record { #this is missing dataType anydata id; string name; decimal tag?; " +
                "string 'type?;};";
        Assert.assertTrue(syntaxTree.toString().trim().replaceAll("\\s+", "").
                contains(expected.trim().replaceAll("\\s+", "")));
    }

    @Test(description = "When the component schema has primitive data type instead of object schema")
    public void generateSchemaForPrimitiveData() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/schema_with_primitive.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema_with_primitive.bal",
                syntaxTree);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
    }
}
