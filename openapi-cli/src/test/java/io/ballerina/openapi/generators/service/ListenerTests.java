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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * All the tests related to the {@code io.ballerina.openapi.generators.service.ListenerGenerator} util.
 */
public class ListenerTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;


    @Test(description = "Generate importors")
    public void generateImports() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/listeners/petstore_listeners.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners/importors.bal", syntaxTree);
    }

    @Test(description = "Generate listeners", expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Failed to read endpoint details of the server: /v1")
    public void generatelisteners() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/listeners/petstore_listeners02.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners/listeners.bal", syntaxTree);
    }

    @Test(description = "Generate listeners")
    public void generatelisteners02() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/listeners/petstore_listeners03.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners/listeners03.bal", syntaxTree);
    }

    @Test(description = "Generate listeners when the server url is there and variables are absent")
    public void generatelisteners03() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/listeners/petstore_listeners04.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners/listeners04.bal", syntaxTree);
    }

    @Test(description = "Generate listeners when the server url base path is absent")
    public void generatelisteners04() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/service/swagger/listeners/petstore_listeners05.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners/listeners05.bal", syntaxTree);
    }
}
