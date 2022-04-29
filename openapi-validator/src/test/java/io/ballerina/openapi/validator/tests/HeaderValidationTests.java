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
import static io.ballerina.openapi.validator.tests.ValidatorTest.getDiagnostics;
import static io.ballerina.openapi.validator.tests.ValidatorTest.getProject;

public class HeaderValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/headers").toAbsolutePath();
    @Test(description = "Required Header test")
    public void resourceHeaderValidation() {
        Path path = RES_DIR.resolve("header.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 2);
        // Type mismatch
        String typeMismatchError = "ERROR [header.bal:(8:32,8:61)] Implementation type does not match with OAS " +
                "contract type (expected 'string',found 'integer') for the header 'x-offset' in http method 'get'" +
                " that associated with the path '/pets'.";
        Assert.assertEquals(typeMismatchError, errors[0].toString());

        // Undocumented header
        String undocumentedParameter = "ERROR [header.bal:(10:34,10:78)] 'x-offset' header for the method 'get' " +
                "of the resource associated with the path '/pets02' is not documented in the OpenAPI contract.";
        Assert.assertEquals(undocumentedParameter, errors[1].toString());
    }

    @Test(description = "Oas Header test")
    public void oasHeader() {
        Path path = RES_DIR.resolve("oas_header.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // unimplemented header
        String typeMismatchError = "ERROR [oas_header.bal:(8:5,10:6)] Missing OpenAPI contract header" +
                " 'x-offset' in the counterpart Ballerina service resource (method: 'get', path: '/pets')";
        Assert.assertEquals(typeMismatchError, errors[0].toString());
    }

    @Test(description = "Header has array types")
    public void headerArrays() {
        Path path = RES_DIR.resolve("array_header.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // unimplemented header
        String typeMismatchError = "ERROR [oas_header.bal:(8:5,10:6)] Missing OpenAPI contract header" +
                " 'x-offset' in the counterpart Ballerina service resource (method: 'get', path: '/pets')";
        Assert.assertEquals(typeMismatchError, errors[0].toString());
    }
}
