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
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.service.BallerinaServiceGenerator;
import io.ballerina.openapi.core.generators.service.BallerinaServiceObjectGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import static io.ballerina.openapi.cmd.CmdConstants.TEST_DIR;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.UNTITLED_SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdUtils.setGeneratedFileName;
import static io.ballerina.openapi.core.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

/**
 * This class generates Ballerina Services/Clients for a provided OAS definition.
 *
 * @since 1.3.0
 */
public class BallerinaCodeGenerator {
    private String srcPackage;
    private String licenseHeader = "";
    private boolean includeTestFiles;

    private static final PrintStream outStream = System.err;

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     */
    public void generateClientAndService(String definitionPath, String serviceName,
                                         String outPath, Filter filter, boolean nullable,
                                         boolean isResource, boolean generateServiceType)
            throws IOException, FormatterException,
            io.ballerina.openapi.core.exception.BallerinaOpenApiException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        Path openAPIPath = Path.of(definitionPath);
        // Normalize OpenAPI definition, in the client generation we suppose to terminate code generation when the
        // absence of the operationId in operation. Therefor we enable client flag true as default code generation.
        // if resource is enabled, we avoid checking operationId.
        OpenAPI openAPIDef = GeneratorUtils.normalizeOpenAPI(openAPIPath, !isResource);

        // Generate service
        String concatTitle = serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = concatTitle + "_service.bal";
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPIDef)
                .withFilters(filter)
                .withNullable(nullable)
                .withGenerateServiceType(generateServiceType)
                .build();
        BallerinaServiceGenerator serviceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        String serviceContent = Formatter.format
                (serviceGenerator.generateSyntaxTree()).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, srcFile,
                (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + serviceContent));

        if (generateServiceType) {
            BallerinaServiceObjectGenerator ballerinaServiceObjectGenerator = new
                    BallerinaServiceObjectGenerator(serviceGenerator.getFunctionList());
            String serviceType = Formatter.format(ballerinaServiceObjectGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                    "service_type.bal", (licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                    licenseHeader) + serviceType));
        }
        // Generate client.
        // Generate ballerina client remote.
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withNullable(nullable)
                .withPlugin(false)
                .withOpenAPI(openAPIDef)
                .withResourceMode(isResource).build();

        BallerinaClientGenerator clientGenerator = new BallerinaClientGenerator(oasClientConfig);
        String clientContent = Formatter.format(clientGenerator.generateSyntaxTree()).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME,
                (licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER : licenseHeader) + clientContent));
        String utilContent = Formatter.format(clientGenerator
                .getBallerinaUtilGenerator()
                .generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, srcPackage, UTIL_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + utilContent));
        }

        //Update type definition list
        List<TypeDefinitionNode> preGeneratedTypeDefNodes = new ArrayList<>(
                clientGenerator.getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        List<TypeDefinitionNode> typeInclusionRecords = serviceGenerator.getTypeInclusionRecords();
        List<TypeDefinitionNode> typeDefinitionNodeList = clientGenerator.getTypeDefinitionNodeList();
        preGeneratedTypeDefNodes.addAll(typeInclusionRecords);
        preGeneratedTypeDefNodes.addAll(typeDefinitionNodeList);

        // Generate ballerina types.
        // Generate ballerina records to represent schemas.
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(
                openAPIDef, nullable, preGeneratedTypeDefNodes);

        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (filter.getTags().size() > 0) {
            // Remove unused records and enums when generating the client by the tags given.
            schemaContent = GeneratorUtils.removeUnusedEntities(schemaSyntaxTree, clientContent, schemaContent,
                    serviceContent);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, srcPackage, TYPE_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + schemaContent));
        }

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

        writeGeneratedSources(newGenFiles, srcPath, implPath, GEN_BOTH);
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {
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
     * @param nullable       Enable nullable option for make record field optional
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateClient(String definitionPath, String outPath, Filter filter, boolean nullable,
                               boolean isResource)
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generateClientFiles(Paths.get(definitionPath), filter, nullable, isResource);
        writeGeneratedSources(genFiles, srcPath, implPath, GEN_CLIENT);
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
                                boolean nullable, boolean generateServiceType)
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generateBallerinaService(Paths.get(definitionPath), serviceName,
                filter, nullable, generateServiceType);
        writeGeneratedSources(genFiles, srcPath, implPath, GEN_SERVICE);
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
                    CodegenUtils.writeFile(filePath, fileContent);
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
                CodegenUtils.writeFile(filePath, fileContent);
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
    private List<GenSrcFile> generateClientFiles(Path openAPI, Filter filter, boolean nullable, boolean isResource)
            throws IOException, BallerinaOpenApiException, FormatterException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_CLIENT_PKG;
        }
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        // Normalize OpenAPI definition
        OpenAPI openAPIDef = GeneratorUtils.normalizeOpenAPI(openAPI, !isResource);
        // Generate ballerina service and resources.
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withNullable(nullable)
                .withPlugin(false)
                .withOpenAPI(openAPIDef)
                .withResourceMode(isResource)
                .build();
        //Take default DO NOT modify
        licenseHeader = licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER : licenseHeader;
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME,
                licenseHeader + mainContent));
        String utilContent = Formatter.format(
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, srcPackage, UTIL_FILE_NAME,
                    licenseHeader + utilContent));
        }

        List<TypeDefinitionNode> preGeneratedTypeDefNodes = new ArrayList<>(
                ballerinaClientGenerator.getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        preGeneratedTypeDefNodes.addAll(ballerinaClientGenerator.getTypeDefinitionNodeList());
        // Generate ballerina records to represent schemas.
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(
                openAPIDef, nullable, preGeneratedTypeDefNodes);

        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toSourceCode();
        if (filter.getTags().size() > 0) {
            // Remove unused records and enums when generating the client by the tags given.
            schemaContent = GeneratorUtils.removeUnusedEntities(schemaSyntaxTree, mainContent, schemaContent, null);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, srcPackage, TYPE_FILE_NAME,
                    licenseHeader + schemaContent));
        }

        // Generate test boilerplate code for test cases
        if (this.includeTestFiles) {
            BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(ballerinaClientGenerator);
            String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME,
                    licenseHeader + testContent));

            String configContent = ballerinaTestGenerator.getConfigTomlFile();
            if (!configContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                        CONFIG_FILE_NAME, configContent));
            }
        }

        return sourceFiles;
    }


    public List<GenSrcFile> generateBallerinaService(Path openAPI, String serviceName,
                                                      Filter filter, boolean nullable, boolean generateServiceType)
            throws IOException, FormatterException, BallerinaOpenApiException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_MOCK_PKG;
        }
        OpenAPI openAPIDef = GeneratorUtils.normalizeOpenAPI(openAPI, false);
        if (openAPIDef.getInfo() == null) {
            throw new BallerinaOpenApiException("Info section of the definition file cannot be empty/null: " +
                    openAPI);
        }

        if (openAPIDef.getInfo().getTitle().isBlank() && (serviceName == null || serviceName.isBlank())) {
            openAPIDef.getInfo().setTitle(UNTITLED_SERVICE);
        } else {
            openAPIDef.getInfo().setTitle(serviceName);
        }

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String concatTitle = serviceName == null ?
                openAPIDef.getInfo().getTitle().toLowerCase(Locale.ENGLISH) :
                serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = concatTitle + "_service.bal";
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPIDef)
                .withFilters(filter)
                .withNullable(nullable)
                .withGenerateServiceType(generateServiceType)
                .build();
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(oasServiceMetadata);
        String mainContent = Formatter.format(ballerinaServiceGenerator.generateSyntaxTree()).toSourceCode();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, srcFile,
                (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + mainContent));
        List<TypeDefinitionNode> preGeneratedTypeDefNodes = new ArrayList<>(
                ballerinaServiceGenerator.getTypeInclusionRecords());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(
                openAPIDef, nullable, preGeneratedTypeDefNodes);
        String schemaContent = Formatter.format(
                ballerinaSchemaGenerator.generateSyntaxTree()).toSourceCode();
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TYPE_FILE_NAME,
                    (licenseHeader.isBlank() ? DEFAULT_FILE_HEADER : licenseHeader) + schemaContent));
        }
        if (generateServiceType) {
            BallerinaServiceObjectGenerator ballerinaServiceObjectGenerator = new
                    BallerinaServiceObjectGenerator(ballerinaServiceGenerator.getFunctionList());
            String serviceType = Formatter.format(ballerinaServiceObjectGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                    "service_type.bal",
                    (licenseHeader.isBlank() ? DO_NOT_MODIFY_FILE_HEADER : licenseHeader) + serviceType));
        }
        return sourceFiles;
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
}
