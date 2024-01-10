/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.cli.tool.BuildToolRunner;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.projects.Package;
import io.ballerina.projects.ToolContext;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.bal.tool.Constants.CLIENT;
import static io.ballerina.openapi.bal.tool.Constants.CLIENT_METHODS;
import static io.ballerina.openapi.bal.tool.Constants.LICENSE;
import static io.ballerina.openapi.bal.tool.Constants.MODE;
import static io.ballerina.openapi.bal.tool.Constants.NULLABLE;
import static io.ballerina.openapi.bal.tool.Constants.OPERATIONS;
import static io.ballerina.openapi.bal.tool.Constants.TAGS;
import static io.ballerina.openapi.bal.tool.Constants.TRUE;
import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;
import static io.ballerina.openapi.core.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorUtils.normalizeOpenAPI;

/**
 * This class includes the implementation of the {@code BuildToolRunner} for the OpenAPI tool.
 *
 * @since 1.9.0
 */
public class OpenAPIBuildToolRunner implements BuildToolRunner {
    static List<Diagnostic> diagnostics = new ArrayList<>();
    static String md5HexOpenAPI;

    @Override
    public String getToolName() {
        return "openapi";
    }

    @Override
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    @Override
    public void executeTool(ToolContext toolContext) {

        ImmutablePair<OASClientConfig, OASServiceMetadata> codeGeneratorConfig;
        TomlNodeLocation location = toolContext.packageInstance().ballerinaToml().get().tomlAstNode().location();
        Path cachePath = toolContext.cachePath();

        try {
            // Validate the OAS file whether we can handle within OpenAPI tool
            if (!canHandle(toolContext)) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.WARNING_FOR_UNSUPPORTED_CONTRACT;
                reportDiagnostics(error, error.getDescription(), location);
                return;
            }
            String oasFilePath = toolContext.filePath();
            Path packagePath = toolContext.packageInstance().project().sourceRoot();
            OpenAPI openAPI = getOpenAPIContract(packagePath, Path.of(oasFilePath), location);
            if (openAPI == null) {
                return;
            }
            TomlTableNode tomlTableNode = toolContext.optionsTable();

            if (tomlTableNode == null) {
                // Default generate client
                Filter filter = new Filter();
                OASClientConfig clientConfig = new OASClientConfig.Builder()
                        .withFilters(filter).withOpenAPI(openAPI).build();
                OASServiceMetadata serviceMetaData = new OASServiceMetadata.Builder()
                        .withFilters(filter).withOpenAPI(openAPI).build();
                codeGeneratorConfig =  new ImmutablePair<>(clientConfig, serviceMetaData);
                if (validateCache(toolContext, cachePath, clientConfig)) {
                    return;
                }
                generateClient(toolContext, codeGeneratorConfig);
            } else {
                codeGeneratorConfig = extractOptionDetails(toolContext, openAPI);
                Map<String, TopLevelNode> options = tomlTableNode.entries();
                if (validateCache(toolContext, cachePath, codeGeneratorConfig.getLeft())) {
                    return;
                }
                if (options.containsKey(MODE)) {
                    TopLevelNode value = options.get(MODE);
                    String mode = value.toNativeObject().toString().trim();
                    handleCodeGenerationMode(toolContext, codeGeneratorConfig, location, mode);
                } else {
                    // Create client for the given OAS
                    generateClient(toolContext, codeGeneratorConfig);
                }
            }
        } catch (BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.PARSER_ERROR;
            reportDiagnostics(error, e.getMessage(), location);
        } catch (IOException | FormatterException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
            reportDiagnostics(error, e.getMessage(), location);
        }
    }

    private static boolean validateCache(ToolContext toolContext, Path cachePath, OASClientConfig clientConfig)
            throws IOException {
        String cacheDir = toolContext.toolId();
        Path toolCachePath = cachePath.getParent();
        assert toolCachePath != null;
        File directoryPath = new File(toolCachePath.toString());
        String[] cacheDirs = directoryPath.list();
        md5HexOpenAPI = getHashValue(clientConfig, toolContext.targetModule());
        assert cacheDirs != null;
        for (String path : cacheDirs) {
            if (path.equals(cacheDir)) {
                // read the cache file
                Path cacheFilePath = Paths.get(cachePath.toString(), "openapi-cache.txt");
                String cacheContent = Files.readString(Paths.get(cacheFilePath.toString()));
                if (cacheContent.equals(md5HexOpenAPI)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method uses to handle the code generation mode. ex: client, service
     */
    private void handleCodeGenerationMode(ToolContext toolContext,
                                          ImmutablePair<OASClientConfig, OASServiceMetadata> codeGeneratorConfig,
                                          TomlNodeLocation location, String mode)
            throws BallerinaOpenApiException, IOException, FormatterException {
        if (mode.equals(CLIENT)) {
            // Create client for the given OAS
            generateClient(toolContext, codeGeneratorConfig);
        } else {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.WARNING_FOR_OTHER_GENERATION;
            reportDiagnostics(error, String.format(error.getDescription(), mode),
                    location);
        }
    }

    /**
     * This method uses to check whether given specification can be handled via the openapi client generation tool.
     * This includes basic requirements like file extension check.
     */
    private static boolean canHandle(ToolContext toolContext) {
        String oasPath = toolContext.filePath();
        if (oasPath.isBlank()) {
            TomlNodeLocation location = toolContext.packageInstance().ballerinaToml().get().tomlAstNode().location();
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.EMPTY_CONTRACT_PATH;
            reportDiagnostics(error, error.getDescription(), location);
            return false;
        }
        return (oasPath.endsWith(YAML_EXTENSION) || oasPath.endsWith(JSON_EXTENSION) ||
                oasPath.endsWith(YML_EXTENSION));
    }

    /**
     * This method uses to read the openapi contract and return the {@code OpenAPI} object.
     */
    private OpenAPI getOpenAPIContract(Path ballerinaFilePath, Path openAPIPath, Location location) {
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
            return normalizeOpenAPI(Path.of(relativePath.toString()), true);
        } catch (IOException | BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.UNEXPECTED_EXCEPTIONS;
            reportDiagnostics(error, error.getDescription(), location);
        }
        return null;
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
        Map<String, TopLevelNode> options = toolContext.optionsTable().entries();
        for (Map.Entry<String, TopLevelNode> field : options.entrySet()) {
            TomlValueNode valueNode = ((TomlKeyValueNode) field.getValue()).value();
            String value = valueNode.toNativeValue().toString().trim();
            String fieldName = field.getKey();
            switch (fieldName) {
                case TAGS:
                    filter.setTags(getArrayItems(valueNode));
                    break;
                case OPERATIONS:
                    filter.setOperations(getArrayItems(valueNode));
                    break;
                case NULLABLE:
                    serviceMetaDataBuilder.withNullable(value.contains(TRUE));
                    clientMetaDataBuilder.withNullable(value.contains(TRUE));
                    break;
                case CLIENT_METHODS:
                    clientMetaDataBuilder.withResourceMode(value.contains("resource"));
                    break;
                case LICENSE:
                    String licenseContent = Objects.equals(value, "") ? null :
                            getLicenseContent(toolContext, Paths.get(value));
                    clientMetaDataBuilder.withLicense(licenseContent != null ? licenseContent :
                            DO_NOT_MODIFY_FILE_HEADER);
                    break;
                default:
                    break;
            }
        }
        clientMetaDataBuilder.withFilters(filter);
        serviceMetaDataBuilder.withFilters(filter);
        return new ImmutablePair<>(clientMetaDataBuilder.build(), serviceMetaDataBuilder.build());
    }

    private static List<String> getArrayItems(TomlValueNode valueNode) {
        List<String> arrayItems = new ArrayList<>();
        if (valueNode instanceof TomlArrayValueNode arrayValueNode) {
            arrayValueNode.elements().forEach(element -> {
                arrayItems.add(element.toNativeValue().toString());
            });
        }
        return arrayItems;
    }

    /**
     * This method uses to generate the client module for the given openapi contract.
     */
    private void generateClient(ToolContext toolContext, ImmutablePair<OASClientConfig,
            OASServiceMetadata> codeGeneratorConfig) throws BallerinaOpenApiException, IOException, FormatterException {
        OASClientConfig clientConfig = codeGeneratorConfig.getLeft();
        List<GenSrcFile> sources = generateClientFiles(clientConfig);
        Path outputPath = toolContext.outputPath();
        writeGeneratedSources(sources, outputPath);
        // Update the cache file
        Path cachePath = toolContext.cachePath();
        List<GenSrcFile> sourcesForCache = new ArrayList<>();
        GenSrcFile genSrcFile = new GenSrcFile(GenSrcFile.GenFileType.CACHE_SRC, null,
                "openapi-cache.txt", md5HexOpenAPI);
        sourcesForCache.add(genSrcFile);
        writeGeneratedSources(sourcesForCache, cachePath);
    }

    private static String getHashValue(OASClientConfig clientConfig, String targetPath) {
        String openAPIDefinitions = clientConfig.getOpenAPI().toString().trim().replaceAll("\\s+", "");
        StringBuilder summaryOfCodegen = new StringBuilder();
        summaryOfCodegen.append(openAPIDefinitions)
                .append(targetPath)
                .append(clientConfig.isResourceMode())
                .append(clientConfig.getLicense())
                .append(clientConfig.isNullable());
        List<String> tags = clientConfig.getFilters().getTags();
        tags.sort(String.CASE_INSENSITIVE_ORDER);
        for (String str : tags) {
            summaryOfCodegen.append(str);
        }
        List<String> operations = clientConfig.getFilters().getOperations();
        operations.sort(String.CASE_INSENSITIVE_ORDER);
        for (String str : operations) {
            summaryOfCodegen.append(str);
        }

        return DigestUtils.md5Hex(summaryOfCodegen.toString()).toUpperCase(Locale.ENGLISH);
    }

    /**
     * This method uses to generate ballerina files for openapi client stub.
     * This will return list of (client.bal, util.bal, types.bal) {@code GenSrcFile}.
     */
    private static List<GenSrcFile> generateClientFiles(OASClientConfig oasClientConfig) throws
            BallerinaOpenApiException, IOException, FormatterException {

        List<GenSrcFile> sourceFiles = new ArrayList<>();

        // Generate ballerina client files.
        String licenseContent = oasClientConfig.getLicense();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
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

        // Generate ballerina records to represent schemas.
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        typeDefinitionNodeList.addAll(ballerinaClientGenerator.getTypeDefinitionNodeList());
        typeDefinitionNodeList.addAll(ballerinaClientGenerator
                .getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(oasClientConfig.getOpenAPI(),
                oasClientConfig.isNullable(), typeDefinitionNodeList);
        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (oasClientConfig.getFilters().getTags().size() > 0) {
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

    /**
     * Util to read license content.
     */
    private static String getLicenseContent(ToolContext context, Path licensePath) {
        Package packageInstance = context.packageInstance();
        Location location = context.optionsTable().entries().get("license").location();
        Path ballerinaFilePath = Optional.ofNullable(packageInstance.project().sourceRoot()).orElse(null);
        try {
            Path relativePath = getLicensePath(licensePath, location, ballerinaFilePath);
            return (relativePath != null) ? createLicenseContent(relativePath, location) : null;
        } catch (IOException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_READING_LICENSE_FILE;
            reportDiagnostics(error, error.getDescription(), location);
            return null;
        }
    }

    /**
     * Util to get license path.
     */
    private static Path getLicensePath(Path licensePath, Location location, Path ballerinaFilePath)
            throws IOException {
        Path relativePath = null;
        if (licensePath.toString().isBlank()) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.LICENSE_PATH_BLANK;
            reportDiagnostics(error, error.getDescription(), location);
        } else {
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
    private static String createLicenseContent(Path relativePath, Location location) {
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
            reportDiagnostics(error, error.getDescription(), location);
            return null;
        }
        return licenseHeader;
    }

    /**
     * This method uses to report the diagnostics.
     */
    private static void reportDiagnostics(Constants.DiagnosticMessages error, String error1, Location location) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), error1,
                error.getSeverity());
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo,
                location);
        diagnostics.add(diagnostic);
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
