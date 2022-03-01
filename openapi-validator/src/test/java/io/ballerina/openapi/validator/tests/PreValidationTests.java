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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.validator.tests.ValidatorTest.getCompilation;
import static io.ballerina.openapi.validator.tests.ValidatorTest.getProject;

public class PreValidationTests {
    //1. compilation issue tests error -done,
    //2. compilation issue tests warning -done,
    //5. multiple service node - not to repeat diagnostic
    //6. multiple service node with multiple annotation
    private static final Path RES_DIR = Paths.get("src/test/resources/pre-processing")
            .toAbsolutePath();
    @Test(description = "Annotation with non http service")
    public void nonHttpService() {
        Path path = RES_DIR.resolve("non_http.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
    }

    @Test(description = "Given ballerina file has compilation issue")
    public void compilationIssue() {
        Path path = RES_DIR.resolve("compilation_error.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
        Assert.assertEquals(i, 2);
    }

    @Test(description = "Given ballerina file has compilation issue as warning")
    public void compilationWarnings() {
        Path path = RES_DIR.resolve("compilation_error.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
//        Assert.assertEquals(i, 2);
    }

}
