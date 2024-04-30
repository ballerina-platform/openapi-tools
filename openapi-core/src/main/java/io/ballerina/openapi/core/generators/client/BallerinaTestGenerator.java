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
package io.ballerina.openapi.core.generators.client;

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
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BASIC;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BEARER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CLIENT_CRED;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CONNECTION_CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PASSWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REFRESH_TOKEN;

/**
 * This class use for generating boilerplate codes for test cases.
 *
 * @since 1.3.0
 */
public class BallerinaTestGenerator {
    private final BallerinaClientGenerator ballerinaClientGenerator;
    private final List<String> remoteFunctionNameList;
    private String configFileName;
    private boolean isHttpOrOAuth;

    public BallerinaTestGenerator(BallerinaClientGenerator ballerinaClientGenerator) {

        this.ballerinaClientGenerator = ballerinaClientGenerator;
        this.remoteFunctionNameList = ballerinaClientGenerator.getRemoteFunctionNameList();
        this.configFileName = "";
        this.isHttpOrOAuth = false;
    }

    /**
     * Generate test.bal file synatx tree.
     *
     * @return {@link SyntaxTree}
     * @throws IOException               Throws exception when syntax tree not modified
     * @throws BallerinaOpenApiException Throws exception if open api validation failed
     */
    public SyntaxTree generateSyntaxTree() throws IOException, BallerinaOpenApiException {

        List<FunctionDefinitionNode> functions = new ArrayList<>();
        getFunctionDefinitionNodes(functions);
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>(getModuleVariableDeclarationNodes());
        NodeList<ImportDeclarationNode> imports = getImportNodes();
        nodes.addAll(functions);
        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, createNodeList(nodes), eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generate import nodes for test.bal file.
     *
     * @return Import node list
     */
    private NodeList<ImportDeclarationNode> getImportNodes() {

        NodeList<ImportDeclarationNode> imports;
        ImportDeclarationNode importForTest = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA,
                GeneratorConstants.MODULE_TEST);
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA,
                GeneratorConstants.HTTP);
        if (isHttpOrOAuth) {
            imports = AbstractNodeFactory.createNodeList(importForTest, importForHttp);
        } else {
            imports = AbstractNodeFactory.createNodeList(importForTest);
        }
        return imports;
    }

    /**
     * Returns String of the relevant config file to the generated test.bal.
     *
     * @return {@link String}
     * @throws IOException Throws an exception if file not exists
     */
    public String getConfigTomlFile() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        if (!configFileName.isBlank()) {
            if (!isHttpOrOAuth) {
                StringBuilder configFileContent = new StringBuilder("[" + API_KEY_CONFIG_PARAM + "]\n");

                for (String apiKey : ballerinaClientGenerator.getApiKeyNameList()) {
                    configFileContent.append(GeneratorUtils.escapeIdentifier(apiKey)).
                            append(" = \"<Enter Value>\"\n");
                }
                return configFileContent.toString();
            }
            InputStream inputStream = classLoader.getResourceAsStream("config_toml_files/" + configFileName);
            if (inputStream != null) {
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }
        return "";
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
     * @return {@link List<ModuleVariableDeclarationNode>} List of variable declaration nodes
     */
    private List<ModuleVariableDeclarationNode> getModuleVariableDeclarationNodes() {
        isHttpOrOAuth = true;
        Set<String> authTypes = ballerinaClientGenerator.getAuthType();
        List<ModuleVariableDeclarationNode> moduleVariableDeclarationNodes = new ArrayList<>();
        BuiltinSimpleNameReferenceNode typeBindingPattern;
        if (!authTypes.isEmpty()) {
            String authType = authTypes.iterator().next();
            switch (authType) {
                case BASIC:
                    typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:CredentialsConfig & readonly"));
                    moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                    moduleVariableDeclarationNodes.add(getAuthConfigAssignmentNode());
                    moduleVariableDeclarationNodes.add(getClientInitializationNode(
                            GeneratorConstants.CONFIG));
                    configFileName = "basic_config.toml";
                    break;
                case BEARER:
                    typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:BearerTokenConfig & readonly"));
                    moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                    moduleVariableDeclarationNodes.add(getAuthConfigAssignmentNode());
                    moduleVariableDeclarationNodes.add(getClientInitializationNode(
                            GeneratorConstants.CONFIG));
                    configFileName = "bearer_config.toml";
                    break;
                case CLIENT_CRED:
                    typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:OAuth2ClientCredentialsGrantConfig & readonly"));
                    moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                    moduleVariableDeclarationNodes.add(getAuthConfigAssignmentNode());
                    configFileName = "client_credentials_config.toml";
                    break;
                case PASSWORD:
                    typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:OAuth2PasswordGrantConfig & readonly"));
                    moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                    moduleVariableDeclarationNodes.add(getAuthConfigAssignmentNode());
                    configFileName = "password_config.toml";
                    break;
                case REFRESH_TOKEN:
                    typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:OAuth2RefreshTokenGrantConfig & readonly"));
                    moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                    moduleVariableDeclarationNodes.add(getAuthConfigAssignmentNode());
                    configFileName = "refresh_token_config.toml";
                    break;
                case API_KEY:
                    isHttpOrOAuth = false;
                    boolean combinationOfApiKeyAndHTTPOAuth = ballerinaClientGenerator.getBallerinaAuthConfigGenerator()
                            .isApiKey() && ballerinaClientGenerator.getBallerinaAuthConfigGenerator().isHttpOROAuth();
                    if (combinationOfApiKeyAndHTTPOAuth) {
                        // todo : The test file in the combination
                    } else {
                        typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(GeneratorConstants.API_KEYS_CONFIG + " & readonly"));
                        moduleVariableDeclarationNodes.add(getConfigurableVariable(typeBindingPattern));
                        moduleVariableDeclarationNodes.add(getClientInitializationNode
                                (API_KEY_CONFIG_PARAM));
                    }
                    configFileName = "api_key_config.toml";
                    break;
                default:
                    break;
            }
        } else {
            isHttpOrOAuth = false;
            moduleVariableDeclarationNodes.add(getClientInitForNoAuth());
        }

        return moduleVariableDeclarationNodes;
    }

    /**
     * Generate the configurable variable to get auth record from Config.toml.
     * --ex     {@code ClientConfig clientConfig = {authConfig : authConfig};}
     *
     * @return {@link ModuleVariableDeclarationNode}   ClientConfig declaration node
     */
    private ModuleVariableDeclarationNode getAuthConfigAssignmentNode() {

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(CONNECTION_CONFIG));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(GeneratorConstants.CONFIG));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);
        NodeList<Token> nodeList = createEmptyNodeList();
        ExpressionNode expressionNode = createRequiredExpressionNode(createIdentifierToken(String.format("{%s : %s}",
                GeneratorConstants.AUTH, GeneratorConstants.AUTH_CONFIG)));
        return createModuleVariableDeclarationNode(metadataNode, null, nodeList, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), expressionNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate the configurable variable to get auth record from Config.toml.
     * -- ex: Configurable variable for bearer auth mechanism.
     * {@code configurable http:BearerTokenConfig & readonly authConfig = ?;}
     * -- ex: Configurable variable for API Key auth mechanism.
     * {@code configurable ApiKeysConfig & readonly apiKeyConfig = ?;}
     * -- ex: Configurable variable for combination of ApiKey and HTTP/OAuth authentication mechanism.
     * {@code configurable AuthConfig & readonly authConfig = ?;}
     *
     * @param typeBindingPattern Variable name
     * @return {@link ModuleVariableDeclarationNode}   Configurable variable declaration node
     */
    private ModuleVariableDeclarationNode getConfigurableVariable(
            BuiltinSimpleNameReferenceNode typeBindingPattern) {

        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        Token configurableNode = createIdentifierToken("configurable");
        NodeList<Token> nodeList = createNodeList(configurableNode);
        CaptureBindingPatternNode bindingPattern;
        boolean combinationOfApiKeyAndHTTPOAuth = ballerinaClientGenerator.getBallerinaAuthConfigGenerator()
                .isApiKey() && ballerinaClientGenerator.getBallerinaAuthConfigGenerator().isHttpOROAuth();
        if (isHttpOrOAuth || combinationOfApiKeyAndHTTPOAuth) {
            bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken(GeneratorConstants.AUTH_CONFIG));
        } else {
            bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken(API_KEY_CONFIG_PARAM));
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
     * {@code Client baseClient = check new Client(clientConfig, serviceUrl = "https://domain/services/data");}
     * -- ex: CClient initialization node for API key auth mechanism.
     * {@code Client baseClient = check new Client(apiKeyConfig, serviceUrl = "https://domain/services/data");}
     *
     * @param configRecordName Config record name
     * @return {@link ModuleVariableDeclarationNode}   Client initialization node
     */
    private ModuleVariableDeclarationNode getClientInitializationNode(String configRecordName) {

        String serverURL = ballerinaClientGenerator.getServerUrl();
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.CLIENT));
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
        Token commaToken = createToken(COMMA_TOKEN);
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
                (GeneratorConstants.CLIENT));

        ExplicitNewExpressionNode explicitNewExpressionNode = createExplicitNewExpressionNode(newKeyWord,
                clientClassType, parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                explicitNewExpressionNode);
        NodeList<Token> nodeList = createEmptyNodeList();
        return createModuleVariableDeclarationNode(metadataNode, null, nodeList, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate client initialization node when no auth mehanism found.
     *
     * @return {@link ModuleVariableDeclarationNode}
     */
    private ModuleVariableDeclarationNode getClientInitForNoAuth() {

        String serverURL = ballerinaClientGenerator.getServerUrl();
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.CLIENT));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("baseClient"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);
        Token openParenArg = createToken(OPEN_PAREN_TOKEN);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        Token newKeyWord = createIdentifierToken("new");
        List<Node> argumentsList = new ArrayList<>();
        ExpressionNode expressionNode = createRequiredExpressionNode(createIdentifierToken
                ("\"" + serverURL + "\""));
        NamedArgumentNode positionalArgumentNode = createNamedArgumentNode(createSimpleNameReferenceNode
                (createIdentifierToken(GeneratorConstants.SERVICE_URL)), createToken(EQUAL_TOKEN), expressionNode);
        argumentsList.add(positionalArgumentNode);
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        TypeDescriptorNode clientClassType = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken
                (GeneratorConstants.CLIENT));

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
    private void getFunctionDefinitionNodes(List<FunctionDefinitionNode> functions) {

        if (!remoteFunctionNameList.isEmpty()) {
            for (String functionName : remoteFunctionNameList) {
                MetadataNode metadataNode = getAnnotation();
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
    private FunctionSignatureNode getFunctionSignature() {
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
    private FunctionBodyNode getFunctionBody() {
        List<StatementNode> statementsList = new ArrayList<>();
        NodeList<StatementNode> statementsNodeList = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementsNodeList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generate test annotation node.
     * --ex: {@code @test:Config {}}
     *
     * @return {@link MetadataNode}
     */
    private MetadataNode getAnnotation() {

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
     * @param name remote function name
     * @return formatted name
     */
    private String modifyFunctionName(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }
}
