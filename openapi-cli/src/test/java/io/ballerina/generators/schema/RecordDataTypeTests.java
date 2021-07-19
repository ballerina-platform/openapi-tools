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
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests related to the record data structure.
 */
public class RecordDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;
    CodeGenerator codeGenerator = new CodeGenerator();

    @Test(description = "Generate record with record type filed record")
    public void generateRecordWithRecordField() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario05.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema05.bal", syntaxTree);
    }

    @Test(description = "Generate record with nested record type filed record")
    public void generateNestedRecord() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario07.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema07.bal", syntaxTree);
    }
}
