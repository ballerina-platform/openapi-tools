/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.toml.syntax.tree.AbstractNodeFactory;
import io.ballerina.toml.syntax.tree.ArrayNode;
import io.ballerina.toml.syntax.tree.DocumentMemberDeclarationNode;
import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.IdentifierToken;
import io.ballerina.toml.syntax.tree.KeyNode;
import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.Minutiae;
import io.ballerina.toml.syntax.tree.MinutiaeList;
import io.ballerina.toml.syntax.tree.Node;
import io.ballerina.toml.syntax.tree.NodeFactory;
import io.ballerina.toml.syntax.tree.NodeList;
import io.ballerina.toml.syntax.tree.SeparatedNodeList;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.ValueNode;
import io.ballerina.toml.validator.SampleNodeGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import picocli.CommandLine;

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
import java.util.List;
import java.util.Locale;

import static io.ballerina.cli.cmd.CommandUtil.exitError;
import static io.ballerina.openapi.cmd.CmdConstants.DEFAULT_CLIENT_ID;
import static io.ballerina.openapi.cmd.CmdConstants.JSON_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.OPENAPI_ADD_CMD;
import static io.ballerina.openapi.cmd.CmdConstants.YAML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdConstants.YML_EXTENSION;
import static io.ballerina.openapi.cmd.CmdUtils.searchEnum;
import static io.ballerina.openapi.cmd.CmdUtils.getBallerinaTomlPath;
import static io.ballerina.openapi.core.generators.common.ErrorMessages.invalidFileType;
import static io.ballerina.toml.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.toml.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.toml.syntax.tree.AbstractNodeFactory.createToken;

/**
 * Main class to implement "add" subcommand for openapi to ballerina code generation in build command.
 *
 * @since 1.9.0
 */
@CommandLine.Command(
        name = "add",
        description = "Update Ballerina.toml with the OpenAPI CLI code generation implementation"
)
public class Add implements BLauncherCmd {
    private static final String COMMAND_IDENTIFIER = "openapi-add";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;
    private CmdConstants.Mode mode;


    @CommandLine.Mixin
    private final BaseCmd baseCmd = new BaseCmd();
    @CommandLine.ParentCommand
    private OpenApiCmd openApiMainCmd;

    @CommandLine.Option(names = {"--module"}, description = "Location of the generated Ballerina client")
    private String outputModule;

    @CommandLine.Option(names = {"--id"}, description = "ID for the generated Ballerina client")
    private String id;

    @CommandLine.Option(names = {"-p", "--package"}, description = "Location for the Ballerina package")
    private String packagePath;

    public Add() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    public Add(PrintStream outStream, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        Path projectPath = packagePath != null ? Path.of(packagePath) :
                Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        try {
            if (baseCmd.helpFlag) {
                String commandUsageInfo = BLauncherCmd
                        .getCommandUsageInfo(COMMAND_IDENTIFIER, Add.class.getClassLoader());
                outStream.println(commandUsageInfo);
                return;
            }
            //check the given path is the Ballerina package
            Path ballerinaTomlPath = getBallerinaTomlPath(projectPath);
            if (!isValidInputPath()) {
                return;
            }
            Path tomlPath = projectPath.resolve(ballerinaTomlPath);
            TextDocument configDocument = TextDocuments.from(Files.readString(tomlPath));
            SyntaxTree syntaxTree = SyntaxTree.from(configDocument);
            DocumentNode rootNode = syntaxTree.rootNode();
            NodeList<DocumentMemberDeclarationNode> nodeList = rootNode.members();
            NodeList<DocumentMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createEmptyNodeList();
            for (DocumentMemberDeclarationNode node: nodeList) {
                moduleMembers = moduleMembers.add(node);
            }
            //set default client id
            createDefaultClientID();
            CmdOptions options = collectCLIOptions();
            moduleMembers = CmdUtils.addNewLine(moduleMembers, 1);
            moduleMembers = populateOpenAPITomlConfig(options, moduleMembers);
            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            DocumentNode documentNode = NodeFactory.createDocumentNode(moduleMembers, eofToken);
            TextDocument textDocument = TextDocuments.from(documentNode.toSourceCode());
            String content = SyntaxTree.from(textDocument).toSourceCode();
            //write toml file
            try (FileWriter writer = new FileWriter(tomlPath.toString(), StandardCharsets.UTF_8)) {
                writer.write(content);
                outStream.print(ErrorMessages.TOML_UPDATED_MSG);
            }
        } catch (OpenAPICmdToolException | IOException e) {
            outStream.println(e.getMessage());
            exitError(exitWhenFinish);
        }
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private void createDefaultClientID() {
        if (id == null || id.isBlank()) {
            String file = getFileNameWithoutExtension(toPortablePath(baseCmd.inputPath));
            if (baseCmd.mode != null) {
                mode = searchEnum(CmdConstants.Mode.class, baseCmd.mode);
                if (mode == null) {
                    // Exit the code generation process
                    outStream.printf("ERROR:Invalid value for option '--mode': expected one of [service, client] but" +
                            " was '%s'%n", baseCmd.mode);
                    exitError(this.exitWhenFinish);
                }
            }
            id = String.format(DEFAULT_CLIENT_ID, mode == null ? "client" : baseCmd.mode, file);
        }
    }

    static String getFileNameWithoutExtension(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        int extensionStart = fileName.lastIndexOf('.');
        return extensionStart > 0 ? fileName.substring(0, extensionStart) : fileName;
    }

    static String toPortablePath(String path) {
        return toPortablePath(path, File.separatorChar);
    }

    /**
     * Converts Windows path separators to `/` so the path in Ballerina.toml works on every platform. On other
     * platforms, a backslash is a valid file name character and is kept as it is.
     *
     * @param path      path given by the user
     * @param separator file separator of the platform
     * @return path to be written to Ballerina.toml
     */
    static String toPortablePath(String path, char separator) {
        return separator == '\\' ? path.replace('\\', '/') : path;
    }

    /**
     * Escapes the characters which have a special meaning inside a TOML basic string.
     *
     * @param value string value
     * @return escaped value
     */
    static String escapeTomlString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private boolean isValidInputPath() {
        if (baseCmd.inputPath == null || baseCmd.inputPath.isBlank()) {
            outStream.printf(ErrorMessages.INVALID_INPUT_PATH);
            exitError(this.exitWhenFinish);
            return false;
        }
        // The build tool only handles these contract file types, so do not write an entry it cannot build.
        if (!(baseCmd.inputPath.endsWith(YAML_EXTENSION) || baseCmd.inputPath.endsWith(YML_EXTENSION) ||
                baseCmd.inputPath.endsWith(JSON_EXTENSION))) {
            outStream.println("ERROR: " + invalidFileType());
            exitError(this.exitWhenFinish);
            return false;
        }
        return true;
    }

    private CmdOptions collectCLIOptions() {
        return new CmdOptions.CmdOptionsBuilder()
                .withInput(toPortablePath(baseCmd.inputPath))
                .withId(id)
                .withOutputModule(outputModule)
                .withPackagePath(packagePath)
                .withClientMethod(baseCmd.generateClientMethods)
                .withMode(mode)
                .withNullable(baseCmd.nullable)
                .withOperations(getOperations())
                .withTags(getTags())
                .withLicensePath(baseCmd.licenseFilePath == null ? null : toPortablePath(baseCmd.licenseFilePath))
                .withStatusCodeBinding(baseCmd.statusCodeBinding)
                .withMock(baseCmd.mock)
                .withSingleFile(baseCmd.singleFile)
                .build();
    }

    /**
     * This util is to create the openapi tool config in toml.
     * <pre>
     *     [[tool.openapi]]
     *     id = "openapi_client"
     *     filePath = "input.yaml"
     *     targetModule = "delivery"
     *     options.mode = "client"
     *     option.operations = ["op1","op2","op3"]
     *     options.nullable = true
     *     options.clientMethod = "remote"
     * </pre>
     */
    private NodeList<DocumentMemberDeclarationNode> populateOpenAPITomlConfig(CmdOptions optionsBuilder,
                                                                              NodeList<DocumentMemberDeclarationNode>
                                                                                      moduleMembers) {
        TableNode openapiToolConfig = SampleNodeGenerator.createTable("[tool.openapi]", null);
        moduleMembers = moduleMembers.add(openapiToolConfig);
        moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("id",
                escapeTomlString(optionsBuilder.getId()), null));
        moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("filePath",
                escapeTomlString(optionsBuilder.getInput()), null));
        if (optionsBuilder.getOutputModule() != null) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("targetModule",
                    escapeTomlString(optionsBuilder.getOutputModule()), null));
        }
        if (optionsBuilder.getMode() != null) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("options.mode",
                    optionsBuilder.getMode().toString().toLowerCase(Locale.ENGLISH), null));
        }
        if (optionsBuilder.getTags() != null && !optionsBuilder.getTags().isEmpty()) {
            Node[] arrayItems = getArrayItemNodes(getTags());
            SeparatedNodeList<ValueNode> value = createSeparatedNodeList(arrayItems);
            ArrayNode arrayNode = NodeFactory.createArrayNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN), value,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            KeyValueNode tagNodes = NodeFactory.createKeyValueNode(getKeyNode("options.tags"), getAssignToken(),
                    arrayNode);
            moduleMembers = moduleMembers.add(tagNodes);
            if (optionsBuilder.getOperations() != null && !optionsBuilder.getOperations().isEmpty()) {
                moduleMembers = CmdUtils.addNewLine(moduleMembers, 1);
            }
        }
        if (optionsBuilder.getOperations() != null && !optionsBuilder.getOperations().isEmpty()) {
            Node[] arrayItems = getArrayItemNodes(getOperations());
            SeparatedNodeList<ValueNode> value = createSeparatedNodeList(arrayItems);
            ArrayNode arrayNode = NodeFactory.createArrayNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN), value,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            KeyValueNode opsNodes = NodeFactory.createKeyValueNode(getKeyNode("options.operations"),
                    getAssignToken(), arrayNode);
            moduleMembers = moduleMembers.add(opsNodes);
            moduleMembers = CmdUtils.addNewLine(moduleMembers, 1);
        }
        if (optionsBuilder.isNullable()) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createBooleanKV("options.nullable",
                    optionsBuilder.isNullable(), null));
        }
        if (optionsBuilder.getClientMethod() != null) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("options.clientMethods",
                    escapeTomlString(optionsBuilder.getClientMethod()), null));
        }
        if (optionsBuilder.getLicensePath() != null && !optionsBuilder.getLicensePath().isBlank()) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createStringKV("options.license",
                    escapeTomlString(optionsBuilder.getLicensePath()), null));
        }
        if (optionsBuilder.getStatusCodeBinding()) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createBooleanKV("options.statusCodeBinding",
                    optionsBuilder.getStatusCodeBinding(), null));
        }
        if (optionsBuilder.getMock()) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createBooleanKV("options.mock",
                    optionsBuilder.getMock(), null));
        }
        if (optionsBuilder.getSingleFile()) {
            moduleMembers = moduleMembers.add(SampleNodeGenerator.createBooleanKV("options.singleFile",
                    optionsBuilder.getSingleFile(), null));
        }
        moduleMembers = CmdUtils.addNewLine(moduleMembers, 2);
        return moduleMembers;
    }

    private Node[] getArrayItemNodes(List<String> items) {
        List<Node> itemList = new ArrayList<>();
        IdentifierToken doubleQuotes = createIdentifierToken("\"");
        for (String item : items) {
            itemList.add(NodeFactory.createStringLiteralNode(doubleQuotes,
                    createIdentifierToken(escapeTomlString(item)), doubleQuotes));
            itemList.add(createToken(SyntaxKind.COMMA_TOKEN));
        }
        // Remove the trailing comma if present
        if (!itemList.isEmpty()) {
            itemList.remove(itemList.size() - 1);
        }

        return itemList.toArray(new Node[0]);
    }

    @Override
    public String getName() {
        return OPENAPI_ADD_CMD;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        //This is the long description of the command and all handle within help command
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        //This is the usage description of the command and all handle within help command
    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {
        //This is not using in this command
    }

    public List<String> getTags() {
        return baseCmd.tags == null ? new ArrayList<>() : Arrays.asList(baseCmd.tags.split(","));
    }

    public List<String> getOperations() {
        return baseCmd.operations == null ? new ArrayList<>() : Arrays.asList(baseCmd.operations.split(","));
    }

    private static KeyNode getKeyNode(String key) {
        return NodeFactory.createKeyNode(NodeFactory.createSeparatedNodeList(
                new Node[]{NodeFactory.createIdentifierToken(key)}));
    }

    private static Token getAssignToken() {
        MinutiaeList whitespaceMinList = NodeFactory.createMinutiaeList(
                new Minutiae[]{NodeFactory.createWhitespaceMinutiae(" ")});
        return NodeFactory.createToken(SyntaxKind.EQUAL_TOKEN, whitespaceMinList, whitespaceMinList);
    }
}
