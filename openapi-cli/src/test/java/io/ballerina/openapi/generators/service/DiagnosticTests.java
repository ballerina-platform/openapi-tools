/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.BallerinaServiceGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnosticsForGenericService;

/**
 * Tests related to the check diagnostic issue in Ballerina service generation.
 */
public class DiagnosticTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/service/diagnostic_files").toAbsolutePath();
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for compilation errors in OpenAPI definition to ballerina service skeleton generation",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssues(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withNullable(false)
                .withGenerateServiceType(false)
                .withGenerateWithoutDataBinding(true)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree =  ballerinaServiceGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnosticsForGenericService(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @DataProvider(name = "singleFileProviderForDiagnosticCheck")
    public Object[][] singleFileProviderForDiagnosticCheck() {
        return new Object[][] {
                {"petstore_original.yaml"}
        };
    }

    @AfterTest
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }
}
