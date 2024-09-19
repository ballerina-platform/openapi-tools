/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.core.generators.common.OASModifier;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Optional;

/**
 * Main class to implement "sanitize" subcommand which is used to flatten and sanitize the OpenAPI definition
 * by generating Ballerina friendly type schema names and Ballerina type name extensions.
 *
 * @since 2.2.0
 */
@CommandLine.Command(
        name = "sanitize",
        description = "Sanitize the OpenAPI definition by generating Ballerina friendly type schema names and " +
                "Ballerina type name extensions."
)
public class Sanitize extends SubCmdBase {

    public Sanitize() {
        super(CommandType.SANITIZE);
    }

    public Sanitize(PrintStream errorStream, boolean exitWhenFinish) {
        super(CommandType.SANITIZE, errorStream, exitWhenFinish);
    }

    @Override
    public String getDefaultFileName() {
        return "sanitized_openapi";
    }

    @Override
    public Optional<OpenAPI> generate(String openAPIFileContent) {
        Optional<OpenAPI> filteredOpenAPI = getFilteredOpenAPI(openAPIFileContent);
        return filteredOpenAPI.flatMap(this::getFlattenOpenAPI).flatMap(this::sanitizeOpenAPI);
    }

    private Optional<OpenAPI> sanitizeOpenAPI(OpenAPI openAPI) {
        OASModifier oasSanitizer = new OASModifier();
        try {
            return Optional.of(oasSanitizer.modifyWithBallerinaConventions(openAPI));
        } catch (BallerinaOpenApiException exp) {
            printError("ERROR: %s".formatted(exp.getMessage()));
            return Optional.empty();
        }
    }
}
