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
        Assert.assertEquals(errors.length, 2);
        String undocumentedReturnCode = "ERROR [single_status_code.bal:(8:5,10:6)] undefined status code(s) '[200]'" +
                " for return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());

        //Unimplemented return type.
        String unimplementedReturnCode = "ERROR [single_status_code.bal:(8:5,10:6)] missing implementation for" +
                " return code(s) '[202]' in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(unimplementedReturnCode, errors[1].toString());
    }

    @Test(description = "Type mis match media type")
    public void typeMistMatchMediaType() {
        Path path = RES_DIR.resolve("single_record_payload_type_mismatch.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String undocumentedReturnCode = "ERROR [single_record_payload_type_mismatch.bal:(14:5,18:6)] undefined " +
                "resource return mediaType(s) '[text/plain]' for return status code '200' in the counterpart " +
                "Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());

        String unimplementedReturnCode = "ERROR [single_record_payload_type_mismatch.bal:(14:5,18:6)] missing" +
                " implementation for return mediaType(s) '[application/json]' for return code '200' in the" +
                " HTTP method 'get' that associated with the path '/'.";
        Assert.assertEquals(unimplementedReturnCode, errors[1].toString());
    }

    @Test(description = "Return type has record including status code field.")
    public void statusCodeWithRecord() {
        Path path = RES_DIR.resolve("single_record_status_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String undocumentedReturnCode = "ERROR [single_record_status_code.bal:(14:5,18:6)] undefined status" +
                " code(s) '[202]' for return type in the counterpart Ballerina service resource" +
                " (method: 'get', path: '/').";
        Assert.assertEquals(undocumentedReturnCode, errors[0].toString());
    }

    @Test(description = "Union type with status code only.")
    public void unionStatusCode() {
        Path path = RES_DIR.resolve("union_status_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String allMessage = "ERROR [union_status_code.bal:(8:5,10:6)] undefined status code(s) '[400, 401, 404]'" +
                " for return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(allMessage, errors[0].toString());
    }


    @Test(description = "Type mismatch record field in Response record")
    public void typeMistMatchRecordFiled() {
        Path path = RES_DIR.resolve("single_record.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 1);
        String typeMismatch = "ERROR [single_record.bal:(10:9,10:11)] implementation type does not match with " +
                "OpenAPI contract type (expected 'string', found 'int') for the field 'id' of type 'Test'.";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Union type validation")
    public void unionReturnType() {
        Path path = RES_DIR.resolve("union_return_type.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 1);
        String typeMismatch = "ERROR [union_return_type.bal:(15:9,15:11)] implementation type does not " +
                "match with OpenAPI contract type (expected 'string', found 'int') for the field 'id' of type 'Pet'.";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Undocumented union type")
    public void undocumentedUnionType() {
        Path path = RES_DIR.resolve("undocumented_union_return_type.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String typeMismatch = "ERROR [undocumented_union_return_type.bal:(24:5,26:6)] undefined status code(s)" +
                " '[406]' for return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Without return type")
    public void withoutReturnType() {
        Path path = RES_DIR.resolve("without_return.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String typeMismatch = "ERROR [without_return.bal:(8:5,9:6)] undefined status code(s) '[202]' for" +
                " return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Error return type")
    public void errorReturnType() {
        Path path = RES_DIR.resolve("error_return.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String typeMismatch = "ERROR [error_return.bal:(8:5,10:6)] undefined status code(s) '[500]' " +
                "for return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Error return type")
    public void errorReturnTypeWithNil() {
        Path path = RES_DIR.resolve("nil_error_return.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 2);
        String typeMismatch = "ERROR [nil_error_return.bal:(8:5,9:6)] undefined status code(s) '[500]' for " +
                "return type in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Unimplemented MediaType")
    public void unimplementedMediaType() {
        Path path = RES_DIR.resolve("unimplemented_media_type.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 1);
        String typeMismatch = "ERROR [unimplemented_media_type.bal:(15:5,23:6)] missing implementation for return" +
                " mediaType(s) '[application/xml]' for return code '200' in the HTTP method 'get' that associated" +
                " with the path '/'.";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }


    @Test(description = "Unimplemented code in OAS to Ballerina")
    public void singleCodeOas() {
        Path path = RES_DIR.resolve("unimplemented_status_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 1);
        String typeMismatch = "ERROR [unimplemented_status_code.bal:(10:5,12:6)] missing implementation for return" +
                " code(s) '[404]' in the counterpart Ballerina service resource (method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Union return type with error")
    public void unionWithError() {
        Path path = RES_DIR.resolve("union_with_error_type.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 3);
        String typeMismatch = "ERROR [union_with_error_type.bal:(8:5,10:6)] undefined status code(s) " +
                "'[400, 500, 401]' for return type in the counterpart Ballerina service resource " +
                "(method: 'get', path: '/').";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Return has record type which has extra field in relevant in OAS object schema")
    public void recordType() {
        Path path = RES_DIR.resolve("record.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 1);
        String typeMismatch = "ERROR [record.bal:(13:5,17:6)] could not find OpenAPI object schema field 'type' in" +
                " 'Pet' the Ballerina record.";
        Assert.assertEquals(typeMismatch, errors[0].toString());
    }

    @Test(description = "Test for all return codes validation")
    public void allReturnCodes() {
        Path path = RES_DIR.resolve("response_code.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertEquals(errors.length, 0);
    }

    @Test(description = "Module level record", enabled = false)
    public void handleModuleLevelQualifierRecord() {
        // TODO: res:ResRecord
    }

}
