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
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
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

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;

/**
 * All the tests related to the check diagnostic issue in code generators
 * {{@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}} util.
 */
public class BallerinaDiagnosticTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/diagnostic_files").toAbsolutePath();
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test openAPI definition to ballerina client source code generation with remote functions",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssuesWithRemoteFunctions(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false)
                .build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @Test(description = "Test openAPI definition to ballerina client source code generation with resource functions",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssuesWithResourceFunctions(String yamlFile) throws IOException,
            BallerinaOpenApiException, FormatterException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true)
                .build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @DataProvider(name = "singleFileProviderForDiagnosticCheck")
    public Object[][] singleFileProviderForDiagnosticCheck() {
        return new Object[][] {
                {"petstore_server_with_base_path.yaml"},
                {"petstore_get.yaml"},
                {"openapi_display_annotation.yaml"},
                {"header_parameter.yaml"},
                {"petstore_post.yaml"},
                {"petstore_with_oneOf_response.yaml"},
                {"response_nested_array.yaml"},
                {"xml_payload.yaml"},
                {"xml_payload_with_ref.yaml"},
                {"duplicated_response.yaml"},
                {"complex_oneOf_schema.yaml"},
                {"request_body_ref.yaml"},
                {"vendor_specific_mime_types.yaml"},
                {"ballerinax_connector_tests/ably.yaml"},
                {"ballerinax_connector_tests/azure.iot.yaml"},
                {"ballerinax_connector_tests/beezup.yaml"},
                {"ballerinax_connector_tests/files.com.yaml"},
                {"ballerinax_connector_tests/openweathermap.yaml"},
                {"ballerinax_connector_tests/soundcloud.yaml"},
                {"ballerinax_connector_tests/stripe.yaml"},
                {"ballerinax_connector_tests/vimeo.yaml"},
                {"ballerinax_connector_tests/ynab.yaml"},
                {"ballerinax_connector_tests/zoom.yaml"}
        };
    }

    @AfterTest
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }
}
