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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;
import static io.ballerina.openapi.generators.common.TestUtils.getOpenAPI;

/**
 * Test cases for generating ballerina parameters for openapi parameters with enum schemas.
 */
public class EnumGenerationTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    @Test(description = "Tests for all the enum scenarios in remote function parameter generation:" +
            "Use case 01 : Enum in query parameter" +
            "Use case 02 : Enums in path parameter" +
            "Use case 03 : Enum in header parameter" +
            "Use case 04 : Enum in reusable parameter" +
            "Use case 05 : Enum in parameter with referenced schema")
    public void generateRemoteParametersWithEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_enum.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/parameters_with_enum.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Tests for all the nullable enum scenarios in remote function parameter generation:" +
            "Use case 01 : Nullable enum in query parameter" +
            "Use case 02 : Nullable enum in path parameter" +
            "Use case 03 : Nullable enum in header parameter" +
            "Use case 04 : Nullable enum in reusable parameter" +
            "Use case 05 : Nullable enum in parameter with referenced schema")
    public void generateRemoteParametersWithNullableEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_nullable_enums.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/parameters_with_nullable_enums.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Tests for all the enum scenarios in resource function parameter generation:" +
            "Use case 01 : Enum in query parameter" +
            "Use case 02 : Enums in path parameter" +
            "Use case 03 : Enum in header parameter" +
            "Use case 04 : Enum in reusable parameter" +
            "Use case 05 : Enum in parameter with referenced schema")
    public void generateResourceParametersWithEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_enum.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/paramters_with_enum_resource.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Tests for all the nullable enum scenarios in resource function parameter generation:" +
            "Use case 01 : Nullable enum in query parameter" +
            "Use case 02 : Nullable enum in path parameter" +
            "Use case 03 : Nullable enum in header parameter" +
            "Use case 04 : Nullable enum in reusable parameter" +
            "Use case 05 : Nullable enum in parameter with referenced schema")
    public void generateResourceParametersWithNullableEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_nullable_enums.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/parameters_with_nullable_enums_resource.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test unsupported nullable path parameter with enums",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Path parameter value cannot be null.")
    public void testNestedArrayQueryParamGeneration() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RES_DIR.resolve("swagger/path_param_nullable_enum.yaml"));
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
    }
    @AfterMethod
    private void deleteGeneratedFiles() {
        try {
            TestUtils.deleteGeneratedFiles();
        } catch (IOException ignored) {
        }
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }
}
