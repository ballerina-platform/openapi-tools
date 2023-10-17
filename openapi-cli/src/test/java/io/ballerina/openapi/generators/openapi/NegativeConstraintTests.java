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
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * This test class for the covering the negative tests for constraint
 * {@link io.ballerina.openapi.converter.service.ConstraintAnnotation} scenarios.
 *
 */
public class NegativeConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();

    @Test(description = "When the string constraint has incompatible REGEX patterns with OAS")
    public void testInvalidRegexPatterns() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint-negative/negative_patternInterpolation.bal");
        List<OpenAPIConverterDiagnostic> errors = TestUtils.compareWithGeneratedFile(new OASContractGenerator(),
                                    ballerinaFilePath, "constraint-negative/negative_patternInterpolation.yaml");
        List<String> expectedPatterns = Arrays.asList("^${i}[a-zA-Z]+$", "^[A-Z]${j}+$", "^[\\${2}a-z]+$"
                                                     , "^[a-z${2}]+$");
        for (int i = 0; i < errors.size(); i++) {
            Assert.assertEquals(errors.get(i).getMessage(), "Given REGEX pattern '" + expectedPatterns.get(i) +
                    "' is not supported by the OpenAPI tool, it may also not support interpolation within the " +
                    "REGEX pattern.");
        }
    }
}
