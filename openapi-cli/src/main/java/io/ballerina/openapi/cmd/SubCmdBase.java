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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.openapi.core.generators.common.InlineModelResolver;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.service.mapper.utils.CodegenUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.cmd.CmdConstants.JSON_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.YAML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SLASH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE;
import static io.ballerina.openapi.service.mapper.utils.CodegenUtils.resolveContractFileName;

/**
 * Abstract class representing the {@link Flatten} and {@link Align} sub command
 * implementations.
 *
 * @since 2.2.0
 */
public abstract class SubCmdBase implements BLauncherCmd {

    private static final String JSON = "json";
    private static final String YAML = "yaml";
    public static final String X_ORIGINAL_SWAGGER_VERSION = "x-original-swagger-version";
    public static final String V2 = "2.0";

    public enum CommandType {
        FLATTEN("flatten"),
        ALIGN("align");

        private final String name;

        CommandType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String COMMAND_IDENTIFIER = "openapi-%s";
    protected static final String COMMA = ",";

    private static final String INFO_OUTPUT_WRITTEN_MSG = "INFO: %s OpenAPI definition file was successfully" +
            " written to: %s%n";
    private static final String WARNING_INVALID_OUTPUT_FORMAT = "WARNING: invalid output format. The output format" +
            " should be either \"json\" or \"yaml\".Defaulting to format of the input file";
    private static final String ERROR_INPUT_PATH_IS_REQUIRED = "ERROR: an OpenAPI definition path is required to " +
            "%s the OpenAPI definition%n";
    private static final String ERROR_INVALID_INPUT_FILE_EXTENSION = "ERROR: invalid input OpenAPI definition file " +
            "extension. The OpenAPI definition file should be in YAML or JSON format";
    private static final String ERROR_OCCURRED_WHILE_READING_THE_INPUT_FILE = "ERROR: error occurred while reading " +
            "the OpenAPI definition file";
    private static final String ERROR_UNSUPPORTED_OPENAPI_VERSION = "ERROR: provided OpenAPI contract version is " +
            "not supported in the tool. Use OpenAPI specification version 2 or higher";
    private static final String ERROR_OCCURRED_WHILE_PARSING_THE_INPUT_OPENAPI_FILE = "ERROR: error occurred while " +
            "parsing the OpenAPI definition file";
    protected static final String FOUND_PARSER_DIAGNOSTICS = "found the following parser diagnostic messages:";
    private static final String ERROR_OCCURRED_WHILE_WRITING_THE_OUTPUT_OPENAPI_FILE = "ERROR: error occurred while " +
            "writing the %s OpenAPI definition file%n";
    private static final String WARNING_SWAGGER_V2_FOUND = "WARNING: Swagger version 2.0 found in the OpenAPI " +
            "definition. The generated OpenAPI definition will be in OpenAPI version 3.0.x";

    private static final PrintStream infoStream = System.out;
    private PrintStream errorStream = System.err;

    private Path targetPath = Paths.get(System.getProperty("user.dir"));
    private boolean exitWhenFinish = true;
    private final CommandType cmdType;
    private final String infoMsgPrefix;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    public boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "OpenAPI definition file path.")
    public String inputPath;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Location of the aligned OpenAPI definition.")
    private String outputPath;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name of the aligned OpenAPI definition file.")
    private String fileName;

    @CommandLine.Option(names = {"-f", "--format"}, description = "Output format of the aligned OpenAPI definition.")
    private String format;

    @CommandLine.Option(names = {"-t", "--tags"}, description = "Tags that need to be considered when sanitizing.")
    public String tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to be included when sanitizing.")
    public String operations;

    protected SubCmdBase(CommandType cmdType, String infoMsgPrefix) {
        this.cmdType = cmdType;
        this.infoMsgPrefix = infoMsgPrefix;
    }

    protected SubCmdBase(CommandType cmdType, String infoMsgPrefix, PrintStream errorStream, boolean exitWhenFinish) {
        this.cmdType = cmdType;
        this.errorStream = errorStream;
        this.exitWhenFinish = exitWhenFinish;
        this.infoMsgPrefix = infoMsgPrefix;
    }

    public void printHelpText() {
        String commandIdentifier = String.format(COMMAND_IDENTIFIER, cmdType.getName());
        String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(commandIdentifier);
        infoStream.println(commandUsageInfo);
    }

    @Override
    public void execute() {
        if (helpFlag) {
            printHelpText();
            return;
        }

        if (Objects.isNull(inputPath) || inputPath.isBlank()) {
            errorStream.printf(ERROR_INPUT_PATH_IS_REQUIRED, cmdType.getName());
            exitError();
            return;
        }

        if (inputPath.endsWith(YAML_EXTENSION) || inputPath.endsWith(JSON_EXTENSION) ||
                inputPath.endsWith(YML_EXTENSION)) {
            populateInputOptions();
            generateOpenAPI();
            return;
        }

        errorStream.println(ERROR_INVALID_INPUT_FILE_EXTENSION);
        exitError();
    }

    public void printError(String message) {
        errorStream.println(message);
    }

    private void generateOpenAPI() {
        String openAPIFileContent;
        try {
            openAPIFileContent = Files.readString(Path.of(inputPath));
        } catch (Exception e) {
            errorStream.println(ERROR_OCCURRED_WHILE_READING_THE_INPUT_FILE);
            exitError();
            return;
        }

        Optional<OpenAPI> openAPIOptional = generate(openAPIFileContent);
        if (openAPIOptional.isEmpty()) {
            exitError();
            return;
        }
        writeGeneratedOpenAPIFile(openAPIOptional.get());
    }

    public abstract Optional<OpenAPI> generate(String openAPIFileContent);

    public abstract String getDefaultFileName();

    public Optional<OpenAPI> getFlattenOpenAPI(OpenAPI openAPI) {
        // Flatten the OpenAPI definition with `flattenComposedSchemas: true` and `skipMatches: false`
        InlineModelResolver inlineModelResolver = new InlineModelResolver(true, false);
        inlineModelResolver.flatten(openAPI);
        return Optional.of(openAPI);
    }

    public Optional<OpenAPI> getFilteredOpenAPI(String openAPIFileContent) {
        // Read the contents of the file with default parser options
        // Flattening will be done after filtering the operations
        ParseOptions parseOptions = new ParseOptions();
        SwaggerParseResult parserResult = new OpenAPIParser().readContents(openAPIFileContent, null,
                parseOptions);
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

        if (Objects.nonNull(openAPI.getExtensions()) && isSwaggerV2(openAPI)) {
            errorStream.println(WARNING_SWAGGER_V2_FOUND);
        }

        filterOpenAPIOperations(openAPI);
        removeDefaultServers(openAPI);
        return Optional.of(openAPI);
    }

    private static boolean isSwaggerV2(OpenAPI openAPI) {
        return openAPI.getExtensions().containsKey(X_ORIGINAL_SWAGGER_VERSION) &&
                openAPI.getExtensions().get(X_ORIGINAL_SWAGGER_VERSION).toString().trim().equals(V2);
    }

    @Override
    public String getName() {
        return cmdType.getName();
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

    private void populateInputOptions() {
        if (Objects.nonNull(format)) {
            if (!format.equalsIgnoreCase(JSON) && !format.equalsIgnoreCase("yaml")) {
                setDefaultFormat();
                errorStream.println(WARNING_INVALID_OUTPUT_FORMAT);
            }
        } else {
            setDefaultFormat();
        }

        if (Objects.isNull(fileName)) {
            fileName = String.format(getDefaultFileName(), cmdType.getName());
        }

        if (Objects.nonNull(outputPath)) {
            targetPath = Paths.get(outputPath).isAbsolute() ?
                    Paths.get(outputPath) : Paths.get(targetPath.toString(), outputPath);
        }
    }

    private void setDefaultFormat() {
        format = inputPath.endsWith(JSON_EXTENSION) ? JSON : YAML;
    }

    private void exitError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }

    private void writeGeneratedOpenAPIFile(OpenAPI openAPI) {
        String outputFileNameWithExt = getOutputFileName();
        try {
            CodegenUtils.writeFile(targetPath.resolve(outputFileNameWithExt),
                    outputFileNameWithExt.endsWith(JSON_EXTENSION) ? Json.pretty(openAPI) : Yaml.pretty(openAPI));
            infoStream.printf(INFO_OUTPUT_WRITTEN_MSG, infoMsgPrefix, targetPath.resolve(outputFileNameWithExt));
        } catch (IOException exception) {
            errorStream.printf(ERROR_OCCURRED_WHILE_WRITING_THE_OUTPUT_OPENAPI_FILE, infoMsgPrefix);
            exitError();
        }
    }

    private String getOutputFileName() {
        return resolveContractFileName(targetPath, fileName + getFileExtension(), format.equals(JSON));
    }

    private String getFileExtension() {
        return (Objects.nonNull(format) && format.equals(JSON)) ? JSON_EXTENSION : YAML_EXTENSION;
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

    private static void removeDefaultServers(OpenAPI openAPI) {
        List<Server> servers = openAPI.getServers();
        if (Objects.nonNull(servers) && servers.size() == 1 && servers.getFirst().equals(new Server().url(SLASH))) {
            openAPI.setServers(null);
        }
    }
}
