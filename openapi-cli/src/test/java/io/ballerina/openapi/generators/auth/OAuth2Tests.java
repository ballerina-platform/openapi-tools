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
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * All the tests related to the auth related code snippet generation for http or oauth 2.0 mechanisms.
 */
public class OAuth2Tests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/auth").toAbsolutePath();

    @Test(description = "Generate config record for OAuth 2.0 authorization code flow",
            dataProvider = "oAuth2IOProvider")
    public void testGetConfigRecord(String yamlFile, String configRecord) throws IOException,
            BallerinaOpenApiException, ClientException {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(false,
                true);
        Path definitionPath = RES_DIR.resolve("scenarios/oauth2/" + yamlFile);
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
        ballerinaAuthConfigGenerator.setAuthTypes(securitySchemeMap);
        String expectedConfigRecord = configRecord;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.generateConnectionConfigRecord()).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }


    @Test(description = "Test the generation of Config params in class init function signature",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetConfigParamForClassInit() {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(false,
                true);
        String expectedParams = TestConstants.HTTP_CLIENT_CONFIG_PARAM;
        StringBuilder generatedParams = new StringBuilder();
        List<ParameterNode> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit();
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }

    @Test(description = "Test the generation of http:Client init node",
            dependsOnMethods = {"testGetConfigRecord"})
    public void testGetClientInitializationNode() {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(false,
                true);
        String expectedParam = TestConstants.HTTP_CLIENT_DECLARATION;
        VariableDeclarationNode generatedInitParamNode = ballerinaAuthConfigGenerator.getClientInitializationNode();
        expectedParam = (expectedParam.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedInitParamNode.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParam, generatedParamsStr);
    }

    @DataProvider(name = "oAuth2IOProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"oauth2_authorization_code.yaml", TestConstants.OAUTH2_CUSTOM_AUTHORIZATION_CODE_CONFIG_REC},
                {"oauth2_authorization_code_without_tokenurl.yaml", TestConstants.OAUTH2_AUTHORIZATION_CODE_CONFIG_REC},
                {"oauth2_client_credential.yaml", TestConstants.OAUTH2_CUSTOM_CLIENT_CRED_CONFIG_REC},
                {"oauth2_client_credential_without_tokenurl.yaml", TestConstants.OAUTH2_CLIENT_CRED_CONFIG_REC},
                {"oauth2_implicit.yaml", TestConstants.OAUTH2_IMPLICIT_CONFIG_REC},
                {"oauth2_password.yaml", TestConstants.OAUTH2_CUSTOM_PASSWORD_CONFIG_REC},
                {"oauth2_password_without_tokenurl.yaml", TestConstants.OAUTH2_PASSWORD_CONFIG_REC},
                {"oauth2_multipleflows.yaml", TestConstants.OAUTH2_MULTI_FLOWS_CONFIG_REC}
        };
    }
}
