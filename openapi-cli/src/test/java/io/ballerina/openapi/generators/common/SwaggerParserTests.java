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

package io.ballerina.openapi.generators.common;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.ServiceGenerationHandler;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtils.FILTER;

/**
 * Tests for BallerinaSchemaGenerators.
 */
public class SwaggerParserTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;

    //TODO: readd expectedExceptionsMessageRegExp = "OpenAPI file has errors: .*" after checking failure
    @Test(description = "Functionality tests for getBallerinaOpenApiType",
            expectedExceptions = BallerinaOpenApiException.class)
    public void generateHandleUnsupportedData() throws  IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/invalid.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
//        syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
    }

    @Test(description = "Functionality tests for swagger parser behaviour when the regex is having syntax errors",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: .*")
    public void testInvalidRegexPatterns() throws  IOException, BallerinaOpenApiException,
            OASTypeGenException {
        Path definitionPath = RES_DIR.resolve("swagger/invalid_pattern_string.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
//        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
//        syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
    }

    @Test(description = "Nullable array and object schemas with a null default value are valid OpenAPI 3.0 schemas",
            dataProvider = "nullableDefaultNullSpecs")
    public void testNullableSchemaWithNullDefault(String specFile) throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(RES_DIR.resolve(specFile));
        Assert.assertNotNull(openAPI.getComponents().getSchemas().get("Tags"));
        Assert.assertNotNull(openAPI.getComponents().getSchemas().get("Labels"));
        Assert.assertNotNull(openAPI.getComponents().getSchemas().get("Holder"));
    }

    @DataProvider(name = "nullableDefaultNullSpecs")
    public Object[][] nullableDefaultNullSpecs() {
        return new Object[][]{
                {"swagger/nullable_default_null.yaml"},
                {"swagger/nullable_default_null_v3_0_3.yaml"}
        };
    }

    @Test(description = "Types are generated for nullable schemas with a null default value")
    public void testTypeGenerationForNullableSchemaWithNullDefault() throws IOException,
            BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/nullable_default_null.yaml"),
                true, false);
        TypeHandler.createInstance(openAPI, false);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withNullable(false)
                .withFilters(FILTER)
                .build();
        serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        syntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        GeneratorTestUtils.assertGeneratedSyntaxTreeContainsExpectedSyntaxTree(
                "schema/ballerina/nullable_default_null.bal", syntaxTree);
    }

    @Test(description = "A null default value is still rejected when the schema is not nullable",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "(?s)OpenAPI definition has errors: .*" +
                    "attribute components.schemas.Tags.default is not of type `non-null array`.*")
    public void testNonNullableSchemaWithNullDefault() throws IOException, BallerinaOpenApiException {
        GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(RES_DIR.resolve("swagger/non_nullable_default_null.yaml"));
    }

    //Get string as a content of ballerina file
    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String s) throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("ballerina/"), s);
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }
}
