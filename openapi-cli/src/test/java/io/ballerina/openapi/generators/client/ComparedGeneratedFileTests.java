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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;

/**
 * All the tests related to the {{@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}} util.
 */
public class ComparedGeneratedFileTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    private static final Path utilsPath = RES_DIR.resolve("ballerina_project/utils.bal");

    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Generate Client for path parameter has parameter name as key word", enabled = false)
    public void generateClientForJira() throws IOException, BallerinaOpenApiException,
            OASTypeGenException,
            FormatterException, URISyntaxException, ClientException {
//        Path definitionPath = RES_DIR.resolve("swagger/request_body_oneOf_scenarios.yaml");
        Path definitionPath = RES_DIR.resolve("openapi.yaml");
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/jira_openapi.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test openAPI definition to ballerina client source code generation",
            dataProvider = "fileProviderForFilesComparison")
    public void  openApiToBallerinaCodeGenTestForClient(String yamlFile, String expectedFile) throws IOException,
            BallerinaOpenApiException, OASTypeGenException, FormatterException, ClientException {
        Path definitionPath = RES_DIR.resolve("file_provider/swagger/" + yamlFile);
        Path expectedPath = RES_DIR.resolve("file_provider/ballerina/" + expectedFile);
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<TypeDefinitionNode> authNodes = ballerinaClientGenerator.getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, schemaSyntaxTree, ballerinaClientGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @DataProvider(name = "fileProviderForFilesComparison")
    public Object[][] fileProviderForFilesComparison() {
        return new Object[][]{
//                {"openapi_weather_api.yaml", "openapi_weather_api.bal"}
//                {"uber_openapi.yaml", "uber_openapi.bal"},
//                {"multiple_pathparam.yaml", "multiple_pathparam.bal"},
//                {"display_annotation.yaml", "display_annotation.bal"},
//                {"api2pdf.yaml", "api2pdf.bal"},
//                {"nillable_response.yaml", "nillable_response.bal"},
//                {"nillable_union_response.yaml", "nillable_union_response.bal"},
//                {"duplicated_response.yaml", "duplicated_response.bal"},
//                {"multiline_param_comment.yaml", "multiline_param_comment.bal"},
//                {"description_with_special_characters.yaml", "description_with_special_characters.bal"}, //special characters in description
//                {"header_with_enum.yaml", "header_with_enum.bal"},
//                {"incorrect_format.yaml", "incorrect_format.bal"},
//                {"format_types_v3_0.yaml", "format_types_v3_0.bal"},
//                {"format_types_v3_1.yaml", "format_types_v3_1.bal"}
        };
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            TestUtils.deleteGeneratedFiles();
        } catch (IOException ignored) {
        }
    }
}
