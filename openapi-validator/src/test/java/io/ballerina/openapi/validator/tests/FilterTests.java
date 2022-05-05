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
 * This test class including all the tests related to filter.
 */
public class FilterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/filter").toAbsolutePath();
    @Test(description = "Tag: validate only resource include in tags")
    public void testTag() {
        Path path = RES_DIR.resolve("tag.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 3);
    }

    @Test(description = "Operation: validate only resource include operations id")
    public void testOperation() {
        Path path = RES_DIR.resolve("operation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
    }

    @Test(description = "ExcludeTags: won't validate resources include in e.tag filter")
    public void testETag() {
        Path path = RES_DIR.resolve("etag.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 3);
    }

    @Test(description = "ExcludeOperation: won't validate resources include in e.operation filter")
    public void testEOperation() {
        Path path = RES_DIR.resolve("eoperation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 4);
    }

    @Test(description = "tags + operations = validate all resource with both")
    public void testTagOperation() {
        Path path = RES_DIR.resolve("tag_operation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 5);
    }

    @Test(description = "e.tags + e.operations =  not all resource with both")
    public void testETagEOperation() {
        Path path = RES_DIR.resolve("etag_eoperation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 2);
    }

    @Test(description = "tags + e.operations =  not resource with tag except given operations ")
    public void testTagEOperation() {
        Path path = RES_DIR.resolve("tag_eoperation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 2);
    }

    @Test(description = "operations + e.tags = given operation has e.tags -> validate operation without skip")
    public void testETagOperation() {
        Path path = RES_DIR.resolve("etag_operation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 3);
    }

    @Test(description = "e.tags + tag = compilation error")
    public void testTagETag() {
        Path path = RES_DIR.resolve("etag_tag.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
    }

    @Test(description = "e.operations + operations = compilation error")
    public void testOperationEOperation() {
        Path path = RES_DIR.resolve("eoperation_operation.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
    }

    @Test(description = "tags + operations + e.tags + e.operations = compilation error ")
    public void testAll() {
        Path path = RES_DIR.resolve("all_four.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
    }
}
