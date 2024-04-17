/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.projects.PackageManifest.Platform;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.toml.syntax.tree.AbstractNodeFactory;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.validator.SampleNodeGenerator;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.FormatterException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static io.ballerina.openapi.cmd.CmdConstants.BAL_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.CLIENT;
import static io.ballerina.openapi.cmd.CmdConstants.JSON_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.REMOTE;
import static io.ballerina.openapi.cmd.CmdConstants.RESOURCE;
import static io.ballerina.openapi.cmd.CmdConstants.SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.YAML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.YML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdUtils.addNewLine;
import static io.ballerina.openapi.cmd.CmdUtils.validateBallerinaProject;
import static io.ballerina.openapi.cmd.ErrorMessages.NOT_A_BALLERINA_PACKAGE;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

/**
 * Main class to implement "openapi" command for ballerina. Commands for Client Stub, Service file and OpenApi contract
 * generation.
 */
@CommandLine.Command(
        name = "openapi",
        description = "Generate the Ballerina sources for a given OpenAPI definition and vice versa.",
        subcommands = {Add.class}
)
public class OpenApiCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";
    private PrintStream outStream;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));
    private Path targetOutputPath;
    private boolean exitWhenFinish;
    private boolean clientResourceMode;
    private boolean statusCodeBinding;
    private Path ballerinaTomlPath;

    @CommandLine.Mixin
    private BaseCmd baseCmd = new BaseCmd();

    @CommandLine.Option(names = {"-o", "--output"}, description = "Location of the generated Ballerina service, " +
            "client and model files.")
    private String outputPath;

    @CommandLine.Option(names = {"-s", "--service"}, description = "Service name that need to documented as openapi " +
            "contract")
    private String service;

    @CommandLine.Option(names = {"--service-name"}, description = "Service name for generated files")
    private String generatedServiceName;

    @CommandLine.Option(names = {"--json"}, description = "Generate json file")
    private boolean generatedFileType;

    @CommandLine.Option(names = {"--with-tests"}, hidden = true, description = "Generate test files")
    private boolean includeTestFiles;

    @CommandLine.Option(names = {"--with-service-type"}, hidden = true, description = "Generate service type")
    private boolean generateServiceType;

    @CommandLine.Option(names = {"--without-data-binding"}, hidden = true,
            description = "Generate service without data binding")
    private boolean generateWithoutDataBinding;

    @CommandLine.Parameters
    private List<String> argList;

    public OpenApiCmd() {
        this.outStream = System.err;
        this.executionPath = Paths.get(System.getProperty("user.dir"));
        this.exitWhenFinish = true;
    }

    private String getVersion() throws IOException {
        try (InputStream inputStream = OpenApiCmd.class.getClassLoader().getResourceAsStream(
                "openapi-client-native-version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("version");
        } catch (IOException exception) {
            throw new IOException("error occurred while reading version from version.properties: " +
                    exception.getMessage());
        }
    }

    public OpenApiCmd(PrintStream outStream, Path executionDir) {
        new OpenApiCmd(outStream, executionDir, true);
    }

    public OpenApiCmd(PrintStream outStream, Path executionDir, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitWhenFinish = exitWhenFinish;
    }
    @Override
    public void execute() {
        if (isHelp()) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }
        //Check if cli input argument is present
        //Check if an OpenApi definition is provided
        if (baseCmd.inputPath == null || baseCmd.inputPath.isBlank()) {
            outStream.println(ErrorMessages.MISSING_CONTRACT_PATH);
            exitError(this.exitWhenFinish);
            return;
        }
        // If given input is yaml contract, it generates service file and client stub
        // else if given ballerina service file it generates openapi contract file
        // else it generates error message to enter correct input file
        String fileName = baseCmd.inputPath;
        if (fileName.endsWith(YAML_EXTENSION) || fileName.endsWith(JSON_EXTENSION) ||
                fileName.endsWith(YML_EXTENSION)) {
            List<String> tag = new ArrayList<>();
            List<String> operation = new ArrayList<>();
            if (baseCmd.tags != null) {
                tag.addAll(Arrays.asList(baseCmd.tags.split(",")));
            }
            if (baseCmd.operations != null) {
                String[] ids = baseCmd.operations.split(",");
                List<String> normalizedOperationIds =
                        Arrays.stream(ids).map(operationId -> getValidName(operationId, false))
                                .collect(Collectors.toList());
                operation.addAll(normalizedOperationIds);
            }
            Filter filter = new Filter(tag, operation);

            if (baseCmd.generateClientMethods != null && !baseCmd.generateClientMethods.isBlank() &&
                    (!baseCmd.generateClientMethods.equals(RESOURCE) &&
                            !baseCmd.generateClientMethods.equals(REMOTE))) {
                // Exit the code generation process
                outStream.println("'--client-methods' only supports `remote` or `resource` options.");
                exitError(this.exitWhenFinish);
            }
            // Add the resource flag enable
            clientResourceMode = baseCmd.generateClientMethods == null || baseCmd.generateClientMethods.isBlank() ||
                    (!baseCmd.generateClientMethods.equals(REMOTE));

            if (!clientResourceMode && baseCmd.mode != null && baseCmd.mode.equals(SERVICE)) {
                // Exit the code generation process
                outStream.println("'--client-methods' option is only available in client generation mode.");
                exitError(this.exitWhenFinish);
            }

            if (generateWithoutDataBinding && baseCmd.mode != null && baseCmd.mode.equals(CLIENT)) {
                // Exit the code generation process
                outStream.println("'--without-data-binding' option is only available in service generation mode.");
                exitError(this.exitWhenFinish);
            }

            if (baseCmd.statusCodeBinding) {
                if (baseCmd.mode != null && baseCmd.mode.equals(SERVICE)) {
                    outStream.println("ERROR: the '--status-code-binding' option is only available in client " +
                            "generation mode.");
                    exitError(this.exitWhenFinish);
                }

                Optional<Path> ballerinaTomlPath = validateBallerinaProject(executionPath, outStream,
                        NOT_A_BALLERINA_PACKAGE, false);
                if (ballerinaTomlPath.isEmpty()) {
                    outStream.printf(NOT_A_BALLERINA_PACKAGE, executionPath.toAbsolutePath());
                } else {
                    statusCodeBinding = true;
                    this.ballerinaTomlPath = ballerinaTomlPath.get();
                }
            }

            try {
                openApiToBallerina(fileName, filter);
            } catch (IOException e) {
                outStream.println(e.getLocalizedMessage());
                exitError(this.exitWhenFinish);
            }
        } else if (fileName.endsWith(BAL_EXTENSION)) {
            // Add the resource flag enable
            if (baseCmd.generateClientMethods != null && !baseCmd.generateClientMethods.isBlank()) {
                // Exit the code generation process
                outStream.println("'--client-methods' option is only available in client generation mode.");
                exitError(this.exitWhenFinish);
            }
            ballerinaToOpenApi(fileName);
        } else {
            outStream.println(ErrorMessages.MISSING_CONTRACT_PATH);
            exitError(this.exitWhenFinish);
        }

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private boolean isHelp() {
        return baseCmd.helpFlag || (argList != null && argList.get(0).equals("help"))
                || (argList == null && baseCmd.inputPath == null);
    }

    /**
     * This util method to generate openApi contract based on the given service ballerina file.
     * @param fileName  input resource file
     */
    private void ballerinaToOpenApi(String fileName) {
        List<OpenAPIMapperDiagnostic> mapperDiagnostics = new ArrayList<>();
        final File balFile = new File(fileName);
        Path balFilePath = null;
        try {
            balFilePath = Paths.get(balFile.getCanonicalPath());
        } catch (IOException e) {
            ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_108,
                    e.getLocalizedMessage());
            mapperDiagnostics.add(error);
        }
        if (balFilePath == null || !Files.exists(balFilePath)) {
            outStream.println("given Ballerina file does not exist: " + fileName);
            exitError(this.exitWhenFinish);
        }
        getTargetOutputPath();
        // Check service name it is mandatory
        OASContractGenerator openApiConverter = new OASContractGenerator();
        openApiConverter.generateOAS3DefinitionsAllService(balFilePath, targetOutputPath, service,
                generatedFileType);
        mapperDiagnostics.addAll(openApiConverter.getDiagnostics());
        boolean exitWithError = false;
        if (mapperDiagnostics.stream().anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.getDiagnosticSeverity()))) {
            this.outStream = System.err;
            exitWithError = true;
        }
        if (!mapperDiagnostics.isEmpty()) {
            for (OpenAPIMapperDiagnostic mapperDiagnostic: mapperDiagnostics) {
                OpenAPIDiagnostic diagnostic = CmdUtils.constructOpenAPIDiagnostic(mapperDiagnostic.getCode(),
                        mapperDiagnostic.getMessage(), mapperDiagnostic.getDiagnosticSeverity(),
                        mapperDiagnostic.getLocation().orElse(null));
                outStream.println(diagnostic);
            }
        }
        if (exitWithError) {
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * This util method for generating service and client stub using given contract file.
     *
     * @param fileName input resource file
     */
    private void openApiToBallerina(String fileName, Filter filter) throws IOException {
        boolean skipDependecyUpdate = true;
        if (statusCodeBinding && Objects.nonNull(ballerinaTomlPath)) {
            skipDependecyUpdate = clientNativeDependencyAlreadyExist(getVersion());
        }
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        generator.setLicenseHeader(this.setLicenseHeader());
        generator.setIncludeTestFiles(this.includeTestFiles);
        final File openApiFile = new File(fileName);
        String serviceName;
        if (generatedServiceName != null) {
            serviceName = generatedServiceName;
        } else {
            serviceName = openApiFile.getName().split("\\.")[0];
        }
        getTargetOutputPath();
        Path resourcePath = Paths.get(openApiFile.getCanonicalPath());
        if (baseCmd.nullable) {
            outStream.println("WARNING: All the constraints in the OpenAPI contract will be ignored when generating" +
                    " the Ballerina client/service with the `--nullable` option");
        }
        if (baseCmd.mode != null) {
            switch (baseCmd.mode) {
                case "service":
                    generateServiceFile(generator, serviceName, resourcePath, filter);
                    break;
                case "client":
                    generatesClientFile(generator, resourcePath, filter, clientResourceMode, statusCodeBinding);
                    break;
                default:
                    break;
            }
        } else {
            generateBothFiles(generator, serviceName, resourcePath, filter, clientResourceMode, statusCodeBinding);
        }
        if (!skipDependecyUpdate) {
            updateBallerinaTomlWithClientNativeDependency();
        }
    }

    /**
     * This util is to take the resource Path.
     *
     * @param resourceFile      resource file path
     * @return path of given resource file
     */
    public Path getRelativePath(File resourceFile, String targetOutputPath) {
        Path resourcePath = Paths.get(resourceFile.getAbsoluteFile().getParentFile().toString());
        Path targetPath = Paths.get(targetOutputPath).toAbsolutePath();
        try {
            Path relativePath = targetPath.relativize(resourcePath);
            return relativePath.resolve(resourceFile.getName());
        } catch (IllegalArgumentException exception) {
            return resourcePath.resolve(resourceFile.getName());
        }
    }

    /**
     * This util is to get the output Path.
     */
    private void getTargetOutputPath() {
        targetOutputPath = executionPath;
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath);
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
            }
        }
    }
    /**
     * This util is to set the license header content which is to be added at the beginning of the ballerina files.
     */
    private String setLicenseHeader() {
        String licenseHeader = "";
        try {
            if (this.baseCmd.licenseFilePath != null && !this.baseCmd.licenseFilePath.isBlank()) {
                Path filePath = Paths.get((new File(this.baseCmd.licenseFilePath).getCanonicalPath()));
                licenseHeader = Files.readString(Paths.get(filePath.toString()));
                if (!licenseHeader.endsWith("\n")) {
                    licenseHeader = licenseHeader + "\n\n";
                } else if (!licenseHeader.endsWith("\n\n")) {
                    licenseHeader = licenseHeader + "\n";
                }
            }
        } catch (IOException e) {
            outStream.println("Invalid license file path : " + this.baseCmd.licenseFilePath +
                    ". " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
        return licenseHeader;
    }

    /**
     * A Util to Client generation.
     * @param generator         generator object
     * @param resourcePath      resource Path
     * @param resourceMode      flag for client resource method generation
     */
    private void generatesClientFile(BallerinaCodeGenerator generator, Path resourcePath, Filter filter,
                                     boolean resourceMode, boolean statusCodeBinding) {
        try {
            generator.generateClient(resourcePath.toString(), targetOutputPath.toString(), filter, baseCmd.nullable,
                    resourceMode, statusCodeBinding);
        } catch (IOException | FormatterException | BallerinaOpenApiException |
                 OASTypeGenException e) {
            if (e.getLocalizedMessage() != null) {
                outStream.println(e.getLocalizedMessage());
                exitError(this.exitWhenFinish);
            } else {
                outStream.println(ErrorMessages.CLIENT_GENERATION_FAILED);
                exitError(this.exitWhenFinish);
            }
        }
    }

    /**
     * A util to generate service file.
     * @param generator     generator object
     * @param serviceName   service name uses for naming the generated file
     * @param resourcePath  resource Path
     */
    private void generateServiceFile(BallerinaCodeGenerator generator, String serviceName, Path resourcePath,
                                     Filter filter) {

        try {
            assert resourcePath != null;
            generator.generateService(resourcePath.toString(), serviceName, targetOutputPath.toString(), filter,
                    baseCmd.nullable, generateServiceType, generateWithoutDataBinding);
        } catch (IOException | FormatterException | BallerinaOpenApiException e) {
            outStream.println("Error occurred when generating service for OpenAPI contract at " + argList.get(0) +
                    ". " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * This util method is to generate both service and client stub files based on the given yaml contract file.
     * @param generator         generator object
     * @param fileName          service name  use for naming the files
     * @param resourcePath      resource path
     */
    private void generateBothFiles(BallerinaCodeGenerator generator, String fileName, Path resourcePath,
                                   Filter filter, boolean generateClientResourceFunctions, boolean statusCodeBinding) {
        try {
            assert resourcePath != null;
            generator.generateClientAndService(resourcePath.toString(), fileName, targetOutputPath.toString(), filter,
                    baseCmd.nullable, generateClientResourceFunctions, generateServiceType, generateWithoutDataBinding,
                    statusCodeBinding);
        } catch (BallerinaOpenApiException e) {
            outStream.println(e.getMessage());
            exitError(this.exitWhenFinish);
        } catch (IOException | FormatterException | OASTypeGenException | ClientException e) {
            outStream.println("Error occurred when generating service for openAPI contract at " + baseCmd.inputPath +
                    "." + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
    }

    private void updateBallerinaTomlWithClientNativeDependency() {
        try {
            String version = getVersion();
            TextDocument configDocument = TextDocuments.from(Files.readString(ballerinaTomlPath));
            SyntaxTree syntaxTree = SyntaxTree.from(configDocument);
            DocumentNode rootNode = syntaxTree.rootNode();
            NodeList<DocumentMemberDeclarationNode> nodeList = rootNode.members();
            NodeList<DocumentMemberDeclarationNode> tomlMembers = AbstractNodeFactory.createEmptyNodeList();
            for (DocumentMemberDeclarationNode node : nodeList) {
                tomlMembers = tomlMembers.add(node);
            }
            tomlMembers = addNewLine(tomlMembers, 1);
            tomlMembers = populateClientNativeDependency(tomlMembers, version);
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            DocumentNode documentNode = NodeFactory.createDocumentNode(tomlMembers, eofToken);
            TextDocument textDocument = TextDocuments.from(documentNode.toSourceCode());
            String content = SyntaxTree.from(textDocument).toSourceCode();
            try (FileWriter writer = new FileWriter(ballerinaTomlPath.toString(), StandardCharsets.UTF_8)) {
                writer.write(content);
                outStream.print(ErrorMessages.TOML_UPDATED_WITH_CLIENT_NATIVE_DEPENDENCY);
            }
        } catch (IOException e) {
            outStream.println("ERROR: error occurred when updating Ballerina.toml file. " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
    }

    private boolean clientNativeDependencyAlreadyExist(String version) {
        Project project = ProjectLoader.loadProject(executionPath);
        Map<String, Platform> platforms = project.currentPackage().manifest().platforms();
        if (Objects.nonNull(platforms) && platforms.containsKey("java17")) {
            Optional<Map<String, Object>> nativeDependency = platforms.get("java17").dependencies().stream().filter(
                    dependency -> dependency.containsKey("groupId") &&
                            dependency.get("groupId").equals("io.ballerina.openapi")
                            && dependency.containsKey("artifactId") &&
                            dependency.get("artifactId").equals("client-native")
            ).findFirst();
            if (nativeDependency.isEmpty()) {
                return false;
            }

            if (nativeDependency.get().containsKey("version") &&
                    nativeDependency.get().get("version").equals(version)) {
                outStream.println("INFO: the 'Ballerina.toml' file is already updated with the OpenAPI client native " +
                        "dependency.");
            } else {
                outStream.println("ERROR: the 'Ballerina.toml' file is already updated with the OpenAPI client " +
                        "native dependency but the version is different from the current version. Please remove the" +
                        " existing dependency and try again.");
                exitError(this.exitWhenFinish);
            }
            return true;
        }
        return false;
    }

    private NodeList<DocumentMemberDeclarationNode> populateClientNativeDependency(
            NodeList<DocumentMemberDeclarationNode> tomlMembers, String version) {
        String desc = "This dependency is added automatically by the OpenAPI tool. DO NOT REMOVE UNLESS REQUIRED";
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createTableArray("platform.java17.dependency", desc));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("groupId", "io.ballerina.openapi", null));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("artifactId", "client-native", null));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("version", version, null));
        return tomlMembers;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("cli-help/ballerina-openapi.help");
        try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(inputStreamREader)) {
            String content = br.readLine();
            out.append(content);
            while ((content = br.readLine()) != null) {
                out.append('\n').append(content);
            }
        } catch (IOException ignore) {
        }
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    /**
     * Exit with error code 1.
     *
     * @param exit Whether to exit or not.
     */
    private static void exitError(boolean exit) {
        if (exit) {
            Runtime.getRuntime().exit(1);
        }
    }
}
