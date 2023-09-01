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
import io.swagger.v3.oas.models.SpecVersion;
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

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnosticsForService;
import static io.ballerina.openapi.generators.common.TestUtils.normalizeOpenAPI;

/**
 * Tests related to the check diagnostic issue in Ballerina service generation.
 */
public class ServiceDiagnosticTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/diagnostic_files").toAbsolutePath();
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
                .withGenerateWithoutDataBinding(false)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree =  ballerinaServiceGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnosticsForService(syntaxTree, openAPI, ballerinaServiceGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @Test(description = "Test for compilation errors in OpenAPI definition to ballerina service skeleton generation",
            dataProvider = "singleFileProviderForDiagnosticCheck")
    public void checkDiagnosticIssuesInGenericServiceGen(String yamlFile) throws IOException, BallerinaOpenApiException,
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
        List<Diagnostic> diagnostics = getDiagnosticsForService(syntaxTree, openAPI, ballerinaServiceGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @Test(description = "Test for compilation errors in OpenAPI 3.1.0 definition to ballerina service " +
            "skeleton generation",
            dataProvider = "fileProviderForOpenAPI31DiagnosticCheck")
    public void checkDiagnosticIssuesWith31OpenAPIs(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RESDIR.resolve(yamlFile);
        OpenAPI openAPI = normalizeOpenAPI(definitionPath, true, SpecVersion.V31);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(filter)
                .withNullable(false)
                .withGenerateServiceType(false)
                .withGenerateWithoutDataBinding(false)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        syntaxTree =  ballerinaServiceGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnosticsForService(syntaxTree, openAPI, ballerinaServiceGenerator);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        Assert.assertFalse(hasErrors);
    }

    @DataProvider(name = "singleFileProviderForDiagnosticCheck")
    public Object[][] singleFileProviderForDiagnosticCheck() {
        return new Object[][] {
                {"petstore_server_with_base_path.yaml"},
                {"petstore_get.yaml"},
                // TODO: Uncomment when fixed https://github.com/ballerina-platform/openapi-tools/issues/1416
//                {"openapi_display_annotation.yaml"}, // not working, unknown type '200
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
                {"single_allOf.yaml"},
                // TODO: Uncomment when fixed https://github.com/ballerina-platform/openapi-tools/issues/1415
//                {"ballerinax_connector_tests/ably.yaml"},
                {"ballerinax_connector_tests/azure.iot.yaml"},
               // TODO: Uncomment when fixed https://github.com/ballerina-platform/openapi-tools/issues/1440
//                {"ballerinax_connector_tests/beezup.yaml"},
                {"ballerinax_connector_tests/files.com.yaml"},
                {"ballerinax_connector_tests/openweathermap.yaml"},
                {"ballerinax_connector_tests/soundcloud.yaml"},
                {"ballerinax_connector_tests/stripe.yaml"},
                {"ballerinax_connector_tests/vimeo.yaml"},
//                {"ballerinax_connector_tests/ynab.yaml"}, // 209 status code is not supported in Ballerina
                {"ballerinax_connector_tests/zoom.yaml"}
        };
    }

    @DataProvider(name = "fileProviderForOpenAPI31DiagnosticCheck")
    public Object[][] fileProviderForOpenAPI31DiagnosticCheck() {
        return new Object[][]{
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
//                {"ballerinax_connector_tests/ably.yaml"}, // HTTP status code '2XX' is not supported in Ballerina
                {"ballerinax_connector_tests/azure.iot.yaml"},
                //TODO: Uncomment when fixed https://github.com/ballerina-platform/openapi-tools/issues/1440
//                {"ballerinax_connector_tests/beezup.yaml"},
                {"ballerinax_connector_tests/files.com.yaml"},
                {"ballerinax_connector_tests/openweathermap.yaml"},
                {"ballerinax_connector_tests/soundcloud.yaml"},
//                {"ballerinax_connector_tests/stripe.yaml"}, // not working due to anyOf schema in parameters
                {"ballerinax_connector_tests/vimeo.yaml"},
//                {"ballerinax_connector_tests/ynab.yaml"}, // HTTP status code '209' is not supported in Ballerina.
                {"ballerinax_connector_tests/zoom.yaml"},
                {"3.1.0_openapis/adyen_accountservice.yaml"},
//                {"3.1.0_openapis/codat_accounting.yaml"}, // openapi file has errors
//                {"3.1.0_openapis/codat_bank_feeds.yaml"}, // openapi file has errors
//                {"3.1.0_openapis/disclosure.yaml"}, // openapi file has errors
                {"3.1.0_openapis/listennotes.yaml"},
                {"3.1.0_openapis/placekit.yaml"},
                {"3.1.0_openapis/urlbox.yaml"},
//                {"3.1.0_openapis/vercel.yaml"}, // uncomment when /openapi-tools/issues/1332 is fixed
                {"3.1.0_openapis/webscraping.yaml"},
                {"3.1.0_openapis/wolframalpha.yaml"}
        };
    }

    @AfterTest
    public void cleanUp() throws IOException {
        TestUtils.deleteGeneratedFiles();
    }
}
