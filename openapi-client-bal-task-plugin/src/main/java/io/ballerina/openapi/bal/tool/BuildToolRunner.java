package io.ballerina.openapi.bal.tool;

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
import io.ballerina.projects.ToolContext;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.swagger.v3.oas.models.OpenAPI;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.openapi.bal.tool.Constants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.bal.tool.Constants.IS_RESOURCE;
import static io.ballerina.openapi.bal.tool.Constants.LICENSE;
import static io.ballerina.openapi.bal.tool.Constants.NULLABLE;
import static io.ballerina.openapi.bal.tool.Constants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.bal.tool.Constants.OPENAPI_REGEX_PATTERN;
import static io.ballerina.openapi.bal.tool.Constants.OPERATIONS;
import static io.ballerina.openapi.bal.tool.Constants.TAGS;
import static io.ballerina.openapi.bal.tool.Constants.TEST_DIR;
import static io.ballerina.openapi.bal.tool.Constants.TEST_FILE_NAME;
import static io.ballerina.openapi.bal.tool.Constants.TRUE;
import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;
import static io.ballerina.openapi.core.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorUtils.normalizeOpenAPI;

public class BuildToolRunner implements io.ballerina.cli.tool.BuildToolRunner {
    List<Diagnostic> reportDiagnostics = new ArrayList<>();

    @Override
    public String getToolName() {
        return "openapi";
    }

    @Override
    public List<Diagnostic> diagnostics() {
        return new ArrayList<>();
    }


    @Override
    public void executeTool(ToolContext toolContext) {

        TomlTableNode tomlTableNode = toolContext.optionsTable();
        Map<String, TopLevelNode> options = tomlTableNode.entries();
        ImmutablePair<OASClientConfig, OASServiceMetadata> codeGeneratorConfig;
        try {
            // Validate the OAS file
            String oasFilePath = toolContext.filePath();

            codeGeneratorConfig = extractOptionDetails(toolContext);
            if (options.containsKey("mode")) {
                if (options.get("mode").toString().contains("client")) {
                    // create client for the given OAS
                    OASClientConfig clientConfig = codeGeneratorConfig.getLeft();

                    List<GenSrcFile> sources = generateClientFiles(clientConfig);
                    Path outputPath = toolContext.outputPath();
                    writeGeneratedSources(sources, outputPath);


                } else if (options.get("mode").toString().contains("service")) {
                    // create service
                } else if (options.get("mode").toString().contains("service-type")) {
                    // create service type
                } else {
                    // create both client and service
                }
            } else {
                // create both client and service
            }

        } catch (BallerinaOpenApiException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.PARSER_ERROR;
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), e.getMessage(),
                    error.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    toolContext.optionsTable().location());
            reportDiagnostics.add(diagnostic);
        } catch (IOException | FormatterException e) {
            Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), e.getMessage(),
                    error.getSeverity());
            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo,
                    toolContext.optionsTable().location());
            reportDiagnostics.add(diagnostic);
        }


        System.out.println(toolContext.toolId());

    }

    public static ImmutablePair<OASClientConfig, OASServiceMetadata> extractOptionDetails(ToolContext toolContext) throws IOException, BallerinaOpenApiException {
        String oasPath = toolContext.filePath();
        Path oasFilePath = toolContext.packageInstance().project().sourceRoot().resolve(oasPath);

        OpenAPI openAPI = normalizeOpenAPI(oasFilePath, true);
        Map<String, TopLevelNode> options = toolContext.optionsTable().entries();
        Filter filter = new Filter();
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASServiceMetadata.Builder serviceMetaDataBuilder = new OASServiceMetadata.Builder();
        clientMetaDataBuilder.withOpenAPI(openAPI);
        serviceMetaDataBuilder.withOpenAPI(openAPI);
        for (Map.Entry<String, TopLevelNode> field : options.entrySet()) {

            String[] values = field.getValue().toString().split(",");
            String fieldName = field.getKey();
            switch (fieldName) {
                case TAGS:
                    filter.setTags(List.of(values));
                    break;
                case OPERATIONS:
                    filter.setOperations(List.of(values));
                    break;
                case NULLABLE:
                    serviceMetaDataBuilder.withNullable(Arrays.asList(values).contains(TRUE));
                    clientMetaDataBuilder.withNullable(Arrays.asList(values).contains(TRUE));
                    break;
                case IS_RESOURCE:
//                    isResources = expression.toString().contains(TRUE);
                    clientMetaDataBuilder.withResourceMode(Arrays.asList(values).contains(TRUE));
                    break;
                case LICENSE:
//                    clientMetaDataBuilder.withLicense(
//                            expression.kind() == SyntaxKind.STRING_LITERAL && !expression.toString().isBlank() ?
//                                    getLicenseContent(context,
//                                            Paths.get(getStringValue((BasicLiteralNode) expression))) : "");
                    break;
                default:
                    break;
            }
            clientMetaDataBuilder.withFilters(filter);
            serviceMetaDataBuilder.withFilters(filter);
        }
        return new ImmutablePair<>(clientMetaDataBuilder.build(), serviceMetaDataBuilder.build());
    }

    /**
     * This method uses to generate ballerina files for openapi client stub.
     * This will return list of (client.bal, util.bal, types.bal) {@code GenSrcFile}.
     */
    private static List<GenSrcFile> generateClientFiles(OASClientConfig oasClientConfig) throws BallerinaOpenApiException, IOException, FormatterException {


        List<GenSrcFile> sourceFiles = new ArrayList<>();

        // generate ballerina client files.
        String licenseContent = DO_NOT_MODIFY_FILE_HEADER;
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

        // generate ballerina records to represent schemas.
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        typeDefinitionNodeList.addAll(ballerinaClientGenerator.getTypeDefinitionNodeList());
        typeDefinitionNodeList.addAll(ballerinaClientGenerator
                .getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(oasClientConfig.getOpenAPI(),
                oasClientConfig.isNullable(), typeDefinitionNodeList);
        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (oasClientConfig.getFilters().getTags().size() > 0) {
            // remove unused records and enums when generating the client by the tags given.
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


    private void writeGeneratedSources(List<GenSrcFile> sources, Path outputPath) throws IOException {

        for (GenSrcFile file : sources) {
            Path filePath;
            if (!file.getType().isOverwritable()) {
                filePath = outputPath.resolve(file.getFileName());
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
                    Files.createDirectories(Paths.get(outputPath + OAS_PATH_SEPARATOR + TEST_DIR));
                    filePath = Paths.get(outputPath.resolve(TEST_DIR + OAS_PATH_SEPARATOR +
                            file.getFileName()).toFile().getCanonicalPath());
                } else {
                    filePath = Paths.get(outputPath.resolve(file.getFileName()).toFile().getCanonicalPath());
                }
                String fileContent = file.getContent();
                writeFile(filePath, fileContent);
            }
        }
    }

    public static void writeFile(Path filePath, String content) throws IOException {
        File file = new File(filePath.toString());

        // Ensure the directory structure exists
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists()) {
            if (!parentDirectory.mkdirs()) {
                return; // Directory creation failed
            }
        }
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    /**
     * This method uses to check whether given specification can be handled via the openapi client generation tool.
     * This includes basic requirements like file extension check and file header check.
     */
    private static Optional<OpenAPI> getOpenAPI(ToolContext toolContext) {
        String oasPath = toolContext.filePath();
        try {
            if (!(oasPath.endsWith(YAML_EXTENSION) || oasPath.endsWith(JSON_EXTENSION) ||
                    oasPath.endsWith(YML_EXTENSION))) {
                return Optional.empty();
            }
            Path oasFilePath = toolContext.packageInstance().project().sourceRoot().resolve(oasPath);

            OpenAPI openAPI = normalizeOpenAPI(oasFilePath, true);
            return Optional.of(openAPI);

        } catch (IOException | NullPointerException | BallerinaOpenApiException e) {
            return Optional.empty();
        }
    }
}
