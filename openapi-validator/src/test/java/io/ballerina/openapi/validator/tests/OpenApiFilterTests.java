/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/**
 * This unit tests for filterOpenApi function.
 */
public class OpenApiFilterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/annotation").toAbsolutePath();
    @Test(description = "Test without filters ")
    public void withoutFilters() {
        Path path = RES_DIR.resolve("positive/without_filters.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        int i = diagnostic.diagnosticCount();
        Assert.assertEquals(2, i);
    }
}
