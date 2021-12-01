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
import java.util.List;
import java.util.Objects;

/**
 * All the tests related to the auth related code snippet generation for api key auth mechanism.
 */
public class MixedApiKeyAndHTTPAuthTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/").toAbsolutePath();
    BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(true, true);

    @Test(description = "Generate ApiKeysConfig record", dataProvider = "apiKeyAuthIOProvider")
    public void testGetConfigRecord(String yamlFile) throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/" + yamlFile);
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getConfigRecord(openAPI)).toString();
        Assert.assertFalse(generatedConfigRecord.isBlank());
    }

    @Test(description = "Generate AuthConfig record")
    public void testGetAuthConfigRecord() {
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.getAuthConfigRecord()).toString();
        Assert.assertTrue(generatedConfigRecord.contains(TestConstants.authConfigRecordDoc));
        Assert.assertTrue(generatedConfigRecord.contains("|ApiKeysConfigauth;"));
    }

    @Test(description = "Test the generation of nillable ApiKeysConfig class variable",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetApiKeyMapClassVariable () {
        String expectedClassVariable = TestConstants.API_KEY_CONFIG_NILLABLE_VAR;
        String generatedClassVariable = ballerinaAuthConfigGenerator.getApiKeyMapClassVariable().toString();
        generatedClassVariable = (generatedClassVariable.trim()).replaceAll("\\s+", "");
        expectedClassVariable = (expectedClassVariable.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedClassVariable, expectedClassVariable);
    }

    @Test(description = "Test the generation of init function signature",
            dependsOnMethods = {"testGetApiKeyMapClassVariable"})
    public void testGetConfigParamForClassInit() {
        String expectedParams = TestConstants.AUTH_CONFIG_PARAM;
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
                "https:localhost/8080");
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(generatedParamsStr, expectedParams);
    }

    @DataProvider(name = "apiKeyAuthIOProvider")
    public Object[] dataProvider() {
        return new Object[]{
                "combination_of_apikey_and_http_oauth.yaml"
        };
    }
}
