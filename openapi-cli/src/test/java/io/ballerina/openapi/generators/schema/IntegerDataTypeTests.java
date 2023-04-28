/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for testing integer data types int32 and int64.
 */
public class IntegerDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    @DataProvider(name = "intTestData")
    public Object[][] intFormatTestData() {
        return new Object[][]{
                {"swagger/schema_integer_signed32.yaml", "schema/ballerina/schema_integer_signed32.bal"},
                {"swagger/schema_integer_signed32_ref.yaml", "schema/ballerina/schema_integer_signed32_ref.bal"},
                {"swagger/schema_integer_signed64.yaml", "schema/ballerina/schema_integer_signed64.bal"},
                {"swagger/schema_integer_invalid_format.yaml", "schema/ballerina/schema_integer_invalid_format.bal"},
                {"swagger/schema_integer_array.yaml", "schema/ballerina/schema_integer_array.bal"},
        };
    }

    @Test(dataProvider = "intTestData", description = "Tests valid schema integer value formats")
    public void testIntegerFormatTypeSchema(final String swaggerPath, final String balPath)
            throws IOException, BallerinaOpenApiException {

        final Path definitionPath = RES_DIR.resolve(swaggerPath);
        final OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        final BallerinaTypesGenerator ballerinaTypesGenerator = new BallerinaTypesGenerator(openAPI);
        final SyntaxTree syntaxTree = ballerinaTypesGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(balPath, syntaxTree);
    }
}
