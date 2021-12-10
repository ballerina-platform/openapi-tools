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
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This test class contains the all the tests related to headers.
 */
public class HeaderParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/service").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;
//resource function get pets(@http:Header {name:"x-request-id"} string headers) {}
    @Test(description = "01. Required header parameter.")
    public void requiredHeader() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_null_required.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_null_required.bal", syntaxTree);
    }

    @Test(description = "02. Required header parameter has nullable true property")
    public void requiredHeaderWithNullableTrue() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/headers/header_null_required.yaml");
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(definitionPath);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPI, filter);
        syntaxTree = ballerinaServiceGenerator.generateSyntaxTree();
        System.out.println(Formatter.format(syntaxTree));
        CommonTestFunctions.compareGeneratedSyntaxTreewithExpectedSyntaxTree("headers/header_null_required.bal", syntaxTree);
    }
}
