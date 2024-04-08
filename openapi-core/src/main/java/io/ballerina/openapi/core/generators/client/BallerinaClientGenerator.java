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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.ClientDocCommentGenerator;
import io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.servers.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ERROR_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_API_KEY_DESC;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SELF;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_BALLERINA_INIT_DESCRIPTION;

/**
 * This class is used to generate ballerina client file according to given yaml file.
 *
 * @since 1.3.0
 */
public class BallerinaClientGenerator {

    private final Filter filter;
    private List<ImportDeclarationNode> imports = new ArrayList<>();
    private List<String> apiKeyNameList = new ArrayList<>();
    private final OpenAPI openAPI;
    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final List<String> remoteFunctionNameList;
    private final AuthConfigGeneratorImp authConfigGeneratorImp;
    private final boolean resourceMode;
    private final List<ClientDiagnostic> diagnostics = new ArrayList<>();

    /**
     * Return a Diagnostic list.
     */
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }


    /**
     * Returns ballerinaAuthConfigGenerator.
     */
    public AuthConfigGeneratorImp getBallerinaAuthConfigGenerator() {

        return authConfigGeneratorImp;
    }


    public List<String> getRemoteFunctionNameList() {

        return remoteFunctionNameList;
    }

    /**
     * //todo: add changes
     * Returns server URL.
     *
     * @return {@link String}
     */
    public String getServerUrl() {
        return "serverURL";
    }

    public BallerinaClientGenerator(OASClientConfig oasClientConfig) {

        this.filter = oasClientConfig.getFilter();
        this.openAPI = oasClientConfig.getOpenAPI();
        this.ballerinaUtilGenerator = new BallerinaUtilGenerator();
        this.remoteFunctionNameList = new ArrayList<>();
        this.authConfigGeneratorImp = new AuthConfigGeneratorImp(false, false);
        this.resourceMode = oasClientConfig.isResourceMode();
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return return Syntax tree for the ballerina code.
     * @throws BallerinaOpenApiException When function fail in process.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException, ClientException {

        // Create `ballerina/http` import declaration node
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , HTTP);
        imports.add(importForHttp);
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        // Add authentication related records
        authConfigGeneratorImp.addAuthRelatedRecords(openAPI);
        // Add class definition node to module member nodes
        nodes.add(getClassDefinitionNode());

        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(nodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = syntaxTree.modifyWith(modulePartNode);
        //Add comments
        ClientDocCommentGenerator clientDocCommentGenerator = new ClientDocCommentGenerator(syntaxTree, openAPI, resourceMode);
        return clientDocCommentGenerator.updateSyntaxTreeWithDocComments();
    }

    public BallerinaUtilGenerator getBallerinaUtilGenerator() {
        return ballerinaUtilGenerator;
    }

    /**
     * Generate Class definition Node with below code structure.
     * <pre>
     *     public isolated client class Client {
     *     final http:Client clientEp;
     *     public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "/url")
     *     returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *     }
     *     // Remote functions
     *     remote isolated function pathParameter(int 'version, string name) returns string|error {
     *         string  path = string `/v1/${'version}/v2/${name}`;
     *         string response = check self.clientEp-> get(path);
     *         return response;
     *     }
     * }
     * </pre>
     */
    private ClassDefinitionNode getClassDefinitionNode() {
        // Collect members for class definition node
        List<Node> memberNodeList = new ArrayList<>();
        // Add instance variable to class definition node
        memberNodeList.addAll(createClassInstanceVariables());
        // Add init function to class definition node
        memberNodeList.add(createInitFunction());
        Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperations = filterOperations();
        //switch resource remote
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        if (resourceMode) {
            functionDefinitionNodeList.addAll(createResourceFunctions(filteredOperations));
        } else {
            functionDefinitionNodeList.addAll(createRemoteFunctions(filteredOperations));
        }
        memberNodeList.addAll(functionDefinitionNodeList);
        // Generate the class combining members
        MetadataNode metadataNode = getClassMetadataNode();
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * This function is to filter the operations based on the user given tags and operations
     */
    private Map<String, Map<PathItem.HttpMethod, Operation>> filterOperations() {
        //todo refactor code
        Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperation = new HashMap<>();
        List<String> filterTags = filter.getTags();
        List<String> filterOperations = filter.getOperations();
        Set<Map.Entry<String, PathItem>> pathsItems = openAPI.getPaths().entrySet();
        for (Map.Entry<String, PathItem> path : pathsItems) {
            Map<PathItem.HttpMethod, Operation> operations = new HashMap<>();
            if (!path.getValue().readOperationsMap().isEmpty()) {
                Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
                    List<String> operationTags = operation.getValue().getTags();
                    String operationId = operation.getValue().getOperationId();
                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                        // Generate remote function only if it is available in tag filter or operation filter or both
                        if (operationTags != null || ((!filterOperations.isEmpty()) && (operationId != null))) {
                            if (isaFilteredOperation(filterTags, filterOperations, operationTags, operationId)) {
                                operations.put(operation.getKey(), operation.getValue());
                            }
                        }
                    } else {
                        operations.put(operation.getKey(), operation.getValue());
                    }
                }
                if (!operations.isEmpty()) {
                    filteredOperation.put(path.getKey(), operations);
                }
            }
        }
        return filteredOperation;
    }



    /**
     * Generate metadata node of the class including documentation and display annotation. Content of the documentation
     * will be taken from the `description` section inside the `info` section in OpenAPI definition.
     *
     * @return {@link MetadataNode}    Metadata node of the client class
     */
    private MetadataNode getClassMetadataNode() {

        List<AnnotationNode> classLevelAnnotationNodes = new ArrayList<>();
        if (openAPI.getInfo().getExtensions() != null) {
            Map<String, Object> extensions = openAPI.getInfo().getExtensions();
            DocCommentsGeneratorUtil.extractDisplayAnnotation(extensions, classLevelAnnotationNodes);
        }
        // Generate api doc
        List<Node> documentationLines = new ArrayList<>();
        if (openAPI.getInfo().getDescription() != null && !openAPI.getInfo().getDescription().isBlank()) {
            documentationLines.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(
                    openAPI.getInfo().getDescription(), false));
        }
        MarkdownDocumentationNode apiDoc = createMarkdownDocumentationNode(createNodeList(documentationLines));
        return createMetadataNode(apiDoc, createNodeList(classLevelAnnotationNodes));
    }

    /**
     * Create client class init function
     * -- Scenario 1: init function when authentication mechanism is API key
     * <pre>
     *   public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl,
     *     ConnectionConfig config =  {}) returns error? {
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
     *   public isolated function init(ConnectionConfig config =  {},
     *      string serviceUrl = "base-url") returns error? {
     *         http:Client httpEp = check new (serviceUrl, clientConfig);
     *         self.clientEp = httpEp;
     *   }
     * </pre>
     *
     * @return {@link FunctionDefinitionNode}   Class init function
     * @throws BallerinaOpenApiException When invalid server URL is provided
     */
    private FunctionDefinitionNode createInitFunction() {

        FunctionSignatureNode functionSignatureNode = getInitFunctionSignatureNode();
        FunctionBodyNode functionBodyNode = getInitFunctionBodyNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        return createFunctionDefinitionNode(FUNCTION_DEFINITION, getInitDocComment(), qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Create function body node of client init function.
     *
     * @return {@link FunctionBodyNode}
     */
    private FunctionBodyNode getInitFunctionBodyNode() {

        List<StatementNode> assignmentNodes = new ArrayList<>();

        assignmentNodes.add(authConfigGeneratorImp.getHttpClientConfigVariableNode());
        assignmentNodes.add(authConfigGeneratorImp.getClientConfigDoStatementNode());

        // If both apiKey and httpOrOAuth is supported
        // todo : After revamping
        if (authConfigGeneratorImp.isApiKey() && authConfigGeneratorImp.isHttpOROAuth()) {
            assignmentNodes.add(authConfigGeneratorImp.handleInitForMixOfApiKeyAndHTTPOrOAuth());
        }
        // create initialization statement of http:Client class instance
        assignmentNodes.add(authConfigGeneratorImp.getClientInitializationNode());
        // create {@code self.clientEp = httpEp;} assignment node
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("clientEp")));
        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken("httpEp"));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(httpClientAssignmentStatementNode);


        // Get API key assignment node if authentication mechanism type is only `apiKey`
        if (authConfigGeneratorImp.isApiKey() && !authConfigGeneratorImp.isHttpOROAuth()) {
            assignmentNodes.add(authConfigGeneratorImp.getApiKeyAssignmentNode());
        }
        if (authConfigGeneratorImp.isApiKey()) {
            List<String> apiKeyNames = new ArrayList<>();
            apiKeyNames.addAll(authConfigGeneratorImp.getHeaderApiKeyNameList().values());
            apiKeyNames.addAll(authConfigGeneratorImp.getQueryApiKeyNameList().values());
            setApiKeyNameList(apiKeyNames);
        }
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Create function signature node of client init function.
     *
     * @return {@link FunctionSignatureNode}
     * @throws BallerinaOpenApiException When invalid server URL is provided
     */
    private FunctionSignatureNode getInitFunctionSignatureNode() {
        List<ParameterNode> parameterNodes  = new ArrayList<>();
        ServerURLGeneratorImp serverURLGeneratorImp = new ServerURLGeneratorImp(openAPI.getServers());
        ParameterNode serverURLNode = serverURLGeneratorImp.generateServerURL();
        diagnostics.addAll(serverURLGeneratorImp.getDiagnostics());
        parameterNodes.add(serverURLNode);
        // get auth config details
        List<ParameterNode> configParams = authConfigGeneratorImp.getConfigParamForClassInit();
        diagnostics.addAll(authConfigGeneratorImp.getDiagnostics());
        parameterNodes.addAll(configParams);
        LinkedHashSet<Node> orderedParam = sortParameters(parameterNodes);
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(orderedParam);
        OptionalTypeDescriptorNode returnType = createOptionalTypeDescriptorNode(createToken(ERROR_KEYWORD),
                createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);
        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }


    /**
     * Sort the parameters of the init function.
     *
     * @param parameters list of parameters
     * @return sorted list of parameters
     */
    private LinkedHashSet<Node> sortParameters(List<ParameterNode> parameters) {
        // init (required, default)
        HashMap<String, Node> requiredParams = new HashMap<>();
        List<String> requiredParamNames = new ArrayList<>();
        List<String> defaultParamNames = new ArrayList<>();

        HashMap<String, Node> defaultParams = new HashMap<>();
        for (ParameterNode parameter : parameters) {
            if (parameter instanceof RequiredParameterNode requiredParameterNode) {
                String paramName = requiredParameterNode.paramName().get().toString();
                requiredParamNames.add(paramName);
                requiredParams.put(paramName, requiredParameterNode);
            } else if (parameter instanceof DefaultableParameterNode defaultableParameterNode) {
                String paramName = defaultableParameterNode.paramName().get().toString();
                defaultParams.put(paramName, defaultableParameterNode);
                defaultParamNames.add(paramName);
            }
        }
        List<String> sortedRequiredParamNames = ascendingOrder(requiredParamNames);
        List<String> sortedDefaultParamNames = ascendingOrder(defaultParamNames);
        LinkedHashSet<Node> paramNodes = new LinkedHashSet<>();
        // todo handle the parameter order in ascending order
        for (String paramName : sortedRequiredParamNames) {
            paramNodes.add(requiredParams.get(paramName));
            paramNodes.add(createToken(COMMA_TOKEN));
        }
        for (String paramName : sortedDefaultParamNames) {
            paramNodes.add(defaultParams.get(paramName));
            paramNodes.add(createToken(COMMA_TOKEN));
        }

        // remove trailing comma
        if (!paramNodes.isEmpty()) {
            if (paramNodes.toArray()[paramNodes.size() - 1] instanceof Token) {
                paramNodes.remove(paramNodes.toArray()[paramNodes.size() - 1]);
            }
        }
        return paramNodes;
    }

    public static List<String> ascendingOrder(List<String> inputList) {
        List<String> sortedList = new ArrayList<>(inputList);
        Collections.sort(sortedList);
        return sortedList;
    }


    /**
     * Provide client class init function's documentation including function description and parameter descriptions.
     *
     * @return {@link MetadataNode}    Metadata node containing entire function documentation comment.
     */
    private MetadataNode getInitDocComment() {

        List<Node> docs = new ArrayList<>();
        String clientInitDocComment = "Gets invoked to initialize the `connector`.\n";
        if (openAPI.getInfo().getExtensions() != null && !openAPI.getInfo().getExtensions().isEmpty()) {
            Map<String, Object> extensions = openAPI.getInfo().getExtensions();
            for (Map.Entry<String, Object> extension : extensions.entrySet()) {
                if (extension.getKey().trim().equals(X_BALLERINA_INIT_DESCRIPTION)) {
                    clientInitDocComment = clientInitDocComment.concat(extension.getValue().toString());
                    break;
                }
            }
        }
        docs.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(clientInitDocComment, true));
        if (authConfigGeneratorImp.isApiKey() && !authConfigGeneratorImp.isHttpOROAuth()) {
            MarkdownParameterDocumentationLineNode apiKeyConfig = DocCommentsGeneratorUtil.createAPIParamDoc(
                    "apiKeyConfig", DEFAULT_API_KEY_DESC);
            docs.add(apiKeyConfig);
        }
        // Create method description
        MarkdownParameterDocumentationLineNode clientConfig = DocCommentsGeneratorUtil.
                createAPIParamDoc("config",
                        "The configurations to be used when initializing the `connector`");
        docs.add(clientConfig);
        MarkdownParameterDocumentationLineNode serviceUrlAPI = DocCommentsGeneratorUtil.createAPIParamDoc(
                "serviceUrl", "URL of the target service");
        docs.add(serviceUrlAPI);
        MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGeneratorUtil.createAPIParamDoc(
                "return", "An error if connector initialization failed");
        docs.add(returnDoc);
        MarkdownDocumentationNode clientInitDoc = createMarkdownDocumentationNode(createNodeList(docs));
        return createMetadataNode(clientInitDoc, createEmptyNodeList());
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
        ObjectFieldNode apiKeyFieldNode = authConfigGeneratorImp.getApiKeyMapClassVariable();
        if (apiKeyFieldNode != null) {
            fieldNodeList.add(apiKeyFieldNode);
        }
        return fieldNodeList;
    }

    private static boolean isaFilteredOperation(List<String> filterTags, List<String> filterOperations,
                                                List<String> operationTags, String operationId) {
        return (operationTags != null && GeneratorUtils.hasTags(operationTags, filterTags)) ||
                ((operationId != null) && filterOperations.contains(operationId.trim()));
    }

    private List<FunctionDefinitionNode> createRemoteFunctions(Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperations) {
        List<FunctionDefinitionNode> remoteFunctionNodes = new ArrayList<>();
        for (Map.Entry<String, Map<PathItem.HttpMethod, Operation>> operation : filteredOperations.entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operation.getValue().entrySet()) {
                RemoteFunctionGenerator remoteFunctionGenerator = new RemoteFunctionGenerator(operation.getKey(), operationEntry, openAPI, authConfigGeneratorImp, ballerinaUtilGenerator, imports);
                Optional<FunctionDefinitionNode> remotefunction = remoteFunctionGenerator.generateFunction();
                remotefunction.ifPresent(remoteFunctionNodes::add);
                diagnostics.addAll(remoteFunctionGenerator.getDiagnostics());
            }
        }
        return remoteFunctionNodes;
    }


    private List<FunctionDefinitionNode> createResourceFunctions(Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperations) {
        List<FunctionDefinitionNode> resourceFunctionNodes = new ArrayList<>();
        for (Map.Entry<String, Map<PathItem.HttpMethod, Operation>> operation : filteredOperations.entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operation.getValue().entrySet()) {
                ResourceFunctionGenerator resourceFunctionGenerator = new ResourceFunctionGenerator(operationEntry,
                        operation.getKey(), openAPI, authConfigGeneratorImp, ballerinaUtilGenerator, imports);
                Optional<FunctionDefinitionNode> resourceFunction = resourceFunctionGenerator.generateFunction();
                resourceFunction.ifPresent(resourceFunctionNodes::add);
                diagnostics.addAll(resourceFunctionGenerator.getDiagnostics());
            }
        }
        return resourceFunctionNodes;
    }



    /**
     * Return auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType() {
        return authConfigGeneratorImp.getAuthType();
    }

    /**
     * Provide list of the field names in ApiKeysConfig record to generate the Config.toml file.
     *
     * @return {@link List<String>}
     */
    public List<String> getApiKeyNameList() {
        return apiKeyNameList;
    }

    /**
     * Set the `apiKeyNameList` by adding the Api Key names available under security schemas.
     *
     * @param apiKeyNameList {@link List<String>}
     */
    public void setApiKeyNameList(List<String> apiKeyNameList) {
        this.apiKeyNameList = apiKeyNameList;
    }
}
