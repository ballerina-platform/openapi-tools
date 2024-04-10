/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.auth;

import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * This class contains all the tests related to the ConnectionConfig record generation when
 * x-ballerina-http-config is defined.
 */
public class HttpConfigurationExtensionTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/auth").toAbsolutePath();

    @Test(description = "Generate config record when http version is given")
    public void testGetConfigRecordGeneration() throws IOException, BallerinaOpenApiException, ClientException {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(
                false, false);
        Path definitionPath = RES_DIR.resolve("scenarios/http_config_extension/petstore_with_http_version.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        ballerinaAuthConfigGenerator.addAuthRelatedRecords(openAPI);
        String expectedConfigRecord = TestConstants.CONNECTION_CONFIG_HTTP_VERSION_1_1;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.generateConnectionConfigRecord()).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }

    @Test(description = "Generate config record when invalid http version is given")
    public void testGetConfigRecordGenerationForInvalidHTTPVersion() throws IOException, BallerinaOpenApiException,
            ClientException {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(
                false, false);
        Path definitionPath = RES_DIR.resolve(
                "scenarios/http_config_extension/petstore_with_invalid_http_version.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        ballerinaAuthConfigGenerator.addAuthRelatedRecords(openAPI);
        String expectedConfigRecord = TestConstants.CONNECTION_CONFIG_NO_AUTH;
        String generatedConfigRecord = Objects.requireNonNull(
                ballerinaAuthConfigGenerator.generateConnectionConfigRecord()).toString();
        generatedConfigRecord = (generatedConfigRecord.trim()).replaceAll("\\s+", "");
        expectedConfigRecord = (expectedConfigRecord.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedConfigRecord, generatedConfigRecord);
    }
}
