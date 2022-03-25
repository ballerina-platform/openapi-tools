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
public class QueryParameterValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/parameter")
            .toAbsolutePath();
    @Test(description = "Query parameter type mismatch test")
    public void queryTest() {
        Path path = RES_DIR.resolve("query_parameter.bal");
        Project project = getProject(path);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = getDiagnostics(diagnostic);
        Assert.assertTrue(errors.length == 2);
        // Type mismatch
        String typeMismatchError = "ERROR [query_parameter.bal:(8:32,8:45)] Implementation type does not match with " +
                "OAS contract type (expected 'string',found 'integer') for the parameter 'offset' in http method " +
                "'get' that associated with the path '/pets'.";
        Assert.assertEquals(typeMismatchError, errors[0].toString());

        // Undocumented query parameter
        String undocumentedParameter = "ERROR [query_parameter.bal:(10:46,10:59)] 'limits' parameter for the method " +
                "'get' of the resource associated with the path '/pets02' is not documented in the OpenAPI contract.";
        Assert.assertEquals(undocumentedParameter, errors[1].toString());
    }
}
