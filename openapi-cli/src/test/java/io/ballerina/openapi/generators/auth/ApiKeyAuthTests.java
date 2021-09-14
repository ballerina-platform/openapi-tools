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

package io.ballerina.openapi.generators.auth;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.client.BallerinaAuthConfigGenerator;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * All the tests related to the auth related code snippet generation for api key auth mechanism.
 */
public class ApiKeyAuthTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, false);

    @Test(description = "Generate config record for openweathermap api", dataProvider = "apiKeyAuthIOProvider")
    public void testGetConfigRecord(String yamlFile) throws IOException, BallerinaOpenApiException {
        // generate ApiKeysConfig record
        GeneratorUtils generatorUtils = new GeneratorUtils();
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/" + yamlFile);
        OpenAPI openAPI = generatorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String expectedConfigRecord = TestConstants.API_KEY_CONFIG_REC;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Test the generation of ApiKey map local variable",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetApiKeyMapClassVariable () {
        String expectedClassVariable = TestConstants.API_KEY_MAP_VAR;
        String generatedClassVariable = ballerinaAuthConfigGenerator.getApiKeyMapClassVariable().toString();
        generatedClassVariable = (generatedClassVariable.trim()).replaceAll("\\s+", "");
        expectedClassVariable = (expectedClassVariable.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedClassVariable, generatedClassVariable);
    }

    @Test(description = "Test the generation of api key related parameters in class init function signature",
            dependsOnMethods = {"testGetApiKeyMapClassVariable"})
    public void testGetConfigParamForClassInit() {
        String expectedParams = TestConstants.API_KEY_CONFIG_PARAM;
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
                "https:localhost/8080");
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }

    @Test(description = "Test the generation of api key assignment node",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetApiKeyAssignmentNode () {
        String expectedAssignmentNode = TestConstants.API_KEY_ASSIGNMENT;
        String generatedAssignmentNode = Objects.requireNonNull
                (ballerinaAuthConfigGenerator.getApiKeyAssignmentNode()).toString();
        generatedAssignmentNode = (generatedAssignmentNode.trim()).replaceAll("\\s+", "");
        expectedAssignmentNode = (expectedAssignmentNode.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedAssignmentNode, generatedAssignmentNode);
    }

    @Test(description = "Test the generation of api key documentation comment")
    public void testGetApiKeyDescription () throws IOException, BallerinaOpenApiException {
        GeneratorUtils generatorUtils = new GeneratorUtils();
        Path definitionPath = RES_DIR.resolve("auth/scenarios/api_key/custome_api_key_doc.yaml");
        OpenAPI openAPI = generatorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String expectedApiKeyDescription = TestConstants.API_KEY_DOC_COMMENT;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        String generateApiKeyDescription = ballerinaAuthConfigGenerator.getApiKeyDescription();
        generateApiKeyDescription = (generateApiKeyDescription.trim()).replaceAll("\\s+", "");
        expectedApiKeyDescription = (expectedApiKeyDescription.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedApiKeyDescription, generateApiKeyDescription);
    }

    @DataProvider(name = "apiKeyAuthIOProvider")
    public Object[] dataProvider() {
        return new Object[]{
                "header_api_key.yaml",
                "query_api_key.yaml",
                "multiple_api_keys.yaml",
                "header_api_key_only.yaml"
        };
    }
}
