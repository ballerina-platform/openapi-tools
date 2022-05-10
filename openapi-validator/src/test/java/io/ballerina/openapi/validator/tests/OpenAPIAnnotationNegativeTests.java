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
import static io.ballerina.openapi.validator.tests.ValidatorTest.getProject;

/**
 * This test set for covering the all the negative behaviours of the annotation.
 */
public class OpenAPIAnnotationNegativeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/annotation").toAbsolutePath();
    @Test(description = "Contract has annotation without attributes")
    public void annotationWithoutFields() {
        Path path = RES_DIR.resolve("negative/annotation_without_fields.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 0);
    }

    @Test(description = "Contract attribute has path empty string")
    public void contractPathEmptyString() {
        Path path = RES_DIR.resolve("negative/contract_path_empty.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 0);
    }

    @Test(description = "contract: when it has invalid format instead of .json and .yaml format contract")
    public void invalidFormat() {
        Path path = RES_DIR.resolve("negative/contract_format_invalid.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                        DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 1);
        Assert.assertTrue(errors[0].toString().contains("ERROR [contract_format_invalid.bal:(4:1,11:2)] invalid " +
                "file type. Provide either a .yaml or .json file."));
    }

    @Test(description = "contract path location invaild")
    public void invalidLocation() {
        Path path = RES_DIR.resolve("negative/contract_path_invalid.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 1);
        Assert.assertTrue(errors[0].toString().contains("ERROR [contract_path_invalid.bal:(4:1,11:2)] openapi" +
                " contract does not exist in the given location:"));
    }

    @Test(description = "contract path missing with filters enable")
    public void missingContractPath() {
        Path path = RES_DIR.resolve("negative/missing_contract_path.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 0);
    }

    @Test(description = "annotation is with only embed field")
    public void withEmbedField() {
        Path path = RES_DIR.resolve("negative/with_embed_field.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 0);
    }
}
