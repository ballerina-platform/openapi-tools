/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This test class is to contain the test related to constraint validation.
 */
public class NegativeConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();

    //TODO: Made this disable till the Ballerina lang API received
    @Test(description = "Tests for string type record field has invalid pattern constraint.", enabled = false)
    public void testForRecordStringHasInvalidPattern() throws IOException, BallerinaOpenApiException,
            FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger/constraint" +
                "/invalid_pattern_string.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/constraint/string_pattern.bal", syntaxTree);
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree);
        boolean hasErrors = diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
        assertFalse(hasErrors);
    }
}
