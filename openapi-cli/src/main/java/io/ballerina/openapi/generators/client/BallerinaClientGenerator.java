/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.DocCommentsGenerator;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.schema.BallerinaSchemaGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBinaryExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createElseBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createForEachStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createListBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeTestExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELSE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FOREACH_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LOGICAL_OR_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.HTTP;

/**
 * This Util class uses for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    private final Filter filters;
    private List<ImportDeclarationNode> imports;
    private boolean isQuery;
    private boolean isHeader;
    private List<TypeDefinitionNode> typeDefinitionNodeList;
    private final OpenAPI openAPI;
    private final BallerinaSchemaGenerator ballerinaSchemaGenerator;
    private final GeneratorUtils generatorUtils;
    private final List<String> remoteFunctionNameList;
    private String serverURL;
    private final BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator;

    /**
     * Returns a list of type definition nodes.
     */
    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {

        return typeDefinitionNodeList;
    }
    /**
     * Set the typeDefinitionNodeList.
     */
    public void setTypeDefinitionNodeList(
            List<TypeDefinitionNode> typeDefinitionNodeList) {

        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }
    public List<String> getRemoteFunctionNameList () {
        return remoteFunctionNameList;
    }

    /**
     * Returns server URL.
     * @return {@link String}
     */
    public String getServerUrl () {
        return serverURL;
    }

    public BallerinaClientGenerator(OpenAPI openAPI, Filter filters, boolean nullable) {

        this.filters = filters;
        this.imports = new ArrayList<>();
        this.isQuery = false;
        this.isHeader = false;
        this.typeDefinitionNodeList = new ArrayList<>();
        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI, nullable);
        this.generatorUtils = new GeneratorUtils();
        this.remoteFunctionNameList = new ArrayList<>();
        this.serverURL = "/";
        this.ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(false, false);
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return                              return Syntax tree for the ballerina code.
     * @throws BallerinaOpenApiException    When function fail in process.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {

        // Create `ballerina/http` import declaration node
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        List<ModuleMemberDeclarationNode> nodes =  new ArrayList<>();
        if (openAPI.getComponents() != null && openAPI.getComponents().getSecuritySchemes() != null) {
            // Add authentication related records to module member nodes
            nodes.add(ballerinaAuthConfigGenerator.getConfigRecord(openAPI));
        }
        // Add class definition node to module member nodes
        nodes.add(getClassDefinitionNode());
        if (isQuery) {
            // Add `getPathForQueryParam` function to module member nodes
            nodes.add(generateFunctionForQueryParams());
        }
        if (isHeader) {
            // Add `getMapForHeaders` function to module member nodes
            nodes.add(generateFunctionForHeaderMap());
        }
        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(nodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generate Class definition Node with below code structure.
     * <pre>
     *     public isolated client class Client {
     *     final http:Client clientEp;
     *     public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "/url")
     *     returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *     }
     *     // Remote functions
     *     remote isolated function pathParameter(int 'version, string name) returns string|error {
     *         string  path = string `/v1/${'version}/v2/${name}`;
     *         string response = check self.clientEp-> get(path, targetType = string);
     *         return response;
     *     }
     * }
     * </pre>
     */
    private  ClassDefinitionNode getClassDefinitionNode() throws BallerinaOpenApiException {
        // Collect members for class definition node
        List<Node> memberNodeList =  new ArrayList<>();
        // Add instance variable to class definition node
        memberNodeList.addAll(createClassInstanceVariables());
        // Add init function to class definition node
        memberNodeList.add(createInitFunction());
        // Generate remote function Nodes
        memberNodeList.addAll(createRemoteFunctions(openAPI.getPaths(), filters));
        // Generate the class combining members
        MetadataNode metadataNode = getClassMetadataNode();
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD),  className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate metadata node of the class including documentation and display annotation. Content of the documentation
     * will be taken from the `description` section inside the `info` section in OpenAPI definition.
     *
     * @return  {@link MetadataNode}    Metadata node of the client class
     */
    private MetadataNode getClassMetadataNode() {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        if (openAPI.getInfo().getExtensions() != null) {
            Map<String, Object> extensions = openAPI.getInfo().getExtensions();
            if (!extensions.isEmpty()) {
                for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                    if (extension.getKey().trim().equals("x-display")) {
                        metadataNode = DocCommentsGenerator.getMetadataNodeForDisplayAnnotation(extension);
                    }
                }
            }
        }
        // Get client class documentation
        List<Node> documentationLines = new ArrayList<>();
        if (openAPI.getInfo().getDescription() != null) {
            documentationLines.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    openAPI.getInfo().getDescription(), false));
        }
        MarkdownDocumentationNode apiDoc = createMarkdownDocumentationNode(createNodeList(documentationLines));
        metadataNode = metadataNode.modify(apiDoc, metadataNode.annotations());
        return metadataNode;
    }

    /**
     * Create client class init function
     * -- Scenario 1: init function when authentication mechanism is API key
     * <pre>
     *   public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl,
     *      http:ClientConfiguration clientConfig =  {}) returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *         self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
     *   }
     * </pre>
     * -- Scenario 2: init function when authentication mechanism is OAuth2.0
     * <pre>
     *   public isolated function init(ClientConfig clientConfig, string serviceUrl = "base-url") returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *   }
     * </pre>
     * -- Scenario 3: init function when no authentication mechanism is provided
     * <pre>
     *   public isolated function init(http:ClientConfiguration clientConfig =  {},
     *      string serviceUrl = "base-url") returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *   }
     * </pre>
     *
     * @return {@link FunctionDefinitionNode}   Class init function
     * @throws BallerinaOpenApiException        When invalid server URL is provided
     */
    private FunctionDefinitionNode createInitFunction() throws BallerinaOpenApiException {
        FunctionSignatureNode functionSignatureNode = getInitFunctionSignatureNode();
        FunctionBodyNode functionBodyNode = getInitFunctionBodyNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        return createFunctionDefinitionNode(null, getInitDocComment(), qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Create function body node of client init function.
     *
     * @return  {@link FunctionBodyNode}
     */
    private FunctionBodyNode getInitFunctionBodyNode() {
        // create initialization statement of http:Client class instance
        VariableDeclarationNode clientInitializationNode = ballerinaAuthConfigGenerator.getClientInitializationNode();
        // create {@code self.clientEp = httpEp;} assignment node
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("clientEp")));
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken("httpEp"));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));
        List<StatementNode> assignmentNodes = new ArrayList<>();
        assignmentNodes.add(clientInitializationNode);
        assignmentNodes.add(httpClientAssignmentStatementNode);
        // Get API key assignment node if authentication mechanism type is `apiKey`
        if (ballerinaAuthConfigGenerator.isAPIKey()) {
            assignmentNodes.add(ballerinaAuthConfigGenerator.getApiKeyAssignmentNode());
        }
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Create function signature node of client init function.
     *
     * @return  {@link FunctionSignatureNode}
     * @throws  BallerinaOpenApiException   When invalid server URL is provided
     */
    private FunctionSignatureNode getInitFunctionSignatureNode() throws BallerinaOpenApiException {
        serverURL = getServerURL(openAPI.getServers());
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                ballerinaAuthConfigGenerator.getConfigParamForClassInit(serverURL));
        OptionalTypeDescriptorNode returnType = createOptionalTypeDescriptorNode(createToken(ERROR_KEYWORD),
                createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);
        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    /**
     * Provide client class init function's documentation including function description and parameter descriptions.
     * @return  {@link MetadataNode}    Metadata node containing entire function documentation comment.
     */
    private MetadataNode getInitDocComment() {
        List<Node> docs = new ArrayList<>();
        String clientInitDocComment = "Gets invoked to initialize the `connector`.\n";
        if (openAPI.getInfo().getExtensions() != null && !openAPI.getInfo().getExtensions().isEmpty()) {
            Map<String, Object> extensions = openAPI.getInfo().getExtensions();
            for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                if (extension.getKey().trim().equals("x-init-description")) {
                    clientInitDocComment = clientInitDocComment.concat(extension.getValue().toString());
                    break;
                }
            }
        }
        //todo: setInitDocComment() pass the references
        docs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(clientInitDocComment, true));
        if (ballerinaAuthConfigGenerator.isAPIKey()) {
            MarkdownParameterDocumentationLineNode apiKeyConfig = DocCommentsGenerator.createAPIParamDoc(
                    "apiKeyConfig", ballerinaAuthConfigGenerator.getApiKeyDescription());
            docs.add(apiKeyConfig);
        }
        // Create method description
        MarkdownParameterDocumentationLineNode clientConfig = DocCommentsGenerator.createAPIParamDoc("clientConfig",
                "The configurations to be used when initializing the `connector`");
        docs.add(clientConfig);
        MarkdownParameterDocumentationLineNode serviceUrlAPI = DocCommentsGenerator.createAPIParamDoc("serviceUrl",
                "URL of the target service");
        docs.add(serviceUrlAPI);
        MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGenerator.createAPIParamDoc("return",
                "An error if connector initialization failed");
        docs.add(returnDoc);
        MarkdownDocumentationNode clientInitDoc = createMarkdownDocumentationNode(createNodeList(docs));
        MetadataNode clientInit = createMetadataNode(clientInitDoc, createEmptyNodeList());
        return clientInit;
    }


    /**
     * Generate client class instance variables.
     *
     * @return {@link List<ObjectFieldNode>}    List of instance variables
     */
    private List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);
        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(HTTP),
                createToken(COLON_TOKEN), createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.CLIENT_EP);
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        ObjectFieldNode httpClientField = createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
        fieldNodeList.add(httpClientField);
        // add apiKey instance variable when API key security schema is given
        ObjectFieldNode apiKeyFieldNode = ballerinaAuthConfigGenerator.getApiKeyMapClassVariable();
        if (apiKeyFieldNode != null) {
            fieldNodeList.add(apiKeyFieldNode);
        }
        return fieldNodeList;
    }


    /**
     * Generate remote functions for OpenAPI operations.
     *
     * @param paths     openAPI Paths
     * @param filter    user given tags and operations
     * @return          FunctionDefinitionNodes list
     * @throws BallerinaOpenApiException - throws when creating remote functions fails
     */
    private  List<FunctionDefinitionNode> createRemoteFunctions(Paths paths, Filter filter)
            throws BallerinaOpenApiException {

        List<String> filterTags = filter.getTags();
        List<String> filterOperations = filter.getOperations();
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
        for (Map.Entry<String, PathItem> path : pathsItems) {
            if (!path.getValue().readOperationsMap().isEmpty()) {
                for (Map.Entry<PathItem.HttpMethod, Operation> operation :
                        path.getValue().readOperationsMap().entrySet()) {
                    // create display annotation of the operation
                    MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
                    Map<String, Object> extensions = operation.getValue().getExtensions();
                    if (extensions != null) {
                        for (Map.Entry<String, Object> extension : extensions.entrySet()) {
                            if (extension.getKey().trim().equals("x-display")) {
                                metadataNode = DocCommentsGenerator.getMetadataNodeForDisplayAnnotation(extension);
                                break;
                            }
                        }
                    }
                    List<String> operationTags = operation.getValue().getTags();
                    String operationId = operation.getValue().getOperationId();
                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                        // Generate remote function only if it is available in tag filter or operation filter or both
                        if (operationTags != null || ((!filterOperations.isEmpty()) && (operationId != null))) {
                            if (generatorUtils.hasTags(operationTags, filterTags) ||
                                    ((operationId != null) && filterOperations.contains(operationId.trim()))) {
                                // Generate remote function
                                FunctionDefinitionNode functionDefinitionNode =
                                        getRemoteFunctionDefinitionNode(metadataNode, path.getKey(), operation);
                                functionDefinitionNodeList.add(functionDefinitionNode);
                            }
                        }
                    } else {
                        // Generate remote function
                        FunctionDefinitionNode functionDefinitionNode = getRemoteFunctionDefinitionNode(metadataNode,
                                path.getKey(), operation);
                        functionDefinitionNodeList.add(functionDefinitionNode);
                    }
                }
            }
        }
        return functionDefinitionNodeList;
    }

    /**
     * Generate function definition node.
     * <pre>
     *     remote isolated function pathParameter(int 'version, string name) returns string|error {
     *          string  path = string `/v1/${'version}/v2/${name}`;
     *          string response = check self.clientEp-> get(path, targetType = string);
     *          return response;
     *    }
     * </pre>
     */
    private  FunctionDefinitionNode getRemoteFunctionDefinitionNode(MetadataNode metadataNode, String path,
                                                                    Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        // Create api doc for function
        List<Node> remoteFunctionDocs = new ArrayList<>();
        if (operation.getValue().getSummary() != null) {
            remoteFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    operation.getValue().getSummary(), true));
        } else if (operation.getValue().getDescription() != null) {
            remoteFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    operation.getValue().getDescription(), true));
        } else {
            MarkdownDocumentationLineNode newLine = createMarkdownDocumentationLineNode(null,
                    createToken(SyntaxKind.HASH_TOKEN), createEmptyNodeList());
            remoteFunctionDocs.add(newLine);
        }

        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createIdentifierToken("remote isolated"));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getValue().getOperationId());
        NodeList<Node> relativeResourcePath = createEmptyNodeList();
        remoteFunctionNameList.add(operation.getValue().getOperationId());

        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                ballerinaSchemaGenerator, typeDefinitionNodeList);
        FunctionSignatureNode functionSignatureNode =
                functionSignatureGenerator.getFunctionSignatureNode(operation.getValue(),
                        remoteFunctionDocs);
        typeDefinitionNodeList = functionSignatureGenerator.getTypeDefinitionNodeList();
        // Create metadataNode add documentation string
        metadataNode = metadataNode.modify(createMarkdownDocumentationNode(createNodeList(remoteFunctionDocs)),
                metadataNode.annotations());

        // Create Function Body
        FunctionBodyGenerator functionBodyGenerator = new FunctionBodyGenerator(imports, isQuery, isHeader,
                typeDefinitionNodeList, openAPI, ballerinaSchemaGenerator, ballerinaAuthConfigGenerator);
        FunctionBodyNode functionBodyNode = functionBodyGenerator.getFunctionBodyNode(path, operation);
        if (!isQuery) {
            isQuery = functionBodyGenerator.isQuery();
        }
        if (!isHeader) {
            isHeader = functionBodyGenerator.isHeader();
        }
        imports = functionBodyGenerator.getImports();

        return createFunctionDefinitionNode(null,
                metadataNode, qualifierList, functionKeyWord, functionName, relativeResourcePath,
                functionSignatureNode, functionBodyNode);
    }

    /**
     * Generate serverUrl for client default value.
     */
    private String getServerURL(List<Server> servers) throws BallerinaOpenApiException {
        String serverURL;
        Server selectedServer = servers.get(0);
        if (!selectedServer.getUrl().startsWith("https:") && servers.size() > 1) {
            for (Server server : servers) {
                if (server.getUrl().startsWith("https:")) {
                    selectedServer = server;
                    break;
                }
            }
        }
        GeneratorUtils generatorUtils = new GeneratorUtils();
        if (selectedServer.getUrl() == null) {
            serverURL = "http://localhost:9090/v1";
        } else if (selectedServer.getVariables() != null) {
            ServerVariables variables = selectedServer.getVariables();
            URL url;
            String resolvedUrl = generatorUtils.buildUrl(selectedServer.getUrl(), variables);
            try {
                url = new URL(resolvedUrl);
                serverURL = url.toString();
            } catch (MalformedURLException e) {
                throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
                        selectedServer.getUrl(), e);
            }
        } else {
            serverURL = selectedServer.getUrl();
        }
        return  serverURL;
    }

    /** Generate headerMap filtering functionDefinition Node.
     *
     * <pre>
     *     isolated function getMapForHeaders(map<any> headerParam) returns map<string|string[]> {
     *     map<string|string[]> headerMap = {};
     *     foreach var [key, value] in headerParam.entries() {
     *         if value is string {
     *             headerMap[key] = value;
     *         }
     *     }
     *     return headerMap;
     * }
     * </pre>
     *
     * @return functionDefinitionNode
     */
    private  FunctionDefinitionNode generateFunctionForHeaderMap() {
        // API doc for function
        List<Node> docs = new ArrayList<>();
        docs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                "Generate header map for given header values.", true));
        // Create client init description
        MarkdownParameterDocumentationLineNode queryParam = DocCommentsGenerator.createAPIParamDoc("headerParam",
                "Headers  map");
        docs.add(queryParam);
        MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGenerator.createAPIParamDoc("return",
                "Returns generated map or error at failure of client initialization");
        docs.add(returnDoc);

        MarkdownDocumentationNode functionDoc = createMarkdownDocumentationNode(createNodeList(docs));
        MetadataNode metadataNode = createMetadataNode(functionDoc, createEmptyNodeList());

        Token functionKeyWord = createIdentifierToken("isolated function");
        IdentifierToken functionName = createIdentifierToken(" getMapForHeaders");
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                createSeparatedNodeList(createRequiredParameterNode(createEmptyNodeList(),
                        createIdentifierToken("map<any>"),
                        createIdentifierToken("headerParam"))),
                createToken(CLOSE_PAREN_TOKEN),
                createReturnTypeDescriptorNode(createIdentifierToken(" returns "),
                        createEmptyNodeList(), createBuiltinSimpleNameReferenceNode(
                                null, createIdentifierToken("map<string|string[]>"))));

        List<StatementNode> statementNodes = new ArrayList<>();
        VariableDeclarationNode headerMap = generatorUtils.getSimpleStatement("map<string|string[]>",
                "headerMap", "{}");
        statementNodes.add(headerMap);
        // Create foreach loop
        Token forEachKeyWord = createToken(FOREACH_KEYWORD);

        BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" var"));
        ListBindingPatternNode bindingPattern =
                createListBindingPatternNode(createToken(OPEN_BRACKET_TOKEN),
                        createSeparatedNodeList(createCaptureBindingPatternNode(
                                createIdentifierToken("key")), createToken(COMMA_TOKEN),
                                createCaptureBindingPatternNode(createIdentifierToken("value"))),
                        createToken(CLOSE_BRACKET_TOKEN));

        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(type,
                bindingPattern);

        Token inKeyWord = createToken(IN_KEYWORD);
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken(" headerParam"));
        Token dotToken = createToken(DOT_TOKEN);
        SimpleNameReferenceNode methodName = createSimpleNameReferenceNode(createIdentifierToken("entries"));
        MethodCallExpressionNode actionOrExpr = createMethodCallExpressionNode(expr, dotToken, methodName
                , createToken(OPEN_PAREN_TOKEN), createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));
        //Foreach block statement
        List<StatementNode> foreachBlock = new ArrayList<>();
        // if-else statements
        Token ifKeyWord = createToken(IF_KEYWORD);
        // Create 'value is string' statement
        SimpleNameReferenceNode expression = createSimpleNameReferenceNode(createIdentifierToken(" value "));
        Token isKeyWord = createToken(IS_KEYWORD);
        BuiltinSimpleNameReferenceNode lhd = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" string"));
        TypeTestExpressionNode lhdCondition = createTypeTestExpressionNode(expression, isKeyWord,
                lhd);
        // Create 'value is string[]' statement
        BuiltinSimpleNameReferenceNode rhd = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" string[]"));
        TypeTestExpressionNode rhdCondition = createTypeTestExpressionNode(expression, isKeyWord,
                rhd);

        BinaryExpressionNode mainCondition = createBinaryExpressionNode(null, lhdCondition,
                createToken(LOGICAL_OR_TOKEN), rhdCondition);
        ExpressionStatementNode assignStatement = generatorUtils.getSimpleExpressionStatementNode("headerMap[key] = " +
                "value");
        BlockStatementNode ifBlockStatementMain = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(assignStatement), createToken(CLOSE_BRACE_TOKEN));
        IfElseStatementNode ifElseStatementNode =
                createIfElseStatementNode(ifKeyWord, mainCondition, ifBlockStatementMain, null);
        foreachBlock.add(ifElseStatementNode);
        BlockStatementNode forEachBlockStatement = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(foreachBlock), createToken(CLOSE_BRACE_TOKEN));

        ForEachStatementNode forEachStatementNode = createForEachStatementNode(forEachKeyWord,
                typedBindingPatternNode, inKeyWord, actionOrExpr, forEachBlockStatement, null);

        statementNodes.add(forEachStatementNode);

        // return statement
        ExpressionStatementNode returnHeaderMap = generatorUtils.getSimpleExpressionStatementNode("return headerMap");
        statementNodes.add(returnHeaderMap);

        FunctionBodyNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, createNodeList(statementNodes), createToken(CLOSE_BRACE_TOKEN));

        return createFunctionDefinitionNode(FUNCTION_DEFINITION, metadataNode, createEmptyNodeList(), functionKeyWord,
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);

    }

    /**
     * Generate queryParameter path generation functionDefinitionNode.
     *
     * @return functionDefinitionNode
     */
    private FunctionDefinitionNode generateFunctionForQueryParams() {
        // Add `import ballerina/url` to the import declaration node list
        ImportDeclarationNode url = GeneratorUtils.getImportDeclarationNode(
                GeneratorConstants.BALLERINA, "url");
        imports.add(url);
        //Create API doc
        List<Node> docs = new ArrayList<>();
        docs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                "Generate query path with query parameter.", true));
        // Create method description
        MarkdownParameterDocumentationLineNode queryParam = DocCommentsGenerator.createAPIParamDoc("queryParam",
                "Query parameter map");
        docs.add(queryParam);
        MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGenerator.createAPIParamDoc("return",
                "Returns generated Path or error at failure of client initialization");
        docs.add(returnDoc);

        MarkdownDocumentationNode functionDoc = createMarkdownDocumentationNode(createNodeList(docs));
        MetadataNode metadataNode = createMetadataNode(functionDoc, createEmptyNodeList());
        Token functionKeyWord = createIdentifierToken("isolated function");
        IdentifierToken functionName = createIdentifierToken(" getPathForQueryParam");
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                createSeparatedNodeList(createRequiredParameterNode(createEmptyNodeList(),
                        createIdentifierToken("map<anydata>"),
                        createIdentifierToken("queryParam"))),
                createToken(CLOSE_PAREN_TOKEN),
                createReturnTypeDescriptorNode(createIdentifierToken(" returns "),
                        createEmptyNodeList(), createBuiltinSimpleNameReferenceNode(
                                null, createIdentifierToken("string|error"))));

        // FunctionBody
        List<StatementNode> statementNodes = new ArrayList<>();
        VariableDeclarationNode variable = generatorUtils.getSimpleStatement("string[]", "param",
                "[]");
        statementNodes.add(variable);
        ExpressionStatementNode assign = generatorUtils.getSimpleExpressionStatementNode(
                "param[param.length()] = \"?\"");
        statementNodes.add(assign);

        // Create for each loop
        Token forEachKeyWord = createToken(FOREACH_KEYWORD);

        BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" var"));
        ListBindingPatternNode bindingPattern =
                createListBindingPatternNode(createToken(OPEN_BRACKET_TOKEN),
                        createSeparatedNodeList(createCaptureBindingPatternNode(
                                createIdentifierToken("key")), createToken(COMMA_TOKEN),
                                createCaptureBindingPatternNode(createIdentifierToken("value"))),
                        createToken(CLOSE_BRACKET_TOKEN));

        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(type,
                bindingPattern);

        Token inKeyWord = createToken(IN_KEYWORD);
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken(" queryParam"));
        Token dotToken = createToken(DOT_TOKEN);
        SimpleNameReferenceNode methodName = createSimpleNameReferenceNode(createIdentifierToken("entries"));
        MethodCallExpressionNode actionOrExpr = createMethodCallExpressionNode(expr, dotToken, methodName
                , createToken(OPEN_PAREN_TOKEN), createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));
        // block statement
        // if-else statements
        Token ifKeyWord = createToken(IF_KEYWORD);
        // Create 'value is ()' statement
        SimpleNameReferenceNode expression = createSimpleNameReferenceNode(createIdentifierToken(" value "));
        Token isKeyWord = createToken(IS_KEYWORD);
        BuiltinSimpleNameReferenceNode typeCondition = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" ()"));
        TypeTestExpressionNode mainCondition = createTypeTestExpressionNode(expression, isKeyWord,
                typeCondition);
        // If body
        ExpressionStatementNode assignStatement = generatorUtils.getSimpleExpressionStatementNode(
                "_ = queryParam.remove(key)");
        BlockStatementNode ifBlockStatementMain = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(assignStatement), createToken(CLOSE_BRACE_TOKEN));
        //else body
        Token elseKeyWord = createToken(ELSE_KEYWORD);
        //body statements
        FunctionCallExpressionNode condition = createFunctionCallExpressionNode(createSimpleNameReferenceNode(
                createIdentifierToken(" string:startsWith")), createToken(OPEN_PAREN_TOKEN),
                createSeparatedNodeList(
                        createSimpleNameReferenceNode(createIdentifierToken(" key")),
                        createToken(COMMA_TOKEN), createSimpleNameReferenceNode(
                                createIdentifierToken("\"'\""))), createToken(CLOSE_PAREN_TOKEN));
        List<StatementNode> statements = new ArrayList<>();
        // if body-02
        ExpressionStatementNode ifBody02Statement = generatorUtils.getSimpleExpressionStatementNode(
                " param[param.length()] = string:substring(key, 1, key.length())");

        NodeList<StatementNode> statementNodesForIf02 = createNodeList(ifBody02Statement);
        BlockStatementNode ifBlock02 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                statementNodesForIf02, createToken(CLOSE_BRACE_TOKEN));

        // else block-02
        // else body 02

        ExpressionStatementNode elseBody02Statement = generatorUtils.getSimpleExpressionStatementNode
                ("param[param.length()] = key");
        NodeList<StatementNode> statementNodesForElse02 = createNodeList(elseBody02Statement);
        BlockStatementNode elseBlockNode02 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                statementNodesForElse02, createToken(CLOSE_BRACE_TOKEN));

        ElseBlockNode elseBlock02 = createElseBlockNode(createToken(ELSE_KEYWORD), elseBlockNode02);
        IfElseStatementNode ifElseStatementNode02 = createIfElseStatementNode(ifKeyWord, condition, ifBlock02,
                elseBlock02);
        statements.add(ifElseStatementNode02);

        ExpressionStatementNode assignment = generatorUtils
                .getSimpleExpressionStatementNode("param[param.length()] = \"=\"");
        statements.add(assignment);

        //If block 03
        SimpleNameReferenceNode exprIf03 = createSimpleNameReferenceNode(createIdentifierToken(" value "));
        BuiltinSimpleNameReferenceNode typeCondition03 = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" string"));
        TypeTestExpressionNode condition03 = createTypeTestExpressionNode(exprIf03, isKeyWord, typeCondition03);

        ExpressionStatementNode variableIf03 = generatorUtils.getSimpleExpressionStatementNode(
                "string updateV =  check url:encode(value, \"UTF-8\")");
        ExpressionStatementNode assignIf03 = generatorUtils
                .getSimpleExpressionStatementNode("param[param.length()] = updateV");

        BlockStatementNode ifBody03 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(variableIf03, assignIf03), createToken(CLOSE_BRACE_TOKEN));
        BlockStatementNode elseBodyBlock03 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), createNodeList(
                        generatorUtils.getSimpleExpressionStatementNode("param[param.length()] = value.toString()")),
                createToken(CLOSE_BRACE_TOKEN));
        ElseBlockNode elseBody03 = createElseBlockNode(elseKeyWord, elseBodyBlock03);
        IfElseStatementNode ifElse03 = createIfElseStatementNode(ifKeyWord, condition03, ifBody03, elseBody03);

        statements.add(ifElse03);

        ExpressionStatementNode andStatement = generatorUtils
                .getSimpleExpressionStatementNode("param[param.length()] = \"&\"");
        statements.add(andStatement);

        NodeList<StatementNode> elseBodyStatements = createNodeList(statements);

        BlockStatementNode elseBlock = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), elseBodyStatements,
                createToken(CLOSE_BRACE_TOKEN));

        ElseBlockNode elseBlockMain = createElseBlockNode(elseKeyWord, elseBlock);

        IfElseStatementNode mainIfElse = createIfElseStatementNode(ifKeyWord, mainCondition,
                ifBlockStatementMain, elseBlockMain);

        //For each block statement
        BlockStatementNode forEachBlockStatement = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(mainIfElse), createToken(CLOSE_BRACE_TOKEN));

        ForEachStatementNode forEachStatementNode = createForEachStatementNode(forEachKeyWord,
                typedBindingPatternNode, inKeyWord, actionOrExpr, forEachBlockStatement, null);

        statementNodes.add(forEachStatementNode);

        //Remove last `&` statement
        ExpressionStatementNode assignLine02 = generatorUtils.getSimpleExpressionStatementNode(
                "_ = param.remove(param.length()-1)");
        statementNodes.add(assignLine02);

        //IfElseStatement
        SimpleNameReferenceNode lhs = createSimpleNameReferenceNode(createIdentifierToken(" param.length()"));
        Token equalToken = createIdentifierToken("==");
        BuiltinSimpleNameReferenceNode rhs = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" 1"));
        TypeTestExpressionNode conditionForIfElse = createTypeTestExpressionNode(lhs, equalToken, rhs);
        //if body block

        ExpressionStatementNode newAssign = generatorUtils.getSimpleExpressionStatementNode("_ = param.remove(0)");
        BlockStatementNode ifBlock = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(newAssign), createToken(CLOSE_BRACE_TOKEN));
        IfElseStatementNode ifElseStatementNode = createIfElseStatementNode(ifKeyWord, conditionForIfElse, ifBlock,
                null);
        statementNodes.add(ifElseStatementNode);

        statementNodes.add(
                generatorUtils.getSimpleExpressionStatementNode("string restOfPath = string:'join(\"\", ...param)"));
        statementNodes.add(generatorUtils.getSimpleExpressionStatementNode("return restOfPath"));
        FunctionBodyNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, createNodeList(statementNodes), createToken(CLOSE_BRACE_TOKEN));

        return createFunctionDefinitionNode(FUNCTION_DEFINITION, metadataNode,
                createEmptyNodeList(), functionKeyWord, functionName, createEmptyNodeList(),
                functionSignatureNode, functionBodyNode);
    }

    /**
     * Return auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType () {
        return ballerinaAuthConfigGenerator.getAuthType();
    }
}
