package io.ballerina.generators.test;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.generators.BallerinaClientGenerator;
import io.ballerina.generators.GeneratorConstants;
import io.ballerina.generators.GeneratorUtils;
import io.ballerina.generators.auth.AuthTypeMap;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModuleVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNamedArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.generators.auth.AuthTypeMap.Flags;

/**
 * This class use for generating boilerplate codes for test cases.
 */
public class BallerinaTestGenerator {
    private static final List<String> remoteFunctionNameList = BallerinaClientGenerator.getRemoteFunctionNameList();
    private static final String serverURL = BallerinaClientGenerator.getServerUrl();

    /**
     * Generate test.bal file synatx tree.
     *
     * @return {@link SyntaxTree}
     * @throws IOException                  Throws exception when syntax tree not modified
     * @throws BallerinaOpenApiException    Throws exception if open api validation failed
     */
    public static SyntaxTree generateSyntaxTree()
            throws IOException, BallerinaOpenApiException {

        ImportDeclarationNode importForTest = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA,
                GeneratorConstants.MODULE_TEST);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForTest);

        List<FunctionDefinitionNode> functions = new ArrayList<>();
        getFunctionDefinitionNodes(functions);
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        nodes.addAll(getModuleVariableDeclarationNodes());
        nodes.addAll(functions);
        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, createNodeList(nodes), eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Returns String of the relevant config file to the generated test.bal.
     *
     * @return {@link String}
     * @throws IOException  Throws an exception if file not exists
     */
    public static String getConfigTomlFile () throws IOException {
        Path resDir = Paths.get("src/main/resources/config_toml_files").toAbsolutePath();
        String configFileName;
        if (AuthTypeMap.getFlag(Flags.BASIC)) {
            configFileName = "basic_config.toml";
        } else if (AuthTypeMap.getFlag(Flags.BEARER)) {
            configFileName = "bearer_config.toml";
        } else if (AuthTypeMap.getFlag(Flags.CLIENT_CREDENTIAL)) {
            configFileName = "client_credentials_config.toml";
        } else if (AuthTypeMap.getFlag(Flags.PASSWORD)) {
            configFileName = "password_config.toml";
        } else if (AuthTypeMap.getFlag(Flags.API_KEY)) {
            configFileName = "api_key_config.toml";
        } else {
            // todo: remove this
            configFileName = "empty_config.toml";
        }
        Path configFile = resDir.resolve(Paths.get(configFileName));
        return getStringFromGivenBalFile(configFile);
    }

    /**
     * Provide module variable declaration nodes of the test.bal file.
     * -- ex: Variable declaration nodes of test.bal for basic auth.
     * <pre>
     *      configurable http:CredentialsConfig & readonly authConfig = ?;
     *      ClientConfig clientConfig = {authConfig : authConfig};
     *      Client baseClient = check new Client(clientConfig, serviceUrl = "https://domain/services/data");
     * </pre>
     *
     * @return  {@link List<ModuleVariableDeclarationNode>} List of variable declaration nodes
     */
    private static List<ModuleVariableDeclarationNode> getModuleVariableDeclarationNodes () {
        List<ModuleVariableDeclarationNode> moduleVariableDeclarationNodes = new ArrayList<>();
        BuiltinSimpleNameReferenceNode typeBindingPattern;
        if (AuthTypeMap.getFlag(Flags.API_KEY)) {
            typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorConstants.API_KEY_CONFIG + " & readonly"));
            moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
            moduleVariableDeclarationNodes.add(getClientInitializationNode(GeneratorConstants.API_KEY_CONFIG_PARAM));
        } else if (AuthTypeMap.getFlag(Flags.HTTP_OR_OAUTH)) {
            typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorConstants.CONFIG_RECORD_NAME + " & readonly"));
            moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
            moduleVariableDeclarationNodes.add(getClientInitializationNode(GeneratorConstants.CONFIG_RECORD_ARG));
        }
        return moduleVariableDeclarationNodes;
    }

    /**
     * Generate the configurable variable to get auth record from Config.toml.
     * -- ex: Configurable variable for bearer auth mechanism.
     *        {@code configurable http:BearerTokenConfig & readonly authConfig = ?;}
     * -- ex: Configurable variable for API Key auth mechanism.
     *        {@code configurable ApiKeysConfig & readonly apiKeyConfig = ?;}
     *
     * @param   typeBindingPattern                      Variable name
     * @return  {@link ModuleVariableDeclarationNode}   Configurable variable declaration node
     */
    private static ModuleVariableDeclarationNode getConfigurableVariable (BuiltinSimpleNameReferenceNode
                                                                                  typeBindingPattern) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        Token configurableNode = createIdentifierToken("configurable");
        NodeList<Token> nodeList = createNodeList(configurableNode);
        CaptureBindingPatternNode bindingPattern;
        if (AuthTypeMap.getFlag(Flags.API_KEY)) {
            bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken(GeneratorConstants.API_KEY_CONFIG_PARAM));
        } else {
            bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken(GeneratorConstants.CONFIG_RECORD_ARG));
        }
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);
        ExpressionNode expressionNode = createRequiredExpressionNode(createToken(QUESTION_MARK_TOKEN));
        return createModuleVariableDeclarationNode(metadataNode, null, nodeList, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), expressionNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate client initialization node.
     * -- ex: Client initialization node of http or OAuth mechanisms.
     *        {@code Client baseClient = check new Client(clientConfig, serviceUrl = "https://domain/services/data");}
     * -- ex: CClient initialization node for API key auth mechanism.
     *        {@code Client baseClient = check new Client(apiKeyConfig, serviceUrl = "https://domain/services/data");}
     *
     * @param   configRecordName                        Config record name
     * @return  {@link ModuleVariableDeclarationNode}   Client initialization node
     */
    private static ModuleVariableDeclarationNode getClientInitializationNode (String configRecordName) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("baseClient"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);
        Token openParenArg = createToken(OPEN_PAREN_TOKEN);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        Token newKeyWord = createIdentifierToken("new");
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode1 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(configRecordName)));
        argumentsList.add(positionalArgumentNode1);
        Token commaToken =  createToken(COMMA_TOKEN);
        argumentsList.add(commaToken);
        ExpressionNode expressionNode = createRequiredExpressionNode(createIdentifierToken
                ("\"" + serverURL + "\""));
        NamedArgumentNode positionalArgumentNode2 = createNamedArgumentNode(createSimpleNameReferenceNode
                (createIdentifierToken(GeneratorConstants.SERVICE_URL)), createToken(EQUAL_TOKEN), expressionNode);
        argumentsList.add(positionalArgumentNode2);
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        TypeDescriptorNode clientClassType = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken
                (GeneratorConstants.CLIENT_CLASS));

        ExplicitNewExpressionNode explicitNewExpressionNode = createExplicitNewExpressionNode(newKeyWord,
                clientClassType, parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                explicitNewExpressionNode);
        NodeList<Token> nodeList = createEmptyNodeList();
        return createModuleVariableDeclarationNode(metadataNode, null, nodeList, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate test functions for each remote function in the generated client.bal.
     * <pre>
     *      @test:Config {}
     *      isolated function  testCurrentWeatherData() {
     *      }
     * </pre>
     *
     * @param functions Empty function definition node list
     */
    private static void getFunctionDefinitionNodes(List<FunctionDefinitionNode> functions) {
        if (!remoteFunctionNameList.isEmpty()) {
            for (String functionName : remoteFunctionNameList) {
                MetadataNode metadataNode = getTestAnnotation();
                Token functionKeyWord = createToken(FUNCTION_KEYWORD);
                IdentifierToken testFunctionName = createIdentifierToken(GeneratorConstants.PREFIX_TEST
                        + modifyFunctionName(functionName.trim()));
                FunctionSignatureNode functionSignatureNode = getFunctionSignature();
                FunctionBodyNode functionBodyNode = getFunctionBody();
                NodeList<Node> relativeResourcePath = createEmptyNodeList();
                Token isolatedQualifierNode = AbstractNodeFactory.createIdentifierToken("isolated");
                NodeList<Token> qualifierList = createNodeList(isolatedQualifierNode);
                FunctionDefinitionNode functionDefinitionNode = createFunctionDefinitionNode(FUNCTION_DEFINITION,
                        metadataNode, qualifierList, functionKeyWord, testFunctionName, relativeResourcePath,
                        functionSignatureNode, functionBodyNode);
                functions.add(functionDefinitionNode);
            }
        }
    }

    /**
     * Generate function signature node.
     * -- ex: {@code isolated function  testCurrentWeatherData()}
     *
     * @return {@link FunctionSignatureNode}
     */
    private static FunctionSignatureNode getFunctionSignature() {
        List<Node> parameterList = new ArrayList<>();
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                parameters, createToken(CLOSE_PAREN_TOKEN), null);
    }

    /**
     * Generate empty function body node.
     * -- ex: {@code isolated function  testCurrentWeatherData()}
     *
     * @return {@link FunctionBodyNode}
     */
    private static FunctionBodyNode getFunctionBody() {
        List<StatementNode> statementsList = new ArrayList<>();
        NodeList<StatementNode> statementsNodeList = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementsNodeList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate test annotation node.
     * --ex: {@code @test:Config {}}
     *
     * @return {@link MetadataNode}
     */
    private static MetadataNode getTestAnnotation() {
        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANNOT_TEST));
        List<Node> fileds = new ArrayList<>();
        SeparatedNodeList<MappingFieldNode> fieldNodesList = createSeparatedNodeList(fileds);
        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), fieldNodesList, createToken(CLOSE_BRACE_TOKEN));
        AnnotationNode annotationNode = createAnnotationNode(createToken(SyntaxKind.AT_TOKEN),
                annotateReference, annotValue);
        return createMetadataNode(null, createNodeList(annotationNode));
    }

    /**
     * Modify function name by capitalizing the first letter.
     *
     * @param   name    remote function name
     * @return  formatted name
     */
    private static String modifyFunctionName(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Get config.toml file content according to the authentication method in test.bal.
     *
     * @param   configFile      Config.toml file path
     * @return  {@link String}
     * @throws  IOException     Throws an error if file not exists in the given path
     */
    private static String getStringFromGivenBalFile(Path configFile) throws IOException {
        Stream<String> configFileLines = Files.lines(configFile);
        String configFileStr = configFileLines.collect(Collectors.joining("\n"));
        configFileLines.close();
        return configFileStr;
    }
}
