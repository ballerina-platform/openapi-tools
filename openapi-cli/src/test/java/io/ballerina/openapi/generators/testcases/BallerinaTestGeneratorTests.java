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
package io.ballerina.openapi.generators.testcases;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.BallerinaCodeGenerator;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.cmd.CmdConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_DIR;

/**
 * Test cases related to ballerina test skeleton generation.
 */
public class BallerinaTestGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/test_cases/").toAbsolutePath();
    private static final Path PROJECT_DIR = RES_DIR.resolve("ballerina_project");
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path utilPath = RES_DIR.resolve("ballerina_project/utils.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    private static final Path testPath = RES_DIR.resolve("ballerina_project/tests/test.bal");
    private static final Path configPath = RES_DIR.resolve("ballerina_project/tests/Config.toml");

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Generate Client with test skelotins", dataProvider = "httpAuthIOProvider")
    public void generateclientWithTestSkel(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException, BallerinaOpenApiException, URISyntaxException {
        Files.createDirectories(Paths.get(PROJECT_DIR + OAS_PATH_SEPARATOR + TEST_DIR));
        Path definitionPath = RES_DIR.resolve("sample_yamls/" + yamlFile);
        BallerinaCodeGenerator codeGenerator = new BallerinaCodeGenerator();
        codeGenerator.setIncludeTestFiles(true);
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false, false);
        BallerinaTypesGenerator schemaGenerator = new BallerinaTypesGenerator(openAPI);
        schemaGenerator.setTypeDefinitionNodeList(ballerinaClientGenerator.getTypeDefinitionNodeList());
        BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(ballerinaClientGenerator);
        SyntaxTree syntaxTreeClient = ballerinaClientGenerator.generateSyntaxTree();
        SyntaxTree syntaxTreeTest = ballerinaTestGenerator.generateSyntaxTree();
        SyntaxTree syntaxTreeSchema = schemaGenerator.generateSyntaxTree();
        SyntaxTree utilSyntaxTree = ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree();
        String configFile = ballerinaTestGenerator.getConfigTomlFile();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTreeClient, syntaxTreeTest,
                syntaxTreeSchema, configFile, utilSyntaxTree);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    public List<Diagnostic> getDiagnostics(SyntaxTree clientSyntaxTree, SyntaxTree testSyntaxTree,
                                           SyntaxTree schemaSyntaxTree, String configContent, SyntaxTree utilSyntaxTree)
            throws FormatterException, IOException {
        TestUtils.writeFile(clientPath, Formatter.format(clientSyntaxTree).toString());
        TestUtils.writeFile(utilPath, Formatter.format(utilSyntaxTree).toString());
        TestUtils.writeFile(schemaPath, Formatter.format(schemaSyntaxTree).toString());
        TestUtils.writeFile(testPath, Formatter.format(testSyntaxTree).toString());
        TestUtils.writeFile(configPath, configContent);
        SemanticModel semanticModel = TestUtils.getSemanticModel(clientPath);
        return semanticModel.diagnostics();
    }

    @AfterMethod
    public void afterTest() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
            Files.deleteIfExists(utilPath);
            Files.deleteIfExists(testPath);
            Files.deleteIfExists(configPath);
        } catch (IOException ignored) {
        }
    }

    @DataProvider(name = "httpAuthIOProvider")
    public Object[] dataProvider() {
        return new Object[]{
                "basic_auth.yaml",
                "bearer_auth.yaml",
                "oauth2_authorization_code.yaml",
                "oauth2_implicit.yaml",
                "query_api_key.yaml",
                "no_auth.yaml",
                "combination_of_apikey_and_http_oauth.yaml",
                "oauth2_password.yaml"
        };
    }
}
