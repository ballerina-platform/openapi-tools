/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.openapi.generators.openapi.TestUtils.getCompilation;

/**
 * This test class is used to cover the negative tests for path parameter mapping from Ballerina to OAS.
 */
public class NegativePathParameterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();

    @Test(description = "Generate OpenAPI spec with rest parameters in the resource path.")
    public void testRestParamInPath() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("path_with_rest_param.bal");
        List<OpenAPIMapperDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                ballerinaFilePath, "path_with_rest_param.yaml");
        errors.forEach(error -> Assert.assertEquals(error.getMessage(), "Generated OpenAPI specification " +
                "excludes details for operation with rest parameter in the resource path"));
    }

    @Test(description = "When path parameters has invalid types.")
    public void testInvalidInteger() throws ProjectException {
        Path ballerinaFilePath = RES_DIR.resolve("invalid_path_param_type.bal");
        Project project = ProjectLoader.loadProject(ballerinaFilePath);
        DiagnosticResult diagnostic = getCompilation(project);
        Object[] errors = diagnostic.diagnostics().stream().filter(d ->
                DiagnosticSeverity.ERROR == d.diagnosticInfo().severity()).toArray();
        Assert.assertEquals(errors.length, 4);
        Assert.assertTrue(errors[0].toString().contains("only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                "are supported as path params, found 'string[]'"));
        Assert.assertTrue(errors[1].toString().contains("only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                "are supported as path params, found 'Artist'"));
        Assert.assertTrue(errors[2].toString().contains("only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                "are supported as path params, found 'xml'"));
        Assert.assertTrue(errors[3].toString().contains("only 'int', 'string', 'float', 'boolean', 'decimal' types " +
                "are supported as path params, found 'map<json>'"));
    }
}
