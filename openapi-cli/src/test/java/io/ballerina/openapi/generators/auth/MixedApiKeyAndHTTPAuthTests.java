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
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestConstants;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * All the tests related to the auth related code snippet generation for api key auth mechanism.
 */
public class MixedApiKeyAndHTTPAuthTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/").toAbsolutePath();
    List<Diagnostic> diagnostics = new ArrayList<>();
    AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(true, true, diagnostics);

    @Test(description = "Generate ApiKeysConfig record", dataProvider = "apiKeyAuthIOProvider")
    public void testGetConfigRecord(String yamlFile) throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/" + yamlFile);
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        ballerinaAuthConfigGenerator.addAuthRelatedRecords(openAPI);
        List<TypeDefinitionNode> authRelatedTypeDefinitionNodes =
                ballerinaAuthConfigGenerator.getAuthRelatedTypeDefinitionNodes();
        Optional<TypeDefinitionNode> connectionConfig = authRelatedTypeDefinitionNodes.stream()
                .filter(typeDefinitionNode -> typeDefinitionNode.typeName().text().equals("ConnectionConfig"))
                .findFirst();
        if (connectionConfig.isPresent()) {
            String expectedRecord = TestConstants.CONNECTION_CONFIG_MIXED_AUTH;
            String generatedRecord = connectionConfig.get().toString();
            generatedRecord = (generatedRecord.trim()).replaceAll("\\s+", "");
            expectedRecord = (expectedRecord.trim()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedRecord, expectedRecord);
        } else {
            Assert.fail();
        }
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
        List<ParameterNode> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit();
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
