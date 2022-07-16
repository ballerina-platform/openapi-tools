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
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.converter.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.openapi.OpenApiConverter;
import org.ballerinalang.formatter.core.FormatterException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.openapi.generators.GeneratorConstants.BAL_EXTENSION;
import static io.ballerina.openapi.generators.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.generators.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.generators.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * Main class to implement "openapi" command for ballerina. Commands for Client Stub, Service file and OpenApi contract
 * generation.
 */
@CommandLine.Command(
        name = "openapi",
        description = "Generates Ballerina service/client for OpenAPI contract and OpenAPI contract for Ballerina" +
                "Service."
)
public class OpenApiCmd implements BLauncherCmd {
    private static final String CMD_NAME = "openapi";
    private PrintStream outStream;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));
    private Path targetOutputPath;
    private boolean exitWhenFinish;
    private boolean isClientResources;

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Generating the client and service both files")
    private boolean inputPath;

    @CommandLine.Option(names = {"--license"}, description = "Location of the file which contains the license header")
    private String licenseFilePath;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Location of the generated Ballerina service, " +
            "client and model files.")
    private String outputPath;

    @CommandLine.Option(names = {"--mode"}, description = "Generate only service file or client file according to the" +
            " given mode type")
    private String mode;

    @CommandLine.Option(names = {"-n", "--nullable"}, description = "Generate the code by setting nullable true")
    private boolean nullable;

    @CommandLine.Option(names = {"-s", "--service"}, description = "Service name that need to documented as openapi " +
            "contract")
    private String service;

    @CommandLine.Option(names = {"--tags"}, description = "Tag that need to write service")
    private String tags;

    @CommandLine.Option(names = {"--operations"}, description = "Operations that need to write service")
    private String operations;

    @CommandLine.Option(names = {"--service-name"}, description = "Service name for generated files")
    private String generatedServiceName;

    @CommandLine.Option(names = {"--json"}, description = "Generate json file")
    private boolean generatedFileType;

    @CommandLine.Option(names = {"--with-tests"}, hidden = true, description = "Generate test files")
    private boolean includeTestFiles;

    @CommandLine.Option(names = {"--client-methods"}, hidden = true, description = "Generate the client" +
            " using resource functions (instead of using remote functions)")
    private String generateClientMethods;

    @CommandLine.Parameters
    private List<String> argList;

    public OpenApiCmd() {
        this.outStream = System.err;
        this.executionPath = Paths.get(System.getProperty("user.dir"));
        this.exitWhenFinish = true;
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

        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }
        //Check if cli input argument is present
        if (inputPath) {
            //Check if an OpenApi definition is provided
            if (argList == null) {
                outStream.println(OpenApiMesseges.MESSAGE_FOR_MISSING_INPUT);
                exitError(this.exitWhenFinish);
                return;
            }
            // If given input is yaml contract, it generates service file and client stub
            // else if given ballerina service file it generates openapi contract file
            // else it generates error message to enter correct input file
            String fileName = argList.get(0);
            if (fileName.endsWith(YAML_EXTENSION) || fileName.endsWith(JSON_EXTENSION) ||
                    fileName.endsWith(YML_EXTENSION)) {
                List<String> tag = new ArrayList<>();
                List<String> operation = new ArrayList<>();
                if (tags != null) {
                    tag.addAll(Arrays.asList(tags.split(",")));
                }
                if (operations != null) {
                    String[] ids = operations.split(",");
                    List<String> normalizedOperationIds =
                            Arrays.stream(ids).map(operationId -> getValidName(operationId, false))
                                    .collect(Collectors.toList());
                    operation.addAll(normalizedOperationIds);
                }
                Filter filter = new Filter(tag, operation);

                // Add the resource flag enable
                 isClientResources = generateClientMethods != null && !generateClientMethods.isBlank() &&
                                (generateClientMethods.equals("resource"));
                if (isClientResources && mode != null && mode.equals("service")) {
                    // Exit the code generation process
                    outStream.println("'--resource-functions' option is only available with the client mode.");
                    exitError(this.exitWhenFinish);
                }
                try {
                    openApiToBallerina(fileName, filter);
                } catch (IOException e) {
                    outStream.println(e.getLocalizedMessage());
                    exitError(this.exitWhenFinish);
                }
            } else if (fileName.endsWith(BAL_EXTENSION)) {
                // Add the resource flag enable
                if (!generateClientMethods.isBlank()) {
                    // Exit the code generation process
                    outStream.println("'--resource-functions' option is only available with the client mode.");
                    exitError(this.exitWhenFinish);
                }
                ballerinaToOpenApi(fileName);
            } else {
                outStream.println(OpenApiMesseges.MESSAGE_FOR_MISSING_INPUT);
                exitError(this.exitWhenFinish);
            }
        } else {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            exitError(this.exitWhenFinish);
            return;
        }

        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * This util method to generate openApi contract based on the given service ballerina file.
     * @param fileName  input resource file
     */
    private void ballerinaToOpenApi(String fileName) {
        List<OpenAPIConverterDiagnostic> errors = new ArrayList<>();
        final File balFile = new File(fileName);
        Path balFilePath = null;
        try {
            balFilePath = Paths.get(balFile.getCanonicalPath());
        } catch (IOException e) {
            DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_108;
            ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                    message.getDescription(), null,  e.getLocalizedMessage());
            errors.add(error);
        }
        getTargetOutputPath();
        // Check service name it is mandatory
        OpenApiConverter openApiConverter = new OpenApiConverter();
        openApiConverter.generateOAS3DefinitionsAllService(balFilePath, targetOutputPath, service,
                generatedFileType);
        errors.addAll(openApiConverter.getErrors());
        if (!errors.isEmpty()) {
            for (OpenAPIConverterDiagnostic error: errors) {
                if (error instanceof ExceptionDiagnostic) {
                    this.outStream = System.err;
                    ExceptionDiagnostic exceptionDiagnostic = (ExceptionDiagnostic) error;
                    OpenAPIDiagnostic diagnostic = CmdUtils.constructOpenAPIDiagnostic(exceptionDiagnostic.getCode(),
                            exceptionDiagnostic.getMessage(), exceptionDiagnostic.getDiagnosticSeverity(),
                            exceptionDiagnostic.getLocation().orElse(null));
                    outStream.println(diagnostic.toString());
                    exitError(this.exitWhenFinish);
                } else if (error instanceof IncompatibleResourceDiagnostic) {
                    IncompatibleResourceDiagnostic incompatibleError = (IncompatibleResourceDiagnostic) error;
                    OpenAPIDiagnostic diagnostic = CmdUtils.constructOpenAPIDiagnostic(incompatibleError.getCode(),
                            incompatibleError.getMessage(), incompatibleError.getDiagnosticSeverity(),
                            incompatibleError.getLocation().get());
                    outStream.println(diagnostic.toString());
                }
            }
        }
    }

    /**
     * This util method for generating service and client stub using given contract file.
     *
     * @param fileName input resource file
     */
    private void openApiToBallerina(String fileName, Filter filter) throws IOException {
        CodeGenerator generator = new CodeGenerator();
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
        if (mode != null) {
            switch (mode) {
                case "service":
                    generateServiceFile(generator, serviceName, resourcePath, filter);
                    break;
                case "client":
                    generatesClientFile(generator, resourcePath, filter, this.isClientResources);
                    break;
                default:
                    break;
            }
        } else {
            generateBothFiles(generator, serviceName, resourcePath, filter, this.isClientResources);
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
            if (this.licenseFilePath != null && !this.licenseFilePath.isBlank()) {
                Path filePath = Paths.get((new File(this.licenseFilePath).getCanonicalPath()));
                licenseHeader = Files.readString(Paths.get(filePath.toString()));
                if (!licenseHeader.endsWith("\n")) {
                    licenseHeader = licenseHeader + "\n\n";
                } else if (!licenseHeader.endsWith("\n\n")) {
                    licenseHeader = licenseHeader + "\n";
                }
            }
        } catch (IOException e) {
            outStream.println("Invalid license file path : " + this.licenseFilePath +
                    ". " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
        return licenseHeader;
    }

    /**
     * A Util to Client generation.
     * @param generator         generator object
     * @param resourcePath      resource Path
     * @param isResource        boolean value for the
     */
    private void generatesClientFile(CodeGenerator generator,  Path resourcePath, Filter filter,
                                     boolean isResource) {
        try {
            generator.generateClient(resourcePath.toString(), targetOutputPath.toString(), filter, nullable,
                    isResource);
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            if (e.getLocalizedMessage() != null) {
                outStream.println(e.getLocalizedMessage());
                exitError(this.exitWhenFinish);
            } else {
                outStream.println(OpenApiMesseges.OPENAPI_CLIENT_EXCEPTION);
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
    private void generateServiceFile(CodeGenerator generator, String serviceName, Path resourcePath, Filter filter) {

        try {
            assert resourcePath != null;
            generator.generateService(resourcePath.toString(), serviceName, targetOutputPath.toString(), filter,
                    nullable);
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
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
    private void generateBothFiles(CodeGenerator generator, String fileName, Path resourcePath, Filter filter,
                                   boolean generateClientResourceFunctions) {
        try {
            assert resourcePath != null;
            generator.generateBothFiles(GeneratorConstants.GenType.GEN_BOTH,
                    resourcePath.toString(), fileName, targetOutputPath.toString(), filter, nullable,
                    generateClientResourceFunctions);
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            outStream.println("Error occurred when generating service for openAPI contract at " + argList.get(0) + "." +
                    " " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
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
