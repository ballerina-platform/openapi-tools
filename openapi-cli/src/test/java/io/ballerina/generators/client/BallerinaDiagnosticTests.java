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

package io.ballerina.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.generators.common.TestUtils.getDiagnostics;

/**
 * All the tests related to the check diagnostic issue in code generators
 * {@link BallerinaClientGenerator} util.
 */
public class BallerinaDiagnosticTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/client/diagnostic_files").toAbsolutePath();
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test openAPI definition to ballerina client source code generation with diagnostic issue",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssues(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    @DataProvider(name = "singleFileProviderForDiagnosticCheck")
    public Object[][] singleFileProviderForDiagnosticCheck() {
        return new Object[][] {
                {"petstore_server_with_base_path.yaml"},
                {"petstore_without_operation_id.yaml"},
                {"petstore_get.yaml"},
                {"openapi_display_annotation.yaml"},
                {"header_parameter.yaml"},
                {"petstore_post.yaml"},
                {"petstore_with_oneOf_response.yaml"}
        };
    }
}
