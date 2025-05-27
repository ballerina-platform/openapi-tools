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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    public static final String INVALID_ALIGNMENT_OPTIONS = "ERROR: Invalid alignment type '%s' provided. " +
            "The available options are: %s";
    private static final String INFO_MSG_PREFIX = "Aligned";
    public static final String NAME = "name";
    public static final String DOC = "doc";
    public static final String BASEPATH = "basepath";
    public static final List<String> DEFAULT_ALIGNMENT_TYPES = List.of(NAME, DOC, BASEPATH);
    private List<String> alignmentTypes = new ArrayList<>(List.of(NAME, DOC, BASEPATH));;

    @CommandLine.Option(names = {"--include"}, description = "Alignment types to be included. " +
            "The available options are: name, doc, basepath.")
    public String includedAlignments;

    @CommandLine.Option(names = {"--exclude"}, description = "Alignment types to be excluded. " +
            "The available options are: name, doc, basepath.")
    public String excludedAlignments;

    public Align() {
        super(CommandType.ALIGN, INFO_MSG_PREFIX);
    }

    public Align(PrintStream errorStream, boolean exitWhenFinish) {
        super(CommandType.ALIGN, INFO_MSG_PREFIX, errorStream, exitWhenFinish);
    }

    @Override
    public void execute() {
        setAlignmentTypes();
        super.execute();
    }

    private void setAlignmentTypes() {
        List<String> validatedIncludedAlignments = getValidatedAlignmentTypes(includedAlignments);
        List<String> validatedExcludedAlignments = getValidatedAlignmentTypes(excludedAlignments);
        if (!validatedIncludedAlignments.isEmpty()) {
            alignmentTypes = validatedIncludedAlignments;
        } else if (!validatedExcludedAlignments.isEmpty()) {
            alignmentTypes.removeAll(validatedExcludedAlignments);
        }
    }

    private List<String> getValidatedAlignmentTypes(String alignmentTypes) {
        if (Objects.isNull(alignmentTypes) || alignmentTypes.isEmpty()) {
            return Collections.emptyList();
        }
        String[] alignmentTypesList = alignmentTypes.split(COMMA);
        List<String> validatedAlignmentTypes = new ArrayList<>();
        for (String alignmentType : alignmentTypesList) {
            if (DEFAULT_ALIGNMENT_TYPES.contains(alignmentType)) {
                validatedAlignmentTypes.add(alignmentType);
            } else {
                printError(INVALID_ALIGNMENT_OPTIONS.formatted(alignmentType, DEFAULT_ALIGNMENT_TYPES));
            }
        }
        return validatedAlignmentTypes;
    }

    @Override
    public String getDefaultFileName() {
        return "aligned_ballerina_openapi";
    }

    @Override
    public Optional<OpenAPI> generate(String openAPIFileContent) {
        Optional<OpenAPI> filteredOpenAPI = getFilteredOpenAPI(openAPIFileContent);
        return filteredOpenAPI.flatMap(this::alignOpenAPI);
    }

    private Optional<OpenAPI> alignOpenAPI(OpenAPI openAPI) {
        try {
            return Optional.of(new OASModifier().modify(openAPI, alignmentTypes));
        } catch (BallerinaOpenApiException exp) {
            printError("ERROR: %s".formatted(exp.getMessage()));
            return Optional.empty();
        }
    }
}
