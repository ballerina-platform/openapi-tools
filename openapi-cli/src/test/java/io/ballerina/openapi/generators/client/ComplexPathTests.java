/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * This class contains test that related to complex URLs.
 */
public class ComplexPathTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client/complex_path").toAbsolutePath();
    private SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test()
    public void parameterizedPathSegmentWithSpecialCharacters() throws IOException, BallerinaOpenApiException {
        // Create a PrintStream that captures the output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        // Redirect System.out to our custom PrintStream
        System.setErr(printStream);

        Path definitionPath = RESDIR.resolve("swagger/identically_equal_after_workaround.yaml");
        Path expectedPath = RESDIR.resolve("ballerina/identically_equal_after_workaround.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(true).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();

        // Reset System.out back to its original value
        System.setErr(System.err);

        // Get the captured output as a string and print it
        String capturedOutput = outputStream.toString();
        String expectedOutput = "WARNING: found unsupported characters in the given " +
                "`/v3/ClientGroups/GetClientGroupByUserDefinedIdentifier(UserDefinedIdentifier=" +
                "'{userDefinedIdentifier}')` url. Please use the modified `v3/ClientGroups/" +
                "GetClientGroupByUserDefinedIdentifier/UserDefinedIdentifier/[string userDefinedIdentifier]` url.\n" +
                "WARNING: found unsupported characters in the given `/payroll/v1/workers/{associateoid}/" +
                "organizational-pay-statements/{payStatementId}/images/{imageId}.{imageExtension}` url. " +
                "Please use the modified `payroll/v1/workers/[string associateoid]/organizational\\-pay\\-statements/" +
                "[int payStatementId]/images/[int imageId]/[string imageExtension]` url.\n" +
                "WARNING: found unsupported characters in the given `/companies({company_id})/items({item_id})` url. " +
                "Please use the modified `companies/[string company_id]/items/[int item_id]` url.\n" +
                "WARNING: found unsupported characters in the given `/v4/spreadsheets/{spreadsheetId}.{sheetId}" +
                "/sheets/{sheetId}:copyTo` url. Please use the modified `v4/spreadsheets/[string spreadsheetId]/" +
                "[int sheetId]/sheets/[int sheetId]/copyTo` url.\n" +
                "WARNING: found unsupported characters in the given `/v4/spreadsheets/{spreadsheetId}/sheets/" +
                "{sheetId}:copyFrom` url. Please use the modified `v4/spreadsheets/[string spreadsheetId]/sheets/" +
                "[int sheetId]/copyFrom` url.\n" +
                "WARNING: found unsupported characters in the given `/v4/spreadsheets/{spreadsheetId}/sheets/" +
                "{sheetId}:copyTo` url. Please use the modified `v4/spreadsheets/[string spreadsheetId]/sheets/" +
                "[int sheetId]/copyTo` url.\n";
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
        Assert.assertEquals(expectedOutput, capturedOutput);
    }
}
