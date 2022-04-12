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

/**
 * This test class use for cover the query parameter tests.
 */
public class ReturnTypeValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/return")
            .toAbsolutePath();
    @Test(description = "Undocumented return status code")
    public void undocumentedReturnStatusCode() {
        Path path = RES_DIR.resolve("single_status_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Undocumented query parameter
        String undocumentedReturnCode = "ERROR [single_status_code.bal:(8:31,8:46)] Undocumented resource return " +
                "status code '200' for the method 'get' of the resource associated with the path '/'.";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());
    }

    @Test(description = "Type mis match media type", enabled = false)
    public void typeMistMatchMediaType() {
        Path path = RES_DIR.resolve("single_record.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Undocumented query parameter
        String undocumentedReturnCode = "ERROR [single_status_code.bal:(8:31,8:46)] Undocumented resource return " +
                "status code '200' for the method 'get' of the resource associated with the path '/'.";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());
    }

    //--->
    @Test(description = "Status code change in record")
    public void statusCodeWithRecord() {
        Path path = RES_DIR.resolve("single_record_status_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Undocumented query parameter
        String undocumentedReturnCode = "ERROR [single_status_code.bal:(8:31,8:46)] Undocumented resource return " +
                "status code '200' for the method 'get' of the resource associated with the path '/'.";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());
    }

//    @Test(description = "Type mis match media type")
//    public void typeMistMatchMediaType() {
//        Path path = RES_DIR.resolve("single_record.bal");
//        Project project = getProject(path);
//        DiagnosticResult diagnostic = getCompilation(project);
//        Object[] errors = getDiagnostics(diagnostic);
//        Assert.assertTrue(errors.length == 1);
//        // Undocumented query parameter
//        String undocumentedReturnCode = "ERROR [single_status_code.bal:(8:31,8:46)] Undocumented resource return " +
//                "status code '200' for the method 'get' of the resource associated with the path '/'.";
//        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());
//    }
}
