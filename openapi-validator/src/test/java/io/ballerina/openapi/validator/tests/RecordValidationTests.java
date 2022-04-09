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
    private static final Path RES_DIR = Paths.get("src/test/resources/record")
            .toAbsolutePath();
    @Test(description = "Type mismatch record field with basic type")
    public void typeMisMatchField() {
        Path path = RES_DIR.resolve("type_mismatch_field.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Undocumented query parameter
        String undocumentedRB = "ERROR [request_body.bal:(8:33,8:59)] Request body for the method 'post' of the" +
                " resource associated with the path '{2}' is not documented in the OpenAPI contract.";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    @Test(description = "Type mis match request body payload type")
    public void typeMisMatchRequestBodyMediaType() {
        Path path = RES_DIR.resolve("type_mismatch_request_body.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 1);
        // Undocumented query parameter
        String undocumentedRB = "Implementation payload type does not match with OAS contract content type (expected" +
                " 'application/json',found '[text/plain]') for the 'payload' in http method 'post' that associated " +
                "with the path '/pets'.";
        Assert.assertEquals(undocumentedRB, errors[0].toString());
    }

    //TODO: unimplemented requestbody oas->ballerina
}
