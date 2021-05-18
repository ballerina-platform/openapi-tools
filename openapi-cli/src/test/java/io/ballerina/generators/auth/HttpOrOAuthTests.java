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

package io.ballerina.generators.auth;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static io.ballerina.generators.GeneratorUtils.getBallerinaOpenApiType;

/**
 * All the tests related to the auth related code snippet generation for http or oauth 2.0 mechanisms.
 */
public class HttpOrOAuthTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/auth").toAbsolutePath();

    @Test(description = "Generate config record for OAuth 2.0 authorization code flow", enabled = true)
    public void testgetConfigRecordAuthorizationCode() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/oauth2_authrization_code.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }


    @Test(description = "Test the generation of Config params in class init function signature",
            dependsOnMethods = {"testgetConfigRecordAuthorizationCode"}, enabled = true)
    public void testgetConfigParamForClassInit() {
        String expectedParams = "ClientConfig clientConfig";
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = BallerinaHTTPAuthGenerator.getConfigParamForClassInit();
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }

    @Test(description = "Test the generation of SSL init node",
            dependsOnMethods = {"testgetConfigRecordAuthorizationCode"}, enabled = true)
    public void testgetSecureSocketInitNode() {
        String expectedParam = "http:ClientSecureSocket? secureSocketConfig = clientConfig?.secureSocketConfig;";
        VariableDeclarationNode generatedInitParamNode = BallerinaHTTPAuthGenerator.getSecureSocketInitNode();
        expectedParam = (expectedParam.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedInitParamNode.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParam, generatedParamsStr);
    }

    @Test(description = "Test the generation of http:Client init node",
            dependsOnMethods = {"testgetConfigRecordAuthorizationCode"}, enabled = true)
    public void testgetClientInitializationNode() {
        String expectedParam = "http:Client httpEp = check new (serviceUrl, { " +
                "auth: clientConfig.authConfig, " +
                "secureSocket: secureSocketConfig });";
        VariableDeclarationNode generatedInitParamNode = BallerinaHTTPAuthGenerator.getClientInitializationNode();
        expectedParam = (expectedParam.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedInitParamNode.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParam, generatedParamsStr);
    }

    @Test(description = "Generate config record for http basic auth", enabled = true)
    public void testgetConfigRecordBasicAuth() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/basic_auth.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:CredentialsConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Generate config record for http bearer auth", enabled = true)
    public void testgetConfigRecordBearerAuth() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/http_bearer.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:BearerTokenConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Generate config record for oauth 2.0 client credentials flow", enabled = true)
    public void testgetConfigRecordClientCredentialAuth() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/oauth2_client_credential.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:OAuth2ClientCredentialsGrantConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Generate config record for oauth 2.0 password flow", enabled = true)
    public void testgetConfigRecordPasswordAuth() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/oauth2_password.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:OAuth2PasswordGrantConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Generate config record for oauth 2.0 multiple flows configured", enabled = true)
    public void testgetConfigRecordMultipleFlows() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("scenarios/oauth2_multipleflows.yaml");
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        String expectedConfigRecord = "public type ClientConfig record {\n" +
                "    http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig|http:CredentialsConfig authConfig;\n" +
                "    http:ClientSecureSocket secureSocketConfig?;\n" +
                "};";
        String generatedConfigRecord = Objects.requireNonNull(
                BallerinaHTTPAuthGenerator.getConfigRecord(openAPI)).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }
}
