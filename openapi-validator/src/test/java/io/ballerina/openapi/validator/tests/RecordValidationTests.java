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
public class RecordValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/record").toAbsolutePath();

    @Test(description = "Type mismatch record field with basic type")
    public void typeMisMatchField01() {
        Path path = RES_DIR.resolve("type_mismatch_field.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Type mismatch field
        String typeMismatch = "ERROR [type_mismatch_field.bal:(5:12,5:14)] Implementation type does not match with" +
                " OAS contract type (expected 'string', found 'integer') for the field 'id' of type 'Pet'";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Type mismatch record field with array type")
    public void typeMisMatchArrayField() {
        Path path = RES_DIR.resolve("array_type_mismatch_field.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 2);
        // Type mismatch field
        String typeMismatch01 = "ERROR [array_type_mismatch_field.bal:(7:14,7:18)] Implementation type does not match" +
                " with OAS contract type (expected 'string[]', found 'integer[]') for the field 'tags' of type 'Pet'";
        String typeMismatchNestedArray = "ERROR [array_type_mismatch_field.bal:(8:14,8:24)] Implementation type does " +
                "not match with OAS contract type (expected 'string[]', found 'string[][]') for the field " +
                "'categories' of type 'Pet'";
        Assert.assertEquals(typeMismatch01, errors[0].toString());
        Assert.assertEquals(typeMismatchNestedArray, errors[1].toString());
    }

    @Test(description = "Type mismatch record field with array type")
    public void typeMisMatchRecordField() {
        Path path = RES_DIR.resolve("type_mismatch_record.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Type mismatch field
        String typeMismatch01 = "ERROR [type_mismatch_record.bal:(10:9,10:11)] Implementation type does not match " +
                "with OAS contract type (expected 'int', found 'string') for the field 'id' of type 'Type'";
        Assert.assertEquals(typeMismatch01, errors[0].toString());
    }

    @Test(description = "Undocumented record field in record")
    public void undocumentedRecordField() {
        Path path = RES_DIR.resolve("undocumented_field.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedField = "ERROR [undocumented_field.bal:(7:12,7:17)] The 'type' field in the 'Pet'" +
                " record is not documented in the OpenAPI contract 'Pet' schema.";
        Assert.assertEquals(undocumentedField, errors[0].toString());
    }

    //TODO: unimplemented requestbody oas->ballerina
}
