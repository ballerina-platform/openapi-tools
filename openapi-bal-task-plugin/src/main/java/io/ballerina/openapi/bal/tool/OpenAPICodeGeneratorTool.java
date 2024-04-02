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
package io.ballerina.openapi.bal.tool;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaClientGeneratorWithStatusCodeBinding;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.syntax.tree.AbstractNodeFactory;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.validator.SampleNodeGenerator;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static io.ballerina.openapi.bal.tool.Constants.CACHE_FILE;
import static io.ballerina.openapi.bal.tool.Constants.CLIENT;
import static io.ballerina.openapi.bal.tool.Constants.CLIENT_METHODS;
import static io.ballerina.openapi.bal.tool.Constants.LICENSE;
import static io.ballerina.openapi.bal.tool.Constants.MODE;
import static io.ballerina.openapi.bal.tool.Constants.NULLABLE;
import static io.ballerina.openapi.bal.tool.Constants.OPERATIONS;
import static io.ballerina.openapi.bal.tool.Constants.STATUS_CODE_BINDING;
import static io.ballerina.openapi.bal.tool.Constants.TAGS;
import static io.ballerina.openapi.bal.tool.Constants.TRUE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.RESOURCE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.normalizeOpenAPI;

/**
 * This class includes the implementation of the {@code BuildToolRunner} for the OpenAPI tool.
 *
 * @since 1.9.0
 */
@ToolConfig(name = "openapi")
public class OpenAPICodeGeneratorTool implements CodeGeneratorTool {
    static String hashOpenAPI;

    @Override
    public void execute(ToolContext toolContext) {
        ImmutablePair<OASClientConfig, OASServiceMetadata> codeGeneratorConfig;
        TomlNodeLocation location = toolContext.currentPackage().ballerinaToml().get().tomlAstNode().location();
        try {
            // Validate the OAS file whether we can handle within OpenAPI tool
            if (!canHandle(toolContext)) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.WARNING_FOR_UNSUPPORTED_CONTRACT;
                createDiagnostics(toolContext, error, location);
                return;
            }
            // Handle the code generation
            String oasFilePath = toolContext.filePath();
            Path packagePath = toolContext.currentPackage().project().sourceRoot();
            Optional<OpenAPI> openAPI = getOpenAPIContract(packagePath, Path.of(oasFilePath), location, toolContext);
            if (openAPI.isEmpty()) {
                return;
            }
            //Extract the details using the `tool config options` table in the `Ballerina.toml` file.
            //If the `tool options` table is not specified in the TOML file, the client will be generated by default.
            Map<String, ToolContext.Option> options = toolContext.options();
            if (options == null) {
                // Default generate client
                Filter filter = new Filter();
                OASClientConfig clientConfig = new OASClientConfig.Builder()
                        .withFilters(filter).withOpenAPI(openAPI.get()).build();
                OASServiceMetadata serviceMetaData = new OASServiceMetadata.Builder()
                        .withFilters(filter).withOpenAPI(openAPI.get()).build();
                codeGeneratorConfig =  new ImmutablePair<>(clientConfig, serviceMetaData);
                if (validateCache(toolContext, clientConfig)) {
                    return;
                }
                generateClient(toolContext, codeGeneratorConfig, location);
            } else {
                codeGeneratorConfig = extractOptionDetails(toolContext, openAPI.get());
                if (validateCache(toolContext, codeGeneratorConfig.getLeft())) {
                    return;
                }
                if (options.containsKey(MODE)) {
                    String value = (String) options.get(MODE).value().toString().trim();
                    handleCodeGenerationMode(toolContext, codeGeneratorConfig, location, value);
                } else {
                    // Create client for the given OAS
                    generateClient(toolContext, codeGeneratorConfig, location);
                }
            }
        } catch (BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.PARSER_ERROR;
            createDiagnostics(toolContext, error, location);
        } catch (ClientException | IOException | FormatterException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
            createDiagnostics(toolContext, error, location);
        }
    }

    /**
     * This method uses to validate the cache.
     */
    private static boolean validateCache(ToolContext toolContext, OASClientConfig clientConfig) throws IOException {
        Path cachePath = toolContext.cachePath();
        hashOpenAPI = getHashValue(clientConfig, toolContext.targetModule());
        if (!Files.isDirectory(cachePath)) {
            return false;
        }
        // read the cache file
        Path cacheFilePath = Paths.get(cachePath.toString(), CACHE_FILE);
        String cacheContent = Files.readString(Paths.get(cacheFilePath.toString()));
        return cacheContent.equals(hashOpenAPI);
    }

    /**
     * This method uses to handle the code generation mode. ex: client, service
     */
    private void handleCodeGenerationMode(ToolContext toolContext,
                                          ImmutablePair<OASClientConfig, OASServiceMetadata> codeGeneratorConfig,
                                          TomlNodeLocation location, String mode)
            throws BallerinaOpenApiException, IOException, FormatterException, ClientException {
        if (mode.equals(CLIENT)) {
            // Create client for the given OAS
            generateClient(toolContext, codeGeneratorConfig, location);
        } else {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.WARNING_FOR_OTHER_GENERATION;
            createDiagnostics(toolContext, error, location, mode);
        }
    }

    /**
     * This method uses to check whether given specification can be handled via the openapi client generation tool.
     * This includes basic requirements like file extension check.
     */
    private static boolean canHandle(ToolContext toolContext) {
        String oasPath = toolContext.filePath();
        if (oasPath.isBlank()) {
            TomlNodeLocation location = toolContext.currentPackage().ballerinaToml().get().tomlAstNode().location();
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.EMPTY_CONTRACT_PATH;
            createDiagnostics(toolContext, error, location);
            return false;
        }
        return (oasPath.endsWith(YAML_EXTENSION) || oasPath.endsWith(JSON_EXTENSION) ||
                oasPath.endsWith(YML_EXTENSION));
    }

    /**
     * This method uses to read the openapi contract and return the {@code OpenAPI} object.
     */
    private Optional<OpenAPI> getOpenAPIContract(Path ballerinaFilePath, Path openAPIPath, Location location,
                                       ToolContext toolContext) {
        Path relativePath;
        try {
            Path inputPath = Paths.get(openAPIPath.toString());
            if (inputPath.isAbsolute()) {
                relativePath = inputPath;
            } else {
                File file = new File(ballerinaFilePath.toString());
                File openapiContract = new File(file, openAPIPath.toString());
                relativePath = Paths.get(openapiContract.getCanonicalPath());
            }
            if (Files.exists(relativePath)) {
                return Optional.of(normalizeOpenAPI(relativePath, true));
            } else {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.INVALID_CONTRACT_PATH;
                createDiagnostics(toolContext, error, location);
            }

        } catch (IOException | BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.UNEXPECTED_EXCEPTIONS;
            createDiagnostics(toolContext, error, location);
        }
        return Optional.empty();
    }

    /**
     * This method uses to extract the options given by the user.
     */
    public static ImmutablePair<OASClientConfig, OASServiceMetadata> extractOptionDetails(ToolContext toolContext,
                                                                                          OpenAPI openAPI) throws
            IOException, BallerinaOpenApiException {

        Filter filter = new Filter();
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASServiceMetadata.Builder serviceMetaDataBuilder = new OASServiceMetadata.Builder();
        clientMetaDataBuilder.withOpenAPI(openAPI);
        serviceMetaDataBuilder.withOpenAPI(openAPI);
        Map<String, ToolContext.Option> options = toolContext.options();
        for (Map.Entry<String, ToolContext.Option> field : options.entrySet()) {
            String value = field.getValue().value().toString().trim();
            String fieldName = field.getKey();
            switch (fieldName) {
                case TAGS:
                    filter.setTags(getArrayItems(field.getValue()));
                    break;
                case OPERATIONS:
                    filter.setOperations(getArrayItems(field.getValue()));
                    break;
                case NULLABLE:
                    serviceMetaDataBuilder.withNullable(value.contains(TRUE));
                    clientMetaDataBuilder.withNullable(value.contains(TRUE));
                    break;
                case CLIENT_METHODS:
                    clientMetaDataBuilder.withResourceMode(value.contains(RESOURCE));
                    break;
                case LICENSE:
                    Optional<String> licenseContent;
                    if (value.isBlank()) {
                        Constants.DiagnosticMessages error = Constants.DiagnosticMessages.LICENSE_PATH_BLANK;
                        createDiagnostics(toolContext, error, field.getValue().location());
                        licenseContent = Optional.of(DO_NOT_MODIFY_FILE_HEADER);
                    } else {
                        licenseContent = getLicenseContent(toolContext, Paths.get(value));
                    }
                    clientMetaDataBuilder.withLicense(licenseContent.orElse(DO_NOT_MODIFY_FILE_HEADER));
                    break;
                case STATUS_CODE_BINDING:
                    clientMetaDataBuilder.withStatusCodeBinding(value.contains(TRUE));
                    break;
                default:
                    break;
            }
        }
        clientMetaDataBuilder.withFilters(filter);
        serviceMetaDataBuilder.withFilters(filter);
        return new ImmutablePair<>(clientMetaDataBuilder.build(), serviceMetaDataBuilder.build());
    }

    private static List<String> getArrayItems(Object valueNode) {
        List<String> arrayItems = new ArrayList<>();
        if (valueNode instanceof ArrayList) {
            return (List<String>) valueNode;
        }
        return arrayItems;
    }

    /**
     * This method uses to generate the client module for the given openapi contract.
     */
    private void generateClient(ToolContext toolContext, ImmutablePair<OASClientConfig,
            OASServiceMetadata> codeGeneratorConfig, Location location) throws BallerinaOpenApiException, IOException,
            FormatterException, ClientException {
        OASClientConfig clientConfig = codeGeneratorConfig.getLeft();
        List<GenSrcFile> sources = generateClientFiles(clientConfig,
                codeGeneratorConfig.getLeft().isStatusCodeBinding());
        Path outputPath = toolContext.outputPath();
        writeGeneratedSources(sources, outputPath);
        // Update the cache file
        Path cachePath = toolContext.cachePath();
        List<GenSrcFile> sourcesForCache = new ArrayList<>();
        GenSrcFile genSrcFile = new GenSrcFile(GenSrcFile.GenFileType.CACHE_SRC, null,
                CACHE_FILE, hashOpenAPI);
        sourcesForCache.add(genSrcFile);
        writeGeneratedSources(sourcesForCache, cachePath);
        if (toolContext.options().containsKey(STATUS_CODE_BINDING) &&
                toolContext.options().get(STATUS_CODE_BINDING).value().toString().contains(TRUE)) {
            updateBallerinaTomlWithClientNativeDependency(toolContext,
                    toolContext.currentPackage().project().sourceRoot().resolve("Ballerina.toml"), location);
        }
    }

    private void updateBallerinaTomlWithClientNativeDependency(ToolContext toolContext, Path ballerinaTomlPath,
                                                               Location location) {
        try {
            String version = getVersion();
            if (checkForExistingClientNativeDependency(version, toolContext, location)) {
                return;
            }

            TextDocument configDocument = TextDocuments.from(Files.readString(ballerinaTomlPath));
            io.ballerina.toml.syntax.tree.SyntaxTree syntaxTree = io.ballerina.toml.syntax.tree.SyntaxTree.from(configDocument);
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
            String content = io.ballerina.toml.syntax.tree.SyntaxTree.from(textDocument).toSourceCode();
            try (FileWriter writer = new FileWriter(ballerinaTomlPath.toString(), StandardCharsets.UTF_8)) {
                writer.write(content);
                createDiagnostics(toolContext, Constants.DiagnosticMessages.TOML_UPDATED_WITH_CLIENT_NATIVE_DEPENDENCY, location);
            }
        } catch (IOException e) {
            createDiagnostics(toolContext, Constants.DiagnosticMessages.ERROR_WHILE_UPDATING_TOML, location);
        }
    }

    private boolean checkForExistingClientNativeDependency(String version, ToolContext toolContext, Location location) {
        Project project = toolContext.currentPackage().project();
        Map<String, PackageManifest.Platform> platforms = project.currentPackage().manifest().platforms();
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
                createDiagnostics(toolContext, Constants.DiagnosticMessages.TOML_ALREADY_UPDATED_WITH_CLIENT_NATIVE_DEPENDENCY, location);
            } else {
                createDiagnostics(toolContext, Constants.DiagnosticMessages.CLIENT_NATIVE_DEPENDENCY_VERSION_MISMATCH, location);
            }
            return true;
        }
        return false;
    }

    private NodeList<DocumentMemberDeclarationNode> populateClientNativeDependency(
            NodeList<DocumentMemberDeclarationNode> tomlMembers, String version) {
        String description = "This dependency is added automatically by the OpenAPI tool. DO NOT REMOVE";
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createTableArray("platform.java17.dependency", description));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("groupId", "io.ballerina.openapi", null));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("artifactId", "client-native", null));
        tomlMembers = tomlMembers.add(SampleNodeGenerator.createStringKV("version", version, null));
        return tomlMembers;
    }

    private String getVersion() throws IOException {
        try (InputStream inputStream = OpenAPICodeGeneratorTool.class.getClassLoader().getResourceAsStream(
                "openapi-tool-version.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("version");
        } catch (IOException exception) {
            throw new IOException("error occurred while reading version from version.properties: " +
                    exception.getMessage());
        }
    }

    public static NodeList<DocumentMemberDeclarationNode> addNewLine(NodeList moduleMembers, int n) {
        for (int i = 0; i < n; i++) {
            moduleMembers = moduleMembers.add(AbstractNodeFactory.createIdentifierToken(System.lineSeparator()));
        }
        return moduleMembers;
    }

    /**
     * This method uses to generate hash value for the given code generation details.
     * //TODO: This will be extended to support service generation.
     */
    private static String getHashValue(OASClientConfig clientConfig, String targetPath) {
        String openAPIDefinitions = clientConfig.getOpenAPI().toString().trim().replaceAll("\\s+", "");
        StringBuilder summaryOfCodegen = new StringBuilder();
        summaryOfCodegen.append(openAPIDefinitions)
                .append(targetPath)
                .append(clientConfig.isResourceMode())
                .append(clientConfig.getLicense())
                .append(clientConfig.isNullable());
        List<String> tags = clientConfig.getFilter().getTags();
        tags.sort(String.CASE_INSENSITIVE_ORDER);
        for (String str : tags) {
            summaryOfCodegen.append(str);
        }
        List<String> operations = clientConfig.getFilter().getOperations();
        operations.sort(String.CASE_INSENSITIVE_ORDER);
        for (String str : operations) {
            summaryOfCodegen.append(str);
        }
        return DigestUtils.sha256Hex(summaryOfCodegen.toString()).toUpperCase(Locale.ENGLISH);
    }

    /**
     * This method uses to generate ballerina files for openapi client stub.
     * This will return list of (client.bal, util.bal, types.bal) {@code GenSrcFile}.
     */
    private static List<GenSrcFile> generateClientFiles(OASClientConfig oasClientConfig,
                                                        boolean statusCodeBinding) throws
            BallerinaOpenApiException, IOException, FormatterException, ClientException {

        List<GenSrcFile> sourceFiles = new ArrayList<>();

        // Generate ballerina client files.
        TypeHandler.createInstance(oasClientConfig.getOpenAPI(), oasClientConfig.isNullable());
        String licenseContent = oasClientConfig.getLicense();
        BallerinaClientGenerator ballerinaClientGenerator = getClientGenerator(oasClientConfig, statusCodeBinding);
        String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, null, CLIENT_FILE_NAME,
                licenseContent == null || licenseContent.isBlank() ? mainContent :
                        licenseContent + System.lineSeparator() + mainContent));
        String utilContent = Formatter.format(
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();

        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.UTIL_SRC, null, UTIL_FILE_NAME,
                    licenseContent == null || licenseContent.isBlank() ? utilContent :
                            licenseContent + System.lineSeparator() + utilContent));
        }

        SyntaxTree schemaSyntaxTree = TypeHandler.getInstance().generateTypeSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (oasClientConfig.getFilter().getTags().isEmpty()) {
            // Remove unused records and enums when generating the client by the tags given.
            schemaContent = GeneratorUtils.removeUnusedEntities(schemaSyntaxTree, mainContent, schemaContent,
                    null);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, null, TYPE_FILE_NAME,
                    licenseContent == null || licenseContent.isBlank() ? schemaContent :
                            licenseContent + System.lineSeparator() + schemaContent));
        }

        return sourceFiles;
    }

    private static BallerinaClientGenerator getClientGenerator(OASClientConfig oasClientConfig,
                                                               boolean statusCodeBinding) {
        return statusCodeBinding ? new BallerinaClientGeneratorWithStatusCodeBinding(oasClientConfig) :
                new BallerinaClientGenerator(oasClientConfig);
    }

    /**
     * Util to read license content.
     */
    private static Optional<String> getLicenseContent(ToolContext context, Path licensePath) {
        Package packageInstance = context.currentPackage();
        Location location = context.options().get("license").location();
        Path ballerinaFilePath = packageInstance.project().sourceRoot();
        try {
            Path relativePath = getLicensePath(licensePath, ballerinaFilePath);
            return (relativePath != null) ? createLicenseContent(relativePath,
                    location, context) : Optional.empty();
        } catch (IOException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_READING_LICENSE_FILE;
            createDiagnostics(context, error, location);
            return Optional.empty();
        }
    }

    /**
     * Util to get license path.
     */
    private static Path getLicensePath(Path licensePath, Path ballerinaFilePath) throws IOException {
        Path relativePath = null;
        if (!licensePath.toString().isBlank()) {
            Path finalLicensePath = Paths.get(licensePath.toString());
            if (finalLicensePath.isAbsolute()) {
                relativePath = finalLicensePath;
            } else {
                File openapiContract = new File(ballerinaFilePath.toString(), licensePath.toString());
                relativePath = Paths.get(openapiContract.getCanonicalPath());
            }
        }
        return relativePath;
    }

    /**
     * Util to create license content.
     */
    private static Optional<String> createLicenseContent(Path relativePath, Location location,
                                                         ToolContext toolContext) {
        String licenseHeader;
        try {
            String newLine = System.lineSeparator();
            Path filePath = Paths.get((new File(relativePath.toString()).getCanonicalPath()));
            licenseHeader = Files.readString(Paths.get(filePath.toString()));
            if (!licenseHeader.endsWith(newLine)) {
                licenseHeader = licenseHeader + newLine + newLine;
            } else if (!licenseHeader.endsWith(newLine + newLine)) {
                licenseHeader = licenseHeader + newLine;
            }
        } catch (IOException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages
                    .ERROR_WHILE_READING_LICENSE_FILE;
            createDiagnostics(toolContext, error, location);
            return Optional.empty();
        }
        return Optional.of(licenseHeader);
    }

    /**
     * This method uses to report the diagnostics.
     */
    private static void createDiagnostics(ToolContext toolContext, Constants.DiagnosticMessages error,
                                          Location location, String... args) {
        String message = String.format(error.getDescription(), (Object[]) args);
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), message,
                error.getSeverity());
        toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, location));
    }

    /**
     * This method uses to write the generated sources into the given output path.
     */
    private void writeGeneratedSources(List<GenSrcFile> sources, Path outputPath) throws IOException {
        for (GenSrcFile file : sources) {
            Path filePath = Paths.get(outputPath.resolve(file.getFileName()).toFile().getCanonicalPath());
            String fileContent = file.getContent();
            writeFile(filePath, fileContent);
        }
    }

    /**
     * This method uses to write the content into the given file path.
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        File file = new File(filePath.toString());
        // Ensure the directory structure exists
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
                return; // Directory creation failed
        }
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
