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
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.openapi.OpenApiConverter;
import io.ballerina.projects.ProjectException;
import org.ballerinalang.formatter.core.FormatterException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (fileName.endsWith(".yaml") || fileName.endsWith(".json") || fileName.endsWith(".yml")) {
                List<String> tag = new ArrayList<>();
                List<String> operation = new ArrayList<>();
                if (tags != null) {
                     tag.addAll(Arrays.asList(tags.split(",")));
                }
                if (operations != null) {
                    operation.addAll(Arrays.asList(operations.split(",")));
                }
                Filter filter = new Filter(tag, operation);
                try {
                    openApiToBallerina(fileName, filter);
                } catch (IOException e) {
                    outStream.println(e.getLocalizedMessage());
                    exitError(this.exitWhenFinish);
                }
            } else if (fileName.endsWith(".bal")) {
                try {
                    ballerinaToOpenApi(fileName);
                } catch (IOException e) {
                    outStream.println(e.getLocalizedMessage());
                    exitError(this.exitWhenFinish);
                }
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
    private void ballerinaToOpenApi(String fileName) throws IOException {
        final File balFile = new File(fileName);
        Path balFilePath = Paths.get(balFile.getCanonicalPath());
        getTargetOutputPath();
        // Check service name it is mandatory
        try {
            OpenApiConverter openApiConverter = new OpenApiConverter();
            openApiConverter.generateOAS3DefinitionsAllService(balFilePath, targetOutputPath, service,
                    generatedFileType);
        } catch (IOException  | ProjectException | OpenApiConverterException e) {
            outStream.println(e.getLocalizedMessage());
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * A util method for generating service and client stub using given contract file.
     * @param fileName input resource file
     */
    private void openApiToBallerina(String fileName, Filter filter) throws IOException {
        CodeGenerator generator = new CodeGenerator();
        generator.setLicenseHeader(this.setLicenseHeader());
        final File openApiFile = new File(fileName);
        String serviceName;
        if (generatedServiceName != null) {
            serviceName = generatedServiceName;
        } else {
            serviceName = openApiFile.getName().split("\\.")[0];
        }
        getTargetOutputPath();
        Path relativePath = getRelativePath(openApiFile, this.targetOutputPath.toString());
        Path resourcePath = Paths.get(openApiFile.getCanonicalPath());
        if (mode != null) {
            switch (mode) {
                case "service":
                    generateServiceFile(generator, serviceName, resourcePath, filter);
                    break;
                case "client":
                    generatesClientFile(generator, serviceName, resourcePath, filter);
                    break;
                default:
                    break;
            }
        } else {
            generateBothFiles(generator, serviceName, resourcePath, relativePath, filter);
        }
    }

    /**
     * A util to take the resource Path.
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
        } catch (IllegalArgumentException iaex) {
            return resourcePath.resolve(resourceFile.getName());
        }
    }

    /**
     * A util to get the output Path.
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
     * A util to set the license header content which is to be add at the beginning of the ballerina files.
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
     * @param clientName        file name uses to name the generated file
     * @param resourcePath      resource Path
     */
    private void generatesClientFile(CodeGenerator generator, String clientName, Path resourcePath, Filter filter) {
        try {
            generator.generateClient(resourcePath.toString(), clientName, targetOutputPath.toString(), filter,
                    nullable);
        } catch (IOException | BallerinaOpenApiException | FormatterException | URISyntaxException e) {
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
        } catch (IOException | BallerinaOpenApiException | FormatterException | URISyntaxException e) {
            outStream.println("Error occurred when generating service for OpenAPI contract at " + argList.get(0) +
                    ". " + e.getMessage() + ".");
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * A util method to generate both service and client stub files based on the given yaml contract file.
     * @param generator         generator object
     * @param fileName          service name  use for naming the files
     * @param resourcePath      resource path
     */
    private void generateBothFiles(CodeGenerator generator, String fileName, Path resourcePath, Path relativePath,
                                   Filter filter) {
        try {
            assert resourcePath != null;
            generator.generateBothFiles(GeneratorConstants.GenType.GEN_BOTH,
                    resourcePath.toString(), fileName, targetOutputPath.toString(), filter, nullable);
        } catch (IOException | BallerinaOpenApiException | FormatterException | URISyntaxException e) {
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
