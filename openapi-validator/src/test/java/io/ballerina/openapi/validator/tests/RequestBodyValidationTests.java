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
 * This test class use for cover the Request body in resources functions.
 */
public class RequestBodyValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/request-body")
            .toAbsolutePath();
    @Test(description = "Undocumented request body")
    public void undocumentedRequestBody() {
        Path path = RES_DIR.resolve("request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [request_body.bal:(8:33,8:59)] undefined request body for the method 'post' " +
                "of the resource associated with the path '/pets' in the openapi contract.";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    @Test(description = "Type mis match request body payload type")
    public void typeMisMatchRequestBodyMediaType() {
        Path path = RES_DIR.resolve("type_mismatch_request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [type_mismatch_request_body.bal:(8:32,8:58)] undefined mediaType/s " +
                "'application/json' for the request body in the counterpart ballerina service resource" +
                " (method: 'post', path: '/pet').";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    @Test(description = "Unimplemented request body")
    public void unimplementedRequestBody() {
        Path path = RES_DIR.resolve("oas_request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [oas_request_body.bal:(8:5,10:6)] missing openapi contract request body" +
                " implementation in the counterpart ballerina service resource (method: 'post', path: '/pets')";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    @Test(description = "Unimplemented request body media type")
    public void unimplementedMediaType() {
        Path path = RES_DIR.resolve("mis_mediatype_oas_request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [mis_mediatype_oas_request_body.bal:(8:33,8:59)] missing openapi contract" +
                " request body media type '[text/plain]' in the counterpart ballerina service resource" +
                " (method: 'post', path: '/pets')";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    //TODO: union support in request
    //TODO: Array type mapping
    @Test(description = "Request body array type validation")
    public void arrayTypeValidation() {
        Path path = RES_DIR.resolve("array_request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [array_request_body.bal:(5:9,5:11)] implementation type does not match" +
                " with openapi contract type (expected 'string', found 'int') for the field 'id' of type 'Pet'";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    @Test(description = "Unimplemented request body array type validation")
    public void arrayTypeMisMatchValidation() {
        Path path = RES_DIR.resolve("array_request_body_02.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        String undocumentedRB = "ERROR [array_request_body_02.bal:(14:33,14:58)] implementation payload type does" +
                " not match with openapi contract content type 'application/json' (expected 'Pet[]',found 'Pet') for" +
                " the http method 'post' that associated with the path '/pets'.";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }
}
