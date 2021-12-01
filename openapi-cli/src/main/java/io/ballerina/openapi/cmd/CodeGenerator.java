/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.openapi.cmd;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.model.GenSrcFile;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.generators.service.BallerinaServiceGenerator;
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
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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

import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA_TOML;
import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA_TOML_CONTENT;
import static io.ballerina.openapi.generators.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.DEFAULT_CLIENT_PKG;
import static io.ballerina.openapi.generators.GeneratorConstants.DEFAULT_MOCK_PKG;
import static io.ballerina.openapi.generators.GeneratorConstants.DELETE;
import static io.ballerina.openapi.generators.GeneratorConstants.DOUBLE_LINE_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.ESCAPE_PATTERN;
import static io.ballerina.openapi.generators.GeneratorConstants.GET;
import static io.ballerina.openapi.generators.GeneratorConstants.GenType.GEN_CLIENT;
import static io.ballerina.openapi.generators.GeneratorConstants.GenType.GEN_SERVICE;
import static io.ballerina.openapi.generators.GeneratorConstants.HEAD;
import static io.ballerina.openapi.generators.GeneratorConstants.IDENTIFIER;
import static io.ballerina.openapi.generators.GeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.TEMPLATES_DIR_PATH_KEY;
import static io.ballerina.openapi.generators.GeneratorConstants.TEMPLATES_SUFFIX;
import static io.ballerina.openapi.generators.GeneratorConstants.TEST_DIR;
import static io.ballerina.openapi.generators.GeneratorConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.TYPE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.UNTITLED_SERVICE;
import static io.ballerina.openapi.generators.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;
import static io.ballerina.openapi.generators.GeneratorUtils.setGeneratedFileName;

/**
 * This class generates Ballerina Services/Clients for a provided OAS definition.
 */
public class CodeGenerator {
    private String srcPackage;
    private String licenseHeader = "";
    private boolean includeTestFiles;

    private static final PrintStream outStream = System.err;
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaUtilGenerator.class);

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated source will be written to a ballerina module at {@code outPath}
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>client</li>
     *                       </ul>
     * @param definitionPath Input Open Api Definition file path
     * @param serviceName    Output Service Name
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    private void generate(GeneratorConstants.GenType type, String definitionPath, String serviceName, String outPath,
                          Filter filter, boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {

        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genFiles = generateBalSource(type, definitionPath, serviceName, filter, nullable);
        writeGeneratedSources(genFiles, srcPath, implPath, type);
    }

    public void generateBothFiles(GeneratorConstants.GenType type, String definitionPath, String serviceName,
                                  String outPath, Filter filter, boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {
        Path srcPath = Paths.get(outPath);
        Path implPath = CodegenUtils.getImplPath(srcPackage, srcPath);
        List<GenSrcFile> genSrcFiles = generateBalSource(GEN_SERVICE, definitionPath, serviceName, filter, nullable)
                .stream().filter(genSrcFile -> !genSrcFile.getFileName().equals(TYPE_FILE_NAME))
                        .collect(Collectors.toList());
        genSrcFiles.addAll(generateBalSource(GEN_CLIENT, definitionPath, serviceName, filter,
                nullable));
        List<GenSrcFile> newGenFiles = genSrcFiles.stream().filter(distinctByKey(
                GenSrcFile::getFileName)).collect(Collectors.toList());
        writeGeneratedSources(newGenFiles, srcPath, implPath, type);
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
     * @param serviceName    Name of the service
     * @param outPath        Destination file path to save generated source files. If not provided
     *                       {@code definitionPath} will be used as the default destination path
     * @param filter         For take the tags and operation option values
     * @param nullable       Enable nullable option for make record field optional
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when code generator fails
     */
    public void generateClient(String definitionPath, String serviceName, String outPath, Filter filter,
                               boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {
        generate(GEN_CLIENT, definitionPath, serviceName, outPath, filter, nullable);
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
        generate(GEN_SERVICE, definitionPath, serviceName, outPath, filter, nullable);
    }

    /**
     * Generates ballerina source for provided Open API Definition in {@code definitionPath}.
     * Generated code will be returned as a list of source files
     * <p>Method can be user for generating Ballerina mock services and clients</p>
     *
     * @param type           Output type. Following types are supported
     *                       <ul>
     *                       <li>mock</li>
     *                       <li>client</li>
     *                       </ul>
     * @param serviceName    Out put service name
     * @param definitionPath Input Open Api Definition file path
     * @return a list of generated source files wrapped as {@link GenSrcFile}
     * @throws IOException               when file operations fail
     * @throws BallerinaOpenApiException when open api context building fail
     */
    public List<GenSrcFile> generateBalSource(GeneratorConstants.GenType type,
                                              String definitionPath,
                                              String serviceName, Filter filter,
                                              boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {
        String openAPIFileContent = Files.readString(Paths.get(definitionPath));
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);
        if (parseResult.getMessages().size() > 0) {
            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: \n\n");
            for (String message : parseResult.getMessages()) {
                errorMessage.append(message + "\n");
            }
            throw new BallerinaOpenApiException(errorMessage.toString());
        }
        OpenAPI api = parseResult.getOpenAPI();

        if (api.getInfo() == null) {
            throw new BallerinaOpenApiException("Info section of the definition file cannot be empty/null: " +
                    definitionPath);
        }

        if (api.getInfo().getTitle().isBlank() && (serviceName == null || serviceName.isBlank())) {
            api.getInfo().setTitle(UNTITLED_SERVICE);
        } else {
            api.getInfo().setTitle(serviceName);
        }

        List<GenSrcFile> sourceFiles;

        switch (type) {
            case GEN_CLIENT:
                // modelPackage is not in use at the moment. All models will be written into same package
                // as other src files.
                // Therefore value set to modelPackage is ignored here
                if (serviceName != null) {
                    api.getInfo().setTitle(serviceName.replaceAll(ESCAPE_PATTERN, "\\\\$1"));
                }
                sourceFiles = generateClient(Paths.get(definitionPath), filter, nullable);
                break;
            case GEN_SERVICE:
                sourceFiles = generateBallerinaService(Paths.get(definitionPath), serviceName, filter, nullable);
                break;
            default:
                return null;
        }

        return sourceFiles;
    }

    /**
     * Write ballerina definition of a <code>object</code> to a file as described by <code>template.</code>
     *
     * @param object       Context object to be used by the template parser
     * @param templateDir  Directory with all the templates required for generating the source file
     * @param templateName Name of the parent template to be used
     * @param outPath      Destination path for writing the resulting source file
     * @throws IOException when file operations fail
     * @deprecated This method is now deprecated.
     * Use {@link #generateBalSource
     * (GeneratorConstants.GenType, String, String, String, Filter, boolean nullable) generate}
     * and implement a file write functionality your self, if you need to customize file writing steps.
     * Otherwise use {@link #generate
     * (GeneratorConstants.GenType, String, String, String, String, String, Filter, boolean nullable)
     * generate}
     * to directly write generated source to a ballerina module.
     */
    @Deprecated
    public void writeBallerina(Object object, String templateDir, String templateName, String outPath)
            throws IOException {
        PrintWriter writer = null;

        try {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(
                    MapValueResolver.INSTANCE,
                    JavaBeanValueResolver.INSTANCE,
                    FieldValueResolver.INSTANCE).build();
            writer = new PrintWriter(outPath, "UTF-8");
            writer.println(template.apply(context));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private Template compileTemplate(String defaultTemplateDir, String templateName) throws IOException {
        defaultTemplateDir = defaultTemplateDir.replaceAll("\\\\", "/");
        String templatesDirPath = System.getProperty(TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(TEMPLATES_SUFFIX);
        fileTemplateLoader.setSuffix(TEMPLATES_SUFFIX);

        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader).with(EscapingStrategy.NOOP);
        handlebars.setInfiniteLoops(true); //This will allow templates to call themselves with recursion.
        handlebars.registerHelpers(StringHelpers.class);
        handlebars.registerHelper("equals", (object, options) -> {
            CharSequence result;
            Object param0 = options.param(0);

            if (param0 == null) {
                throw new IllegalArgumentException("found 'null', expected 'string'");
            }
            if (object != null) {
                if (object.toString().equals(param0.toString())) {
                    result = options.fn(options.context);
                } else {
                    result = options.inverse();
                }
            } else {
                result = null;
            }

            return result;
        });

        return handlebars.compile(templateName);
    }

    private void writeGeneratedSources(List<GenSrcFile> sources, Path srcPath, Path implPath,
                                       GeneratorConstants.GenType type)
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
    private List<GenSrcFile> generateClient(Path openAPI, Filter filter, boolean nullable)
            throws IOException, BallerinaOpenApiException, FormatterException {
        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_CLIENT_PKG;
        }
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        // Normalize OpenAPI definition
        OpenAPI openAPIDef = normalizeOpenAPI(openAPI, true);
        // Generate ballerina service and resources.
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPIDef, filter, nullable);
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
            schemaContent = modifySchemaContent(schemaSyntaxTree, mainContent, schemaContent);
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
                        GeneratorConstants.CONFIG_FILE_NAME, configContent));
            }
        }

        return sourceFiles;
    }

    private String modifySchemaContent(SyntaxTree schemaSyntaxTree, String clientContent, String schemaContent)
            throws IOException, FormatterException {
        Map<String, String> tempSourceFiles = new HashMap<>();
        tempSourceFiles.put(CLIENT_FILE_NAME, clientContent);
        tempSourceFiles.put(TYPE_FILE_NAME, schemaContent);
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


    private List<GenSrcFile> generateBallerinaService(Path openAPI, String serviceName,
                                                      Filter filter, boolean nullable)
            throws IOException, FormatterException, BallerinaOpenApiException {

        if (srcPackage == null || srcPackage.isEmpty()) {
            srcPackage = DEFAULT_MOCK_PKG;
        }

        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String concatTitle = serviceName.toLowerCase(Locale.ENGLISH);
        String srcFile = concatTitle + "_service.bal";
        OpenAPI openAPIDef = normalizeOpenAPI(openAPI, false);
        BallerinaServiceGenerator ballerinaServiceGenerator = new BallerinaServiceGenerator();
        String mainContent = Formatter.format
                (ballerinaServiceGenerator.generateSyntaxTree(openAPIDef, filter)).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, srcFile, mainContent));

        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPIDef, nullable);
        String schemaContent = Formatter.format(
                ballerinaSchemaGenerator.generateSyntaxTree()).toString();
        sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, srcPackage, TYPE_FILE_NAME, schemaContent));

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
        OpenAPI openAPI = GeneratorUtils.getOpenAPIFromOpenAPIV3Parser(openAPIPath);
        if (isClient) {
            validateOperationIds(openAPI.getPaths().entrySet());
            validateRequestBody(openAPI.getPaths().entrySet());
        } else {
            GeneratorUtils.setOperationId(openAPI.getPaths());
        }

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
                    boolean isRequestBodyInvalid = method.equals(GET) || method.equals(DELETE) || method.equals(HEAD);
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
