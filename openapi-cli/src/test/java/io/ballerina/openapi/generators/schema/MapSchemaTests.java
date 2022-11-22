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
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is for containing the test related to the {@code MapSchema}.
 */
public class MapSchemaTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    @Test
    public void testForAdditionalProperties() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/additional_properties_true.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/additional_properties_true.bal", syntaxTree);
    }

    @Test(description = "Negative test for additionalProperty mapping",
    expectedExceptions = BallerinaOpenApiException.class,
    expectedExceptionsMessageRegExp = "OpenAPI definition has errors: \n" +
            "\n" +
            "attribute components.schemas.User02.additionalProperties.*")
    public void negativeTestForAdditionalPropertiesWithParserIssue() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/additional_properties_true_negative.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
    }

    @Test
    public void negativeTestForAdditionalPropertiesWithoutParserIssue() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/additional_properties_true_negative_without_parser_issue.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/additional_properties_negative.bal", syntaxTree);
    }
}
