/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.openapi.core.generators.common.OASModifier;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.service.mapper.utils.CodegenUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.parser.util.InlineModelResolver;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.cmd.CmdConstants.JSON_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.OPENAPI_FLATTEN_CMD;
import static io.ballerina.openapi.cmd.CmdConstants.YAML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE;
import static io.ballerina.openapi.core.generators.common.OASModifier.getResolvedNameMapping;
import static io.ballerina.openapi.core.generators.common.OASModifier.getValidNameForType;
import static io.ballerina.openapi.service.mapper.utils.CodegenUtils.resolveContractFileName;

/**
 * Main class to implement "flatten" subcommand which is used to flatten the OpenAPI definition
 * by moving all the inline schemas to the "#/components/schemas" section.
 *
 * @since 1.9.0
 */
@CommandLine.Command(
        name = "flatten",
        description = "Flatten the OpenAPI definition by moving all the inline schemas to the " +
                "\"#/components/schemas\" section."
)
public class Flatten implements BLauncherCmd {
    private static final String COMMAND_IDENTIFIER = "openapi-flatten";
    private static final String COMMA = ",";

    private static final String INFO_OUTPUT_WRITTEN_MSG = "INFO: flattened OpenAPI definition file was successfully" +
            " written to: %s%n";
    private static final String WARNING_INVALID_OUTPUT_FORMAT = "WARNING: invalid output format. The output format" +
            " should be either \"json\" or \"yaml\".Defaulting to format of the input file.";
    private static final String ERROR_INPUT_PATH_IS_REQUIRED = "ERROR: an OpenAPI definition path is required to " +
            "flatten the OpenAPI definition.";
    private static final String ERROR_INVALID_INPUT_FILE_EXTENSION = "ERROR: invalid input OpenAPI definition file " +
            "extension. The OpenAPI definition file should be in YAML or JSON format.";
    private static final String ERROR_OCCURRED_WHILE_READING_THE_INPUT_FILE = "ERROR: error occurred while reading " +
            "the OpenAPI definition file.";
    private static final String ERROR_UNSUPPORTED_OPENAPI_VERSION = "ERROR: provided OpenAPI contract version is " +
            "not supported in the tool. Use OpenAPI specification version 2 or higher";
    private static final String ERROR_OCCURRED_WHILE_PARSING_THE_INPUT_OPENAPI_FILE = "ERROR: error occurred while " +
            "parsing the OpenAPI definition file.";
    private static final String FOUND_PARSER_DIAGNOSTICS = "found the following parser diagnostic messages:";
    private static final String ERROR_OCCURRED_WHILE_WRITING_THE_OUTPUT_OPENAPI_FILE = "ERROR: error occurred while " +
            "writing the flattened OpenAPI definition file";
    private static final String ERROR_OCCURRED_WHILE_GENERATING_SCHEMA_NAMES = "ERROR: error occurred while " +
            "generating schema names";

    private PrintStream infoStream = System.out;
    private PrintStream errorStream = System.err;

    private Path targetPath = Paths.get(System.getProperty("user.dir"));
    private boolean exitWhenFinish = true;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    public boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "OpenAPI definition file path.")
    public String inputPath;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Location of the flattened OpenAPI definition.")
    private String outputPath;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the flattened OpenAPI definition file.")
    private String fileName;

    @CommandLine.Option(names = {"-f", "--format"}, description = "Output format of the flattened OpenAPI definition.")
    private String format;

    @CommandLine.Option(names = {"-t", "--tags"}, description = "Tags that need to be considered when flattening.")
    public String tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to be included when flattening.")
    public String operations;

    public Flatten() {
    }

    public Flatten(PrintStream errorStream, boolean exitWhenFinish) {
        this.errorStream = errorStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMMAND_IDENTIFIER);
            infoStream.println(commandUsageInfo);
            return;
        }

        if (Objects.isNull(inputPath) || inputPath.isBlank()) {
            errorStream.println(ERROR_INPUT_PATH_IS_REQUIRED);
            exitError();
            return;
        }

        if (inputPath.endsWith(YAML_EXTENSION) || inputPath.endsWith(JSON_EXTENSION) ||
                inputPath.endsWith(YML_EXTENSION)) {
            populateInputOptions();
            generateFlattenOpenAPI();
            return;
        }

        errorStream.println(ERROR_INVALID_INPUT_FILE_EXTENSION);
        exitError();
    }

    private void populateInputOptions() {
        if (Objects.nonNull(format)) {
            if (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml")) {
                setDefaultFormat();
                errorStream.println(WARNING_INVALID_OUTPUT_FORMAT);
            }
        } else {
            setDefaultFormat();
        }

        if (Objects.isNull(fileName)) {
            fileName = "flattened_openapi";
        }

        if (Objects.nonNull(outputPath)) {
            targetPath = Paths.get(outputPath).isAbsolute() ?
                    Paths.get(outputPath) : Paths.get(targetPath.toString(), outputPath);
        }
    }

    private void setDefaultFormat() {
        format = inputPath.endsWith(JSON_EXTENSION) ? "json" : "yaml";
    }

    private void generateFlattenOpenAPI() {
        String openAPIFileContent;
        try {
            openAPIFileContent = Files.readString(Path.of(inputPath));
        } catch (Exception e) {
            errorStream.println(ERROR_OCCURRED_WHILE_READING_THE_INPUT_FILE);
            exitError();
            return;
        }

        Optional<OpenAPI> openAPIOptional = getFlattenOpenAPI(openAPIFileContent);
        if (openAPIOptional.isEmpty()) {
            exitError();
            return;
        }
        writeFlattenOpenAPIFile(openAPIOptional.get());
    }

    private Optional<OpenAPI> getFlattenOpenAPI(String openAPIFileContent) {
        // Read the contents of the file with default parser options
        // Flattening will be done after filtering the operations
        SwaggerParseResult parserResult = new OpenAPIParser().readContents(openAPIFileContent, null,
                new ParseOptions());
        if (!parserResult.getMessages().isEmpty() &&
                parserResult.getMessages().contains(UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE)) {
            errorStream.println(ERROR_UNSUPPORTED_OPENAPI_VERSION);
            return Optional.empty();
        }

        OpenAPI openAPI = parserResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            errorStream.println(ERROR_OCCURRED_WHILE_PARSING_THE_INPUT_OPENAPI_FILE);
            if (!parserResult.getMessages().isEmpty()) {
                errorStream.println(FOUND_PARSER_DIAGNOSTICS);
                parserResult.getMessages().forEach(errorStream::println);
            }
            return Optional.empty();
        }

        Components components = openAPI.getComponents();
        List<String> existingComponentNames = Objects.nonNull(components) && Objects.nonNull(components.getSchemas()) ?
                new ArrayList<>(components.getSchemas().keySet()) : new ArrayList<>();

        filterOpenAPIOperations(openAPI);

        // Flatten the OpenAPI definition with `flattenComposedSchemas: true` and `camelCaseFlattenNaming: true`
        InlineModelResolver inlineModelResolver = new InlineModelResolver(true, true);
        inlineModelResolver.flatten(openAPI);

        return sanitizeOpenAPI(openAPI, existingComponentNames);
    }

    private void writeFlattenOpenAPIFile(OpenAPI openAPI) {
        String outputFileNameWithExt = getOutputFileName();
        try {
            CodegenUtils.writeFile(targetPath.resolve(outputFileNameWithExt),
                    outputFileNameWithExt.endsWith(JSON_EXTENSION) ? Json.pretty(openAPI) : Yaml.pretty(openAPI));
            infoStream.printf(INFO_OUTPUT_WRITTEN_MSG, targetPath.resolve(outputFileNameWithExt));
        } catch (IOException exception) {
            errorStream.println(ERROR_OCCURRED_WHILE_WRITING_THE_OUTPUT_OPENAPI_FILE);
            exitError();
        }
    }

    private String getOutputFileName() {
        return resolveContractFileName(targetPath, fileName + getFileExtension(), format.equals("json"));
    }

    private String getFileExtension() {
        return (Objects.nonNull(format) && format.equals("json")) ? JSON_EXTENSION : YAML_EXTENSION;
    }

    private void filterOpenAPIOperations(OpenAPI openAPI) {
        Filter filter = getFilter();
        if (filter.getOperations().isEmpty() && filter.getTags().isEmpty()) {
            return;
        }

        // Remove the operations which are not present in the filter
        openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap()
                .forEach((httpMethod, operation) -> {
                    if (!filter.getOperations().contains(operation.getOperationId()) &&
                            operation.getTags().stream().noneMatch(filter.getTags()::contains)) {
                        pathItem.operation(httpMethod, null);
                    }
                })
        );

        // Remove the paths which do not have any operations after filtering
        List<String> pathsToRemove = new ArrayList<>();
        openAPI.getPaths().forEach((path, pathItem) -> {
            if (pathItem.readOperationsMap().isEmpty()) {
                pathsToRemove.add(path);
            }
        });
        pathsToRemove.forEach(openAPI.getPaths()::remove);
    }

    private Optional<OpenAPI> sanitizeOpenAPI(OpenAPI openAPI, List<String> existingComponentNames) {
        Map<String, String> proposedNameMapping = getProposedNameMapping(openAPI, existingComponentNames);
        if (proposedNameMapping.isEmpty()) {
            return Optional.of(openAPI);
        }

        SwaggerParseResult parserResult = OASModifier.getOASWithSchemaNameModification(openAPI, proposedNameMapping);
        openAPI = parserResult.getOpenAPI();
        if (Objects.isNull(openAPI)) {
            errorStream.println(ERROR_OCCURRED_WHILE_GENERATING_SCHEMA_NAMES);
            if (!parserResult.getMessages().isEmpty()) {
                errorStream.println(FOUND_PARSER_DIAGNOSTICS);
                parserResult.getMessages().forEach(errorStream::println);
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

    private Filter getFilter() {
        List<String> tagList = new ArrayList<>();
        List<String> operationList = new ArrayList<>();

        if (Objects.nonNull(tags) && !tags.isEmpty()) {
            tagList.addAll(Arrays.asList(tags.split(COMMA)));
        }

        if (Objects.nonNull(operations) && !operations.isEmpty()) {
            operationList.addAll(Arrays.asList(operations.split(COMMA)));
        }

        return new Filter(tagList, operationList);
    }

    @Override
    public String getName() {
        return OPENAPI_FLATTEN_CMD;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        //This is the long description of the command and all handle within help command
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        //This is the usage description of the command and all handle within help command
    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {
        //This is not used in this command
    }

    private void exitError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }
}
