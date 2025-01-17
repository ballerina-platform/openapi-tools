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
 * Main class to implement "align" subcommand which is used to flatten and align the OpenAPI definition
 * by generating Ballerina friendly type schema names and Ballerina type name extensions.
 *
 * @since 2.2.0
 */
@CommandLine.Command(
        name = "align",
        description = "Align the OpenAPI definition by generating Ballerina friendly type schema names and " +
                "Ballerina type name extensions."
)
public class Align extends SubCmdBase {

    private static final String INFO_MSG_PREFIX = "Aligned";

    public Align() {
        super(CommandType.ALIGN, INFO_MSG_PREFIX);
    }

    public Align(PrintStream errorStream, boolean exitWhenFinish) {
        super(CommandType.ALIGN, INFO_MSG_PREFIX, errorStream, exitWhenFinish);
    }

    @Override
    public String getDefaultFileName() {
        return "aligned_ballerina_openapi";
    }

    @Override
    public Optional<OpenAPI> generate(String openAPIFileContent) {
        Optional<OpenAPI> filteredOpenAPI = getFilteredOpenAPI(openAPIFileContent, true);
        return filteredOpenAPI.flatMap(this::alignOpenAPI);
    }

    private Optional<OpenAPI> alignOpenAPI(OpenAPI openAPI) {
        OASModifier oasAligner = new OASModifier();
        try {
            return Optional.of(oasAligner.modifyWithBallerinaConventions(openAPI));
        } catch (BallerinaOpenApiException exp) {
            printError("ERROR: %s".formatted(exp.getMessage()));
            return Optional.empty();
        }
    }
}
