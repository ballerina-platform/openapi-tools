/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaClientGeneratorWithStatusCodeBinding;
import io.ballerina.openapi.core.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.mock.AdvanceMockClientGenerator;
import io.ballerina.openapi.core.generators.client.mock.BallerinaMockClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.SingleFileGenerator;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.service.ServiceGenerationHandler;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.type.GeneratorConstants;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.openapi.cmd.CmdConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.DEFAULT_CLIENT_PKG;
import static io.ballerina.openapi.cmd.CmdConstants.DEFAULT_MOCK_PKG;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_BOTH;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_CLIENT;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.cmd.CmdConstants.SUPPORTED_OPENAPI_VERSIONS;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_DIR;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.UNTITLED_SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdUtils.setGeneratedFileName;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

/**
 * This class generates Ballerina Services/Clients for a provided OAS definition.
 *
 * @since 1.3.0
 */
public class BallerinaCodeGenerator {
    private String srcPackage;
    private String licenseHeader = "";
    private boolean includeTestFiles;
    private List<Diagnostic> diagnostics = new ArrayList<>();

    private static final PrintStream outStream = System.out;

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     */
    public void generateClientAndService(String definitionPath, String serviceName, String outPath, Filter filter,
                                         ClientServiceGeneratorOptions options)
            throws IOException, FormatterException, BallerinaOpenApiException,
            OASTypeGenException, ClientException {
        Path srcPath = Paths.get(outPath);
        Path implPath = getImplPath(srcPackage, srcPath);

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        Path openAPIPath = Path.of(definitionPath);
        OpenAPI openAPIDef = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(openAPIPath);
        checkOpenAPIVersion(openAPIDef);
        // Add typeHandler
        TypeHandler.createInstance(openAPIDef, options.nullable);
        // Generate service
        String serviceTitle = serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = String.format("%s_service.bal", serviceTitle);
        List<String> complexPaths = GeneratorUtils.getComplexPaths(openAPIDef);
        boolean isResource = options.isResource;
        if (!complexPaths.isEmpty()) {
            isResource = false;
            outStream.println("WARNING: remote function(s) will be generated for client and the service" +
                    " generation can not proceed due to the openapi definition contain following" +
                    " complex path(s):");
            for (String path: complexPaths) {
                outStream.println(path);
            }
        }
        // Normalize OpenAPI definition, in the client generation we suppose to terminate code generation when the
        // absence of the operationId in operation. Therefore, we enable client flag true as default code generation.
        // if resource is enabled, we avoid checking operationId.
        OpenAPI normalizedOpenAPI = GeneratorUtils.normalizeOpenAPI(openAPIDef, !isResource, options.isSanitizedOas);
        // Generate client.
        // Generate ballerina client remote.
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withNullable(options.nullable)
                .withPlugin(false)
                .withOpenAPI(normalizedOpenAPI)
                .withResourceMode(isResource)
                .withStatusCodeBinding(options.statusCodeBinding)
                .withMock(options.isMock).build();

        BallerinaClientGenerator clientGenerator = getBallerinaClientGenerator(oasClientConfig);
        String clientContent = Formatter.format(clientGenerator.generateSyntaxTree()).toSourceCode();

        //Update type definition list with auth related type definitions
        List<TypeDefinitionNode> authNodes = clientGenerator.getBallerinaAuthConfigGenerator()
                .getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef: authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME,
                (licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER : licenseHeader) + clientContent));
        String utilContent = Formatter.format(clientGenerator.getBallerinaUtilGenerator()
                .generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, srcPackage, UTIL_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + utilContent));
        }

        if (complexPaths.isEmpty()) {
            OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                    .withOpenAPI(normalizedOpenAPI)
                    .withFilters(filter)
                    .withNullable(options.nullable)
                    .withGenerateServiceType(options.generateServiceType)
                    .withGenerateServiceContract(options.generateServiceContract)
                    .withGenerateWithoutDataBinding(options.generateWithoutDataBinding)
                    .withSrcFile(srcFile)
                    .withSrcPackage(srcPackage)
                    .withLicenseHeader(licenseHeader)
                    .build();

            ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
            sourceFiles.addAll(serviceGenerationHandler.generateServiceFiles(oasServiceMetadata));
            this.diagnostics.addAll(serviceGenerationHandler.getDiagnostics());
        }

        TypeHandler typeHandler = TypeHandler.getInstance();
        SyntaxTree schemaSyntaxTree = typeHandler.generateTypeSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toSourceCode();
        this.diagnostics.addAll(TypeHandler.getInstance().getDiagnostics());

        generateSchemaFile(sourceFiles, schemaContent, licenseHeader);

        // Generate test boilerplate code for test cases
        if (this.includeTestFiles) {
            BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(clientGenerator);
            String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + testContent));

            String configContent = ballerinaTestGenerator.getConfigTomlFile();
            if (!configContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                        CONFIG_FILE_NAME, configContent));
            }
        }

        List<GenSrcFile> newGenFiles = sourceFiles.stream()
                .filter(distinctByKey(GenSrcFile::getFileName))
                .collect(Collectors.toList());
        //display diagnostics- client
        List<ClientDiagnostic> clientDiagnostic = clientGenerator.getDiagnostics();

        for (ClientDiagnostic diagnostic : clientDiagnostic) {
            outStream.println(diagnostic.getDiagnosticSeverity() + ":" + diagnostic.getMessage());
        }
        printDiagnostic(diagnostics);
        writeGeneratedSources(newGenFiles, srcPath, implPath, GEN_BOTH);
    }

    /**
     * Represents a record which stores the additional generator options for client.
     *
     *  @param nullable                     Enable nullable option to make record field optional
     *  @param isResource                   Enable isResource option to generate resource methods
     *  @param generateServiceType          Enable generateServiceType option to generate service type
     *  @param  generateServiceContract     Enable generateServiceContract option to generate service contract type
     *  @param generateWithoutDataBinding   Enable generateWithoutDataBinding option to generate the
     *                                      service without data binding
     *  @param statusCodeBinding            Enable statusCodeBinding option
     *  @param isMock                       Enable isMock option to generate mocks
     *  @param isSanitizedOas               Enable isSanitizedOas option to modify the OAS to follow the Ballerina
     *                                      language best practices
     */
    public record ClientServiceGeneratorOptions(boolean nullable, boolean isResource, boolean generateServiceType,
                                                boolean generateServiceContract, boolean generateWithoutDataBinding,
                                                boolean statusCodeBinding, boolean isMock, boolean isSanitizedOas) { }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * Method can be user for generating Ballerina clients.
     *
     * @param definitionPath Input Open Api Definition file path
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @param filter         For take the tags and operation option values
     * @param options        Contains the generation options
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateClient(String definitionPath, String outPath, Filter filter, ClientGeneratorOptions options)
            throws IOException, FormatterException, BallerinaOpenApiException, OASTypeGenException {
        Path srcPath = Paths.get(outPath);
        Path implPath = getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = null;
        try {
            genFiles = generateClientFiles(Paths.get(definitionPath), filter, options);
            if (!genFiles.isEmpty()) {
                writeGeneratedSources(genFiles, srcPath, implPath, GEN_CLIENT);
            }
        } catch (ClientException e) {
            outStream.println("error occurred while generating the client: " + e.getMessage());
        }
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * Method can be user for generating Ballerina clients.
     *
     * @param definitionPath Input Open Api Definition file path
     * @param serviceName    service name for the generated service
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateService(String definitionPath, String serviceName, String outPath, Filter filter,
                                ServiceGeneratorOptions options)
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path srcPath = Paths.get(outPath);
        Path implPath = getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generateBallerinaService(Paths.get(definitionPath), serviceName, filter, options);
        if (genFiles.isEmpty()) {
            return;
        }
        diagnostics.forEach(diagnostic -> {
            outStream.println(String.format("%s: %s", diagnostic.diagnosticInfo().severity(), diagnostic.message()));
        });
        writeGeneratedSources(genFiles, srcPath, implPath, GEN_SERVICE);
    }

    /**
     * Represents a record which stores the additional generator options for service.
     *
     * @param nullable                Enable nullable option for make record field optional
     * @param generateServiceType      Enable to generate service type
     * @param generateServiceContract  Enable to generate service contract
     * @param generateWithoutDataBinding  Enable to generate service without data binding
     * @param singleFile                   Enable singleFile option to generate all content in a single file
     * @param isSanitizedOas               Enable isSanitizedOas option to modify the OAS to follow the Ballerina
     *                                      language best practices
     */
    public record ServiceGeneratorOptions(boolean nullable, boolean generateServiceType,
                                          boolean generateServiceContract, boolean generateWithoutDataBinding,
                                          boolean singleFile, boolean isSanitizedOas) {
    }

    private void writeGeneratedSources(List<GenSrcFile> sources, Path srcPath, Path implPath,
                                       CmdConstants.GenType type)
            throws IOException {
        //  Remove old generated file with same name
        List<File> listFiles = new ArrayList<>();
        if (Files.exists(srcPath)) {
            File[] files = new File(String.valueOf(srcPath)).listFiles();
            if (files != null) {
                listFiles.addAll(Arrays.asList(files));
                for (File file : files) {
                    if (file.isDirectory() && file.getName().equals("tests")) {
                        File[] innerFiles = new File(srcPath + "/tests").listFiles();
                        if (innerFiles != null) {
                            listFiles.addAll(Arrays.asList(innerFiles));
                        }
                    }
                }
            }
        }

        for (File file : listFiles) {
            for (GenSrcFile gFile : sources) {
                if (file.getName().equals(gFile.getFileName())) {
                    if (System.console() != null) {
                        String userInput = System.console().readLine("There is already a/an " + file.getName() +
                                " in the location. Do you want to override the file? [y/N] ");
                        if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                            int duplicateCount = 0;
                            setGeneratedFileName(listFiles, gFile, duplicateCount);
                        }
                    }
                }
            }
        }

        for (GenSrcFile file : sources) {
            Path filePath;

            // We only overwrite files of overwritable type.
            // So non overwritable files will be written to disk only once.
            if (!file.getType().isOverwritable()) {
                filePath = implPath.resolve(file.getFileName());
                if (Files.notExists(filePath)) {
                    String fileContent = file.getContent();
                    writeFile(filePath, fileContent);
                }
            } else {
                boolean isDuplicatedFileInTests = file.getFileName().matches("test.+[0-9]+.bal") ||
                        file.getFileName().matches("Config.+[0-9]+.toml");
                if (file.getFileName().equals(TEST_FILE_NAME) || file.getFileName().equals(CONFIG_FILE_NAME) ||
                        isDuplicatedFileInTests) {
                    // Create test directory if not exists in the path. If exists do not throw an error
                    Files.createDirectories(Paths.get(srcPath + OAS_PATH_SEPARATOR + TEST_DIR));
                    filePath = Paths.get(srcPath.resolve(TEST_DIR + OAS_PATH_SEPARATOR +
                            file.getFileName()).toFile().getCanonicalPath());
                } else {
                    filePath = Paths.get(srcPath.resolve(file.getFileName()).toFile().getCanonicalPath());
                }
                String fileContent = file.getContent();
                writeFile(filePath, fileContent);
            }
        }

        //This will print the generated files to the console
        if (type.equals(GEN_SERVICE)) {
            outStream.println("Service generated successfully and the OpenAPI contract is copied to path " + srcPath
                    + ".");
        } else if (type.equals(GEN_CLIENT)) {
            outStream.println("Client generated successfully.");
        }
        outStream.println("Following files were created.");
        Iterator<GenSrcFile> iterator = sources.iterator();
        while (iterator.hasNext()) {
            outStream.println("-- " + iterator.next().getFileName());
        }
    }

    /**
     * Generate code for ballerina client.
     *
     * @return generated source files as a list of {@link GenSrcFile}
     * @throws IOException when code generation with specified templates fails
     */
    private List<GenSrcFile> generateClientFiles(Path openAPI, Filter filter, ClientGeneratorOptions options)
            throws IOException, BallerinaOpenApiException, FormatterException, ClientException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_CLIENT_PKG;
        }
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        OpenAPI openAPIDef = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(openAPI);
        checkOpenAPIVersion(openAPIDef);
        // Validate the service generation
        List<String> complexPaths = GeneratorUtils.getComplexPaths(openAPIDef);
        boolean isResource = options.isResource;
        if (!complexPaths.isEmpty()) {
            outStream.println("WARNING: remote function(s) will be generated for client as the given openapi " +
                    "definition contains following complex path(s):");
            for (String path: complexPaths) {
                outStream.println(path);
            }
            isResource = false;
        }
        // Validate and Normalize OpenAPI definition
        OpenAPI normalizedOpenAPI = GeneratorUtils.normalizeOpenAPI(openAPIDef, !isResource, options.isSanitizedOas);
        // Generate ballerina service and resources.
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withNullable(options.nullable)
                .withPlugin(false)
                .withOpenAPI(normalizedOpenAPI)
                .withResourceMode(isResource)
                .withStatusCodeBinding(options.statusCodeBinding)
                .withMock(options.isMock)
                .build();
        //Take default DO NOT modify
        licenseHeader = licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER : licenseHeader;
        TypeHandler.createInstance(openAPIDef, options.nullable);
        BallerinaClientGenerator clientGenerator = getBallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = clientGenerator.generateSyntaxTree();
        //Update type definition list with auth related type definitions
        List<TypeDefinitionNode> authNodes = clientGenerator.getBallerinaAuthConfigGenerator()
                .getAuthRelatedTypeDefinitionNodes();
        for (TypeDefinitionNode typeDef : authNodes) {
            TypeHandler.getInstance().addTypeDefinitionNode(typeDef.typeName().text(), typeDef);
        }

        if (options.singleFile) {
            syntaxTree = generateSingleFileForClient(syntaxTree, clientGenerator);
        } else {
            generateFilesForClient(syntaxTree, sourceFiles, clientGenerator);
        }

        //Type diagnostic
        List<Diagnostic> diagnosticList = TypeHandler.getInstance().getDiagnostics();
        // Generate test boilerplate code for test cases
        if (this.includeTestFiles) {
            BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(clientGenerator);
            String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME,
                    licenseHeader + testContent));

            String configContent = ballerinaTestGenerator.getConfigTomlFile();
            if (!configContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                        CONFIG_FILE_NAME, configContent));
            }
        }

        List<ClientDiagnostic> clientDiagnostic = clientGenerator.getDiagnostics();
        for (ClientDiagnostic diagnostic : clientDiagnostic) {
            outStream.println(diagnostic.getDiagnosticSeverity() + ":" + diagnostic.getMessage());
        }
        printDiagnostic(diagnosticList);
        if (options.singleFile) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                    CLIENT_FILE_NAME, licenseHeader + Formatter.format(syntaxTree).toSourceCode()));
        }
        return sourceFiles;
    }

    /**
     * Represents a record which stores the additional generator options for client.
     *
     *  @param nullable               Enable nullable option to make record field optional
     *  @param isResource             Enable isResource option to generate resource methods
     *  @param statusCodeBinding      Enable statusCodeBinding option
     *  @param isMock                 Enable isMock option to generate mocks
     *  @param singleFile             Enable singleFile option to generate all content in a single file
     *  @param isSanitizedOas               Enable isSanitizedOas option to modify the OAS to follow the Ballerina
     *                                      language best practices
     */
    public record ClientGeneratorOptions(boolean nullable, boolean isResource, boolean statusCodeBinding,
                                         boolean isMock, boolean singleFile, boolean isSanitizedOas) { }

    private void generateFilesForClient(SyntaxTree syntaxTree, List<GenSrcFile> sourceFiles,
                                        BallerinaClientGenerator clientGenerator) throws FormatterException,
            IOException {
        String mainContent = Formatter.format(syntaxTree).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME,
                licenseHeader + mainContent));
        String utilContent = Formatter.format(
                clientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, srcPackage, UTIL_FILE_NAME,
                    licenseHeader + utilContent));
        }
        // Generate ballerina records to represent schemas.
        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toSourceCode();
        generateSchemaFile(sourceFiles, schemaContent, licenseHeader);
    }

    private static SyntaxTree generateSingleFileForClient(SyntaxTree syntaxTree,
                                                          BallerinaClientGenerator clientGenerator) throws IOException {
        syntaxTree = SingleFileGenerator.combineSyntaxTrees(syntaxTree,
                clientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree(),
                TypeHandler.getInstance().generateTypeSyntaxTree());
        return syntaxTree;
    }

    private void generateSchemaFile(List<GenSrcFile> sourceFiles, String schemaContent, String licenseHeader) {
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, srcPackage, TYPE_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + schemaContent));
        }
    }

    private static BallerinaClientGenerator getBallerinaClientGenerator(OASClientConfig oasClientConfig) {
        boolean statusCodeBinding = oasClientConfig.isStatusCodeBinding();
        boolean isMock = oasClientConfig.isMock();

        if (statusCodeBinding && isMock) {
            return new AdvanceMockClientGenerator(oasClientConfig);
        }
        if (statusCodeBinding) {
            return new BallerinaClientGeneratorWithStatusCodeBinding(oasClientConfig);
        }
        if (isMock) {
            return new BallerinaMockClientGenerator(oasClientConfig);
        }
        return new BallerinaClientGenerator(oasClientConfig);
    }


    public List<GenSrcFile> generateBallerinaService(Path openAPI, String serviceName, Filter filter,
                                                     ServiceGeneratorOptions options)
            throws IOException, FormatterException, BallerinaOpenApiException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_MOCK_PKG;
        }
        OpenAPI openAPIDef = GeneratorUtils.normalizeOpenAPI(openAPI, false, options.isSanitizedOas);
        if (openAPIDef.getInfo() == null) {
            throw new BallerinaOpenApiException("Info section of the definition file cannot be empty/null: " +
                    openAPI);
        }

        checkOpenAPIVersion(openAPIDef);

        if (openAPIDef.getInfo().getTitle().isBlank() && (serviceName == null || serviceName.isBlank())) {
            openAPIDef.getInfo().setTitle(UNTITLED_SERVICE);
        } else {
            openAPIDef.getInfo().setTitle(serviceName);
        }
        // Validate the service generation
        List<String> complexPaths = GeneratorUtils.getComplexPaths(openAPIDef);
        if (!complexPaths.isEmpty()) {
            outStream.println("service generation can not be done as the openapi definition contain" +
                    " following complex path(s):");
            for (String path: complexPaths) {
                outStream.println(path);
            }
            return new ArrayList<>();
        }
        String concatTitle = serviceName == null ?
                openAPIDef.getInfo().getTitle().toLowerCase(Locale.ENGLISH) : serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = concatTitle + "_service.bal";

        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPIDef)
                .withFilters(filter)
                .withNullable(options.nullable)
                .withGenerateServiceType(options.generateServiceType)
                .withGenerateServiceContract(options.generateServiceContract)
                .withGenerateWithoutDataBinding(options.generateWithoutDataBinding)
                .withLicenseHeader(licenseHeader)
                .withSrcFile(srcFile)
                .withSrcPackage(srcPackage)
                .build();
        TypeHandler.createInstance(openAPIDef, options.nullable);
        ServiceGenerationHandler serviceGenerationHandler = new ServiceGenerationHandler();
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        if (options.singleFile) {
            generateSingleFileForService(serviceGenerationHandler, oasServiceMetadata, sourceFiles);
        } else {
            sourceFiles = generateFilesForService(serviceGenerationHandler, oasServiceMetadata);
        }

        this.diagnostics.addAll(serviceGenerationHandler.getDiagnostics());
        this.diagnostics.addAll(TypeHandler.getInstance().getDiagnostics());
        printDiagnostic(diagnostics);
        return sourceFiles;
    }

    private static List<GenSrcFile> generateFilesForService(ServiceGenerationHandler serviceGenerationHandler,
                                                            OASServiceMetadata oasServiceMetadata) throws
            FormatterException, BallerinaOpenApiException {
        List<GenSrcFile> sourceFiles;
        sourceFiles = serviceGenerationHandler.generateServiceFiles(oasServiceMetadata);
        if (!oasServiceMetadata.generateWithoutDataBinding()) {
            String schemaSyntaxTree = Formatter.format(TypeHandler.getInstance()
                    .generateTypeSyntaxTree()).toSourceCode();
            if (!schemaSyntaxTree.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, oasServiceMetadata.getSrcPackage(),
                        GeneratorConstants.TYPE_FILE_NAME,
                        (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                        oasServiceMetadata.getLicenseHeader()) + schemaSyntaxTree));
            }
        }
        return sourceFiles;
    }

    private static void generateSingleFileForService(ServiceGenerationHandler serviceGenerationHandler,
                                                     OASServiceMetadata oasServiceMetadata,
                                                     List<GenSrcFile> sourceFiles) throws
            BallerinaOpenApiException, FormatterException {
        SyntaxTree syntaxTree = serviceGenerationHandler.generateSingleSyntaxTree(oasServiceMetadata);
        if (!oasServiceMetadata.generateWithoutDataBinding()) {
            syntaxTree = SingleFileGenerator.combineSyntaxTrees(syntaxTree,
                    TypeHandler.getInstance().generateTypeSyntaxTree());
        }
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                oasServiceMetadata.getSrcFile(),
                (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                        oasServiceMetadata.getLicenseHeader()) + Formatter.format(syntaxTree).toSourceCode()));
    }

    private void printDiagnostic(List<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            outStream.printf("%s: %s%n", diagnostic.diagnosticInfo().severity(), diagnostic.message());
        }
    }

    /**
     * Set the content of license header.
     *
     * @param licenseHeader license header value received from command line.
     */
    public void setLicenseHeader(String licenseHeader) {
        this.licenseHeader = licenseHeader;
    }

    /**
     * set whether to add test files or not.
     *
     * @param includeTestFiles value received from command line by "--with tests"
     */
    public void setIncludeTestFiles(boolean includeTestFiles) {
        this.includeTestFiles = includeTestFiles;
    }

    private void checkOpenAPIVersion(OpenAPI openAPIDef) {
        if (!SUPPORTED_OPENAPI_VERSIONS.contains(openAPIDef.getOpenapi())) {
            outStream.printf("WARNING: The tool has not been tested with OpenAPI version %s. " +
                    "The generated code may potentially contain errors.%n", openAPIDef.getOpenapi());
        }
    }

    /**
     * Resolves path to write generated implementation source files.
     *
     * @param pkg     module
     * @param srcPath resolved path for main source files
     * @return path to write generated source files
     */
    private static Path getImplPath(String pkg, Path srcPath) {
        return (pkg == null || pkg.isEmpty()) ? srcPath : srcPath.getParent();
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    private static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
