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
 * Validate tests for path parameters.
 */
public class PathParameterValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/parameter")
            .toAbsolutePath();
    @Test(description = "Path parameter type mismatch test")
    public void typeMismatch() {
        Path path = RES_DIR.resolve("path_parameter.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors =
                diagnostic.diagnostics().stream().filter(d -> DiagnosticSeverity.ERROR == d.diagnosticInfo().severity())
                        .toArray();
        Assert.assertTrue(errors.length == 2);
        String error = "ERROR [path_parameter.bal:(13:54,13:69)] Implementation type does not match with OAS " +
                "contract type (expected 'int',found 'string') for the parameter 'owner-id' in http method 'get'" +
                " that associated with the path '/pets/{petId}/owner/{owner-id}'.";
        Assert.assertEquals(error, errors[0].toString());
    }
}
