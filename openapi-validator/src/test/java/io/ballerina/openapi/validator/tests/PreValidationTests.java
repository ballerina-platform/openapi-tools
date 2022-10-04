/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator.tests;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.validator.tests.ValidatorTest.getCompilation;
import static io.ballerina.openapi.validator.tests.ValidatorTest.getDiagnostics;
import static io.ballerina.openapi.validator.tests.ValidatorTest.getProject;

/**
 * This test suit enable all the tests for checking the valida service for validate.
 */
public class PreValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/pre-processing")
            .toAbsolutePath();

    // TODO: enable after releasing graphql with latest master
    @Test(description = "Annotation with non http service", enabled = false)
    public void nonHttpService() {
        Path path = RES_DIR.resolve("non_http.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        boolean isOpenapi = diagnostic.diagnostics().stream()
                .anyMatch(d -> (d.properties().stream()
                        .anyMatch(p -> p.value().toString().contains("openapi"))));
        Assert.assertFalse(isOpenapi);
    }

    @Test(description = "Given ballerina file has compilation issue")
    public void compilationIssue() {
        Path path = RES_DIR.resolve("compilation_error.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Assert.assertEquals(getDiagnostics(diagnostic).length, 2);
    }

    @Test(description = "Given ballerina file has compilation issue as warning")
    public void compilationWarnings() {
        Path path = RES_DIR.resolve("compilation_warning.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Assert.assertEquals(getDiagnostics(diagnostic).length, 0);
    }

    @Test(description = "Given ballerina file has compilation issue as warning")
    public void unImplementedPathAndOperations() {
        Path path = RES_DIR.resolve("unimplemented_resources.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors =
                diagnostic.diagnostics().stream().filter(d -> DiagnosticSeverity.ERROR == d.diagnosticInfo().severity())
                        .toArray();
        //Expected error messages
        String unimplementedPath = "ERROR [unimplemented_resources.bal:(4:1,10:2)] missing Ballerina service" +
                " resource(s) for HTTP method(s) 'get' for the path '/pet' which is documented in the openAPI contract";
        String unimplementedMethod = "ERROR [unimplemented_resources.bal:(4:1,10:2)] missing Ballerina service" +
                " resource for the path '/pet02' which is documented in the openAPI contract.";
        Assert.assertEquals(errors.length, 2);
    }

    @Test(description = "Given ballerina file has extra resources")
    public void undocumentedPathAndOperations() {
        Path path = RES_DIR.resolve("undocumented_resources.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Assert.assertEquals(getDiagnostics(diagnostic).length, 2);
    }

    @Test(description = "OpenAPI annotation with failOnErrors turn off with multiple resources")
    public void validatorTurnOff() {
        Path path = RES_DIR.resolve("multiple_services.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                        DiagnosticSeverity.WARNING == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 5);
    }
}
