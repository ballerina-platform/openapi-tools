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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.service.BallerinaServiceGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.openapi.cmd.CmdConstants.BALLERINA_TOML;
import static io.ballerina.openapi.cmd.CmdConstants.BALLERINA_TOML_CONTENT;
import static io.ballerina.openapi.cmd.CmdConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.DEFAULT_CLIENT_PKG;
import static io.ballerina.openapi.cmd.CmdConstants.DEFAULT_MOCK_PKG;
import static io.ballerina.openapi.cmd.CmdConstants.DOUBLE_LINE_SEPARATOR;
import static io.ballerina.openapi.cmd.CmdConstants.GET;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_BOTH;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_CLIENT;
import static io.ballerina.openapi.cmd.CmdConstants.GenType.GEN_SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.HEAD;
import static io.ballerina.openapi.cmd.CmdConstants.IDENTIFIER;
import static io.ballerina.openapi.cmd.CmdConstants.LINE_SEPARATOR;
import static io.ballerina.openapi.cmd.CmdConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.cmd.CmdConstants.SERVICE_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_DIR;
import static io.ballerina.openapi.cmd.CmdConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.TYPE_NAME;
import static io.ballerina.openapi.cmd.CmdConstants.UNTITLED_SERVICE;
import static io.ballerina.openapi.cmd.CmdConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.cmd.CmdUtils.setGeneratedFileName;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaUtilGenerator.class);

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     */
    public void generateClientAndService(String definitionPath, String serviceName,
                                         String outPath, Filter filter, boolean nullable, boolean isResource)
            throws IOException, FormatterException,
            io.ballerina.openapi.core.exception.BallerinaOpenApiException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        Path openAPIPath = Path.of(definitionPath);
        // Normalize OpenAPI definition, in the client generation we suppose to terminate code generation when the
        // absence of the operationId in operation. Therefor we enable client flag true as default code generation.
        // if resource is enabled, we avoid checking operationId.
        OpenAPI openAPIDef = normalizeOpenAPI(openAPIPath, !isResource);

        // Generate service
        String concatTitle = serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = concatTitle + "_service.bal";
        BallerinaServiceGenerator serviceGenerator = new BallerinaServiceGenerator(openAPIDef, filter);
        String serviceContent = Formatter.format
                (serviceGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, srcFile, serviceContent));

        // Generate client.
        // Generate ballerina client remote.
        BallerinaClientGenerator clientGenerator = new BallerinaClientGenerator(openAPIDef, filter, nullable,
                isResource);
        String clientContent = Formatter.format(clientGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME, clientContent));
        String utilContent = Formatter.format(clientGenerator
                .getBallerinaUtilGenerator()
                .generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, UTIL_FILE_NAME, utilContent));
        }

        // Generate ballerina types.
        // Generate ballerina records to represent schemas.
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef, nullable);

        //Update type definition list
        List<TypeDefinitionNode> typeInclusionRecords = serviceGenerator.getTypeInclusionRecords();
        List<TypeDefinitionNode> typeDefinitionNodeList = clientGenerator.getTypeDefinitionNodeList();
        List<TypeDefinitionNode> externalTypeDefNodes = new ArrayList<>();
        externalTypeDefNodes.addAll(typeInclusionRecords);
        externalTypeDefNodes.addAll(typeDefinitionNodeList);

        ballerinaSchemaGenerator.setTypeDefinitionNodeList(externalTypeDefNodes);
        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();

        if (filter.getTags().size() > 0) {
            // Remove unused records and enums when generating the client by the tags given.
            schemaContent = modifySchemaContent(schemaSyntaxTree, clientContent, schemaContent, serviceContent);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, srcPackage, TYPE_FILE_NAME,
                    schemaContent));
        }

        // Generate test boilerplate code for test cases
        if (this.includeTestFiles) {
            BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(clientGenerator);
            String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toString();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME, testContent));

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
                                boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generateBallerinaService(Paths.get(definitionPath), serviceName, filter, nullable);
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
                    String fileContent = file.getFileName().endsWith(".bal") ?
                            (licenseHeader + file.getContent()) : file.getContent();
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
                String fileContent = file.getFileName().endsWith(".bal") ?
                        (licenseHeader + file.getContent()) : file.getContent();
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
        OpenAPI openAPIDef = normalizeOpenAPI(openAPI, !isResource);
        // Generate ballerina service and resources.
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPIDef, filter, nullable
                , isResource);
        String mainContent = Formatter.format(ballerinaClientGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, CLIENT_FILE_NAME, mainContent));
        String utilContent = Formatter.format(
                ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree()).toString();
        if (!utilContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, UTIL_FILE_NAME, utilContent));
        }

        // Generate ballerina records to represent schemas.
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef, nullable);
        ballerinaSchemaGenerator.setTypeDefinitionNodeList(ballerinaClientGenerator.getTypeDefinitionNodeList());
        SyntaxTree schemaSyntaxTree = ballerinaSchemaGenerator.generateSyntaxTree();
        String schemaContent = Formatter.format(schemaSyntaxTree).toString();
        if (filter.getTags().size() > 0) {
            // Remove unused records and enums when generating the client by the tags given.
            schemaContent = modifySchemaContent(schemaSyntaxTree, mainContent, schemaContent, null);
        }
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.MODEL_SRC, srcPackage, TYPE_FILE_NAME,
                    schemaContent));
        }

        // Generate test boilerplate code for test cases
        if (this.includeTestFiles) {
            BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(ballerinaClientGenerator);
            String testContent = Formatter.format(ballerinaTestGenerator.generateSyntaxTree()).toString();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TEST_FILE_NAME, testContent));

            String configContent = ballerinaTestGenerator.getConfigTomlFile();
            if (!configContent.isBlank()) {
                sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage,
                        CONFIG_FILE_NAME, configContent));
            }
        }

        return sourceFiles;
    }

    private String modifySchemaContent(SyntaxTree schemaSyntaxTree, String clientContent, String schemaContent,
                                       String serviceContent) throws IOException, FormatterException {
        Map<String, String> tempSourceFiles = new HashMap<>();
        tempSourceFiles.put(CLIENT_FILE_NAME, clientContent);
        tempSourceFiles.put(TYPE_FILE_NAME, schemaContent);
        if (serviceContent != null) {
            tempSourceFiles.put(SERVICE_FILE_NAME, schemaContent);
        }
        List<String> unusedTypeDefinitionNameList = getUnusedTypeDefinitionNameList(tempSourceFiles);
        while (unusedTypeDefinitionNameList.size() > 0) {
            ModulePartNode modulePartNode = schemaSyntaxTree.rootNode();
            NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
            List<ModuleMemberDeclarationNode> unusedTypeDefinitionNodeList = new ArrayList<>();
            for (ModuleMemberDeclarationNode node : members) {
                if (node.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                    for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                        if (childNodeEntry.name().equals(TYPE_NAME)) {
                            if (unusedTypeDefinitionNameList.contains(childNodeEntry.node().get().toString())) {
                                unusedTypeDefinitionNodeList.add(node);
                            }
                        }
                    }
                } else if (node.kind().equals(SyntaxKind.ENUM_DECLARATION)) {
                    for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                        if (childNodeEntry.name().equals(IDENTIFIER)) {
                            if (unusedTypeDefinitionNameList.contains(childNodeEntry.node().get().toString())) {
                                unusedTypeDefinitionNodeList.add(node);
                            }
                        }
                    }
                }
            }
            NodeList<ModuleMemberDeclarationNode> modifiedMembers = members.removeAll
                    (unusedTypeDefinitionNodeList);
            ModulePartNode modiedModulePartNode = modulePartNode.modify(modulePartNode.imports(),
                    modifiedMembers, modulePartNode.eofToken());
            schemaSyntaxTree = schemaSyntaxTree.modifyWith(modiedModulePartNode);
            schemaContent = Formatter.format(schemaSyntaxTree).toString();
            tempSourceFiles.put(TYPE_FILE_NAME, schemaContent);
            unusedTypeDefinitionNameList = getUnusedTypeDefinitionNameList(tempSourceFiles);
        }
        return schemaContent;
    }

    private List<String> getUnusedTypeDefinitionNameList(Map<String, String> srcFiles) throws IOException {
        List<String> unusedTypeDefinitionNameList = new ArrayList<>();
        Path tmpDir = Files.createTempDirectory(".openapi-tmp" + System.nanoTime());
        writeFilesTemp(srcFiles, tmpDir);
        if (Files.exists(tmpDir.resolve(CLIENT_FILE_NAME)) && Files.exists(tmpDir.resolve(TYPE_FILE_NAME)) &&
                Files.exists(tmpDir.resolve(BALLERINA_TOML))) {
            SemanticModel semanticModel = this.getSemanticModel(tmpDir.resolve(CLIENT_FILE_NAME));
            List<Symbol> symbols = semanticModel.moduleSymbols();
            for (Symbol symbol : symbols) {
                if (symbol.kind().equals(SymbolKind.TYPE_DEFINITION) || symbol.kind().equals(SymbolKind.ENUM)) {
                    List<Location> references = semanticModel.references(symbol);
                    if (references.size() == 1) {
                        unusedTypeDefinitionNameList.add(symbol.getName().get());
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tmpDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tmpDir, ex);
            }
        }));
        return unusedTypeDefinitionNameList;
    }

    private void writeFilesTemp(Map<String, String> srcFiles, Path tmpDir) throws IOException {
        srcFiles.put(BALLERINA_TOML, BALLERINA_TOML_CONTENT);
        PrintWriter writer = null;
        for (Map.Entry<String, String> entry : srcFiles.entrySet()) {
            String key = entry.getKey();
            Path filePath = tmpDir.resolve(key);
            try {
                writer = new PrintWriter(filePath.toString(), "UTF-8");
                writer.print(entry.getValue());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    private SemanticModel getSemanticModel(Path clientPath) throws ProjectException {
        // Load project instance for single ballerina file
        try {
            Project project = ProjectLoader.loadProject(clientPath);
            Package packageName = project.currentPackage();
            DocumentId docId;

            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                docId = project.documentId(clientPath);
            } else {
                // Take module instance for traversing the syntax tree
                Module currentModule = packageName.getDefaultModule();
                Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
                docId = documentIterator.next();
            }
            return project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
        } catch (ProjectException e) {
            throw new ProjectException(e.getMessage());
        }
    }


    public List<GenSrcFile> generateBallerinaService(Path openAPI, String serviceName,
                                                      Filter filter, boolean nullable)
            throws IOException, FormatterException, BallerinaOpenApiException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_MOCK_PKG;
        }
        OpenAPI openAPIDef = normalizeOpenAPI(openAPI, false);
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
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator(openAPIDef, filter);
        String mainContent = Formatter.format(ballerinaServiceGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef, nullable);
        ballerinaSchemaGenerator.setTypeDefinitionNodeList(ballerinaServiceGenerator.getTypeInclusionRecords());
        String schemaContent = Formatter.format(
                ballerinaSchemaGenerator.generateSyntaxTree()).toString();
        if (!schemaContent.isBlank()) {
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TYPE_FILE_NAME,
                    schemaContent));
        }
        return sourceFiles;
    }

    /**
     * Normalized OpenAPI specification with adding proper naming to schema.
     *
     * @param openAPIPath - openAPI file path
     * @return - openAPI specification
     * @throws IOException
     * @throws BallerinaOpenApiException
     */
    public OpenAPI normalizeOpenAPI(Path openAPIPath, boolean isClient) throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = CmdUtils.getOpenAPIFromOpenAPIV3Parser(openAPIPath);
        io.swagger.v3.oas.models.Paths openAPIPaths = openAPI.getPaths();
        if (isClient) {
            validateOperationIds(openAPIPaths.entrySet());
        }
        validateRequestBody(openAPIPaths.entrySet());

        if (openAPI.getComponents() != null) {
            // Refactor schema name with valid name
            Components components = openAPI.getComponents();
            Map<String, Schema> componentsSchemas = components.getSchemas();
            if (componentsSchemas != null) {
                Map<String, Schema> refacSchema = new HashMap<>();
                for (Map.Entry<String, Schema> schemaEntry : componentsSchemas.entrySet()) {
                    String name = getValidName(schemaEntry.getKey(), true);
                    refacSchema.put(name, schemaEntry.getValue());
                }
                openAPI.getComponents().setSchemas(refacSchema);
            }
        }
        return openAPI;
    }

    /**
     * Check whether an operationId has been defined in each path. If given rename the operationId to accepted format.
     * -- ex: GetPetName -> getPetName
     *
     * @param paths List of paths given in the OpenAPI definition
     * @throws BallerinaOpenApiException When operationId is missing in any path
     */
    private void validateOperationIds(Set<Map.Entry<String, PathItem>> paths) throws BallerinaOpenApiException {
        List<String> errorList = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operation :
                    entry.getValue().readOperationsMap().entrySet()) {
                if (operation.getValue().getOperationId() != null) {
                    String operationId = getValidName(operation.getValue().getOperationId(), false);
                    operation.getValue().setOperationId(operationId);
                } else {
                    errorList.add(String.format("OperationId is missing in the resource path: %s(%s)", entry.getKey(),
                            operation.getKey()));
                }
            }
        }
        if (!errorList.isEmpty()) {
            throw new BallerinaOpenApiException(
                    "OpenAPI definition has errors: " +
                            DOUBLE_LINE_SEPARATOR + String.join(LINE_SEPARATOR, errorList));
        }
    }

    /**
     * Validate if requestBody found in GET/DELETE/HEAD operation.
     *
     * @param paths - List of paths given in the OpenAPI definition
     * @throws BallerinaOpenApiException - If requestBody found in GET/DELETE/HEAD operation
     */
    private void validateRequestBody(Set<Map.Entry<String, PathItem>> paths) throws BallerinaOpenApiException {
        List<String> errorList = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths) {
            if (!entry.getValue().readOperationsMap().isEmpty()) {
                for (Map.Entry<PathItem.HttpMethod, Operation> operation : entry.getValue().readOperationsMap()
                        .entrySet()) {
                    String method = operation.getKey().name().trim().toLowerCase(Locale.ENGLISH);
                    boolean isRequestBodyInvalid = method.equals(GET) || method.equals(HEAD);
                    if (isRequestBodyInvalid && operation.getValue().getRequestBody() != null) {
                        errorList.add(method.toUpperCase(Locale.ENGLISH) + " operation cannot have a requestBody. "
                                + "Error at operationId: " + operation.getValue().getOperationId());
                    }
                }
            }
        }

        if (!errorList.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: " + DOUBLE_LINE_SEPARATOR);
            for (String message : errorList) {
                errorMessage.append(message).append(LINE_SEPARATOR);
            }
            throw new BallerinaOpenApiException(errorMessage.toString());
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
}
