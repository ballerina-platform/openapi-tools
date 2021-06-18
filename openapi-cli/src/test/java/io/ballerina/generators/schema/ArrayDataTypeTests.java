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
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.generators.common.TestUtils;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for all the possible scenarios with array type in the swagger component schema.
 */
public class ArrayDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator();

    @BeforeTest
    public void setUp() {
        System.setErr(new PrintStream(outContent));
    }

    @Test(description = "Generate record with array filed record")
    public void generateRecordWithArrayField() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario03.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema03.bal", syntaxTree);
    }

    @Test(description = "Scenario04-Generate record with nested array filed record")
    public void generateScenario04() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario04.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema04.bal", syntaxTree);
    }

    @Test(description = "Generate record with record type array filed record")
    public void generateRecordWithRecordArrayField() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario06.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema06.bal", syntaxTree);
    }


    @Test(description = "Generate record for schema has array reference")
    public void generateSchemaHasArrayReference() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario08.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema08.bal", syntaxTree);
    }

    @Test(description = "Generate Array for schema has array reference")
    public void generateSchemaArrayReference() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/schema_with_array.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema_with_array.bal",
                syntaxTree);
    }

    @Test(description = "Generate Array for schema has array reference")
    public void generateSchemaNestedArrayReference() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/schema_with_nested_array.yaml");
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema_with_nested_array.bal",
                syntaxTree);
    }
    @AfterTest
    public void tearDown() {
        System.setErr(null);
    }
}
