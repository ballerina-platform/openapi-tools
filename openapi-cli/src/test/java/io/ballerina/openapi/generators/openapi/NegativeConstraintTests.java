/*
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
package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.openapi.generators.openapi.TestUtils.getCompilation;

/**
 * This test class for the covering the negative tests for constraint
 * {@link io.ballerina.openapi.converter.service.ConstraintAnnotation} scenarios.
 *
 */
public class NegativeConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();

    @Test(description = "When the string constraint has incompatible REGEX patterns with OAS")
    public void testInterpolationInRegexPatterns() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/negative_patternInterpolation.bal");
        List<OpenAPIConverterDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                                    ballerinaFilePath, "constraint-negative/negative_patternInterpolation.yaml");
        List<String> expectedPatterns = Arrays.asList("^${i}[a-zA-Z]+$", "^[A-Z]${j}+$", "^[\\${2}a-z]+$"
                                                     , "^[a-z${2}]+$");
        for (int i = 0; i < errors.size(); i++) {
            Assert.assertEquals(errors.get(i).getMessage(), "Given REGEX pattern '" + expectedPatterns.get(i) +
                    "' is not supported by the OpenAPI tool, it may also not support interpolation within the " +
                    "REGEX pattern.");
        }
    }

    @Test(description = "When a constraint has values referenced with variables")
    public void testConstNameRef() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/negative_constNameRef.bal");
        List<OpenAPIConverterDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "constraint-negative/negative_constNameRef.yaml");
        List<String> expectedVariables = Arrays.asList("maxVal", "5 + minVal", "Value");
        for (int i = 0; i < errors.size(); i++) {
            Assert.assertEquals(errors.get(i).getMessage(), "Generated OpenAPI definition does not contain" +
                    " variable assignment '" + expectedVariables.get(i) + "' in constraint validation.");
        }
    }

    /*
     * This test is used to check whether it gives warnings when '@constraint:Date' is being used.
     * Currently, we don't have support for mapping Date constraints to OAS hence we skip them.
     * TODO: <a href="https://github.com/ballerina-platform/ballerina-standard-library/issues/5049">...</a>
     * Once the above improvement is completed this Negative test should be removed and should add as a Unit test!
     */
    @Test(description = "when the record field has time:Date record type")
    public void testDateType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/negative_date.bal");
        List<OpenAPIConverterDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "constraint-negative/negative_date.yaml");
        errors.forEach(error -> Assert.assertEquals(error.getMessage(), "Ballerina Date constraints might " +
                "not be reflected in the OpenAPI definition"));
    }

    /*
     * This test is used to check whether it gives compilation errors when invalid REGEX patterns are given.
     * To get more context go through the referenced links.
     * By fixing below Bug will make this test fail.
     * {@link <a href="https://github.com/ballerina-platform/ballerina-lang/issues/41492">...</a>}
     * TODO: <a href="https://github.com/ballerina-platform/ballerina-standard-library/issues/5048">...</a>
     */
    @Test(description = "When String constraint has compilation errors (REGEX pattern with invalid format)")
    public void testInvalidRegexPattern() throws ProjectException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/invalidRegexPattern.bal");
        Project project = ProjectLoader.loadProject(ballerinaFilePath);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 2);
        Assert.assertTrue(errors[0].toString().contains("ERROR [invalidRegexPattern.bal:(21:20,21:20)] " +
                "invalid char after backslash"));
        Assert.assertTrue(errors[1].toString().contains("ERROR [invalidRegexPattern.bal:(21:22,21:22)] " +
                "missing backslash"));
    }

    @Test(description = "When Integer constraint has float value which is invalid")
    public void testInvalidInteger() throws ProjectException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/invalidInteger.bal");
        Project project = ProjectLoader.loadProject(ballerinaFilePath);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 1);
        Assert.assertTrue(errors[0].toString().contains("incompatible types: expected '(int|record {| int value; " +
                "string message; |})?', found 'float'"));
    }
}
