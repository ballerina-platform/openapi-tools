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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.generators.OpenApiException;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.generators.BallerinaSchemaGenerator.getTypeDefinitionNodeForObjectSchema;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * Tests for BallerinaSchemaGenerators.
 */
public class BallerinaSchemaGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    SyntaxTree syntaxTree;


    @Test(description = "Scenario01-Generate single record")
    public void generateScenario01() throws FormatterException, OpenApiException, IOException,
            BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario01.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema01.bal");
    }

    @Test(description = "Scenario02- Generate multiple record")
    public void generateScenario02() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario02.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema02.bal");
    }

    @Test(description = "Scenario03-Generate record with array filed record")
    public void generateScenario03() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario03.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema03.bal");
    }

    @Test(description = "Scenario04-Generate record with nested array filed record")
    public void generateScenario04() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario04.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema04.bal");
    }

    @Test(description = "Scenario05-Generate record with record type filed record")
    public void generateScenario05() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario05.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema05.bal");
    }

    @Test(description = "Scenario06 - Generate record with record type array filed record")
    public void generateScenario06() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario06.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema06.bal");
    }

    @Test(description = "Scenario07-Generate record with nested record type filed record")
    public void generateScenario07() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario07.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema07.bal");
    }

    @Test(description = "Scenario08-Generate record for schema has array reference")
    public void generateScenario08() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario08.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema08.bal");
    }
//check nested array -test
    @Test(description = "Scenario09-Generate record for schema has allOf reference")
    public void generateScenario09() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario09.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema09.bal");
    }

    @Test(description = "Scenario10-Generate record for schema has not type")
    public void generateScenario10() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario10.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema10.bal");
    }

    @Test(description = "Scenario11-Generate record for schema has inline record in fields reference")
    public void generateScenario11() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario11.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/schema11.bal");
    }

    @Test(description = "Generate record for openapi weather api")
    public void generateOpenAPIWeatherAPI() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/openapi_weather_api.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/openapi_weather_api_schema.bal");
    }

    @Test(description = "Scenario12-Generate record for schema has object type only")
    public void generateForSchemaHasObjectTypeOnly() throws IOException, BallerinaOpenApiException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("generators/schema/swagger/scenario14.yaml");
        OpenAPI openAPI = getOpenAPI(definitionPath);
        Schema schema = openAPI.getComponents().getSchemas().get("Error");
        ObjectSchema objectSchema = (ObjectSchema) schema;
        TypeDefinitionNode recordNode = getTypeDefinitionNodeForObjectSchema(null,
                        AbstractNodeFactory.createIdentifierToken("public type"),
                        AbstractNodeFactory.createIdentifierToken("Error"),
                        null, objectSchema.getProperties());
        Assert.assertTrue(((RecordTypeDescriptorNode) recordNode.typeDescriptor()).fields().isEmpty());
    }
    //Get string as a content of ballerina file
    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String s) throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("generators/ballerina"), s);
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }
}
