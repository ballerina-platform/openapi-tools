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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.core.generators.common.OASModifier.getResolvedNameMapping;
import static io.ballerina.openapi.core.generators.common.OASModifier.getValidNameForType;

/**
 * Main class to implement "flatten" subcommand which is used to flatten the OpenAPI definition
 * by moving all the inline schemas to the "#/components/schemas" section.
 *
 * @since 2.2.0
 */
@CommandLine.Command(
        name = "flatten",
        description = "Flatten the OpenAPI definition by moving all the inline schemas to the " +
                "\"#/components/schemas\" section."
)
public class Flatten extends SubCmdBase {

    private static final String ERROR_OCCURRED_WHILE_GENERATING_SCHEMA_NAMES = "ERROR: error occurred while " +
            "generating schema names";
    private static final String INFO_MSG_PREFIX = "Flattened";

    public Flatten() {
        super(CommandType.FLATTEN, INFO_MSG_PREFIX);
    }

    public Flatten(PrintStream errorStream, boolean exitWhenFinish) {
        super(CommandType.FLATTEN, INFO_MSG_PREFIX, errorStream, exitWhenFinish);
    }

    @Override
    public String getDefaultFileName() {
        return "flattened_openapi";
    }

    @Override
    public Optional<OpenAPI> generate(String openAPIFileContent) {
        Optional<OpenAPI> filteredOpenAPI = getFilteredOpenAPI(openAPIFileContent);
        if (filteredOpenAPI.isEmpty()) {
            return Optional.empty();
        }

        OpenAPI openAPI = filteredOpenAPI.get();
        Components components = openAPI.getComponents();
        List<String> existingComponentNames = Objects.nonNull(components) && Objects.nonNull(components.getSchemas()) ?
                new ArrayList<>(components.getSchemas().keySet()) : new ArrayList<>();

        return getFlattenOpenAPI(openAPI)
                .flatMap(flattenOpenAPI -> sanitizeGeneratedFlattenNames(flattenOpenAPI, existingComponentNames));
    }

    private Optional<OpenAPI> sanitizeGeneratedFlattenNames(OpenAPI openAPI, List<String> existingComponentNames) {
        Map<String, String> proposedNameMapping = getProposedNameMapping(openAPI, existingComponentNames);
        if (proposedNameMapping.isEmpty()) {
            return Optional.of(openAPI);
        }

        SwaggerParseResult parserResult = OASModifier.getOASWithSchemaNameModification(openAPI, proposedNameMapping);
        openAPI = parserResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            printError(ERROR_OCCURRED_WHILE_GENERATING_SCHEMA_NAMES);
            if (!parserResult.getMessages().isEmpty()) {
                printError(FOUND_PARSER_DIAGNOSTICS);
                parserResult.getMessages().forEach(this::printError);
            }
            return Optional.empty();
        }

        return Optional.of(openAPI);
    }

    public Map<String, String> getProposedNameMapping(OpenAPI openapi, List<String> existingComponentNames) {
        Map<String, String> nameMap = new HashMap<>();
        if (Objects.isNull(openapi.getComponents())) {
            return Collections.emptyMap();
        }
        Components components = openapi.getComponents();
        Map<String, Schema> schemas = components.getSchemas();
        if (Objects.isNull(schemas)) {
            return Collections.emptyMap();
        }

        for (Map.Entry<String, Schema> schemaEntry: schemas.entrySet()) {
            if (existingComponentNames.contains(schemaEntry.getKey())) {
                continue;
            }
            String modifiedName = getValidNameForType(schemaEntry.getKey());
            if (modifiedName.equals(schemaEntry.getKey())) {
                continue;
            }
            nameMap.put(schemaEntry.getKey(), modifiedName);
        }
        return getResolvedNameMapping(nameMap);
    }
}
