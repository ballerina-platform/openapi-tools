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
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for SwaggerParser.
 */
public class SwaggerFileParserTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    @Test(description = "Test invalid file path",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI contract doesn't exist in the given .*")
    public void testInvalidFilePath() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(RES_DIR.resolve("user.yaml"));
    }

    //TODO: expectedExceptionsMessageRegExp = "Invalid file type.*"
    @Test(description = "Test invalid file type",
            expectedExceptions = BallerinaOpenApiException.class)
    public void testInvalidFileType() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(RES_DIR.resolve("swagger/petstore.txt"));
    }

    //TODO expectedExceptionsMessageRegExp = "OpenAPI file has errors: .*"
    @Test(description = "Test invalid swagger file ",
            expectedExceptions = BallerinaOpenApiException.class)
    public void testInvalidFile() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(RES_DIR.resolve("swagger/invalid.yaml"));
    }

    @Test(description = "Test swagger file has undocumented reference in schema.",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: \n" +
                    "attribute components.schemas.Person.Person01 is not of type `schema`.*")
    public void testForUndocumentedReference() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/undocument_ref.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
    }
}
