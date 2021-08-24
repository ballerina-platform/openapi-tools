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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
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
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
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
import org.apache.commons.io.IOUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.HTTP;

/**
 * This Util class uses for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    private Filter filters;
    private List<ImportDeclarationNode> imports;
    private boolean isQuery;
    private boolean isHeader;
    private List<TypeDefinitionNode> typeDefinitionNodeList;
    private List<TypeDefinitionNode> typeDefinitionNodeListWithAuth;
    private OpenAPI openAPI;
    private BallerinaSchemaGenerator ballerinaSchemaGenerator;
    private GeneratorUtils generatorUtils;
    private List<String> remoteFunctionNameList;
    private String serverURL;
    private BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator;

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
    public BallerinaClientGenerator(Filter filters,
                                    List<ImportDeclarationNode> imports, boolean isQuery, boolean isHeader,
                                    List<TypeDefinitionNode> typeDefinitionNodeList,
                                    OpenAPI openAPI,
                                    BallerinaSchemaGenerator ballerinaSchemaGenerator) {

        this.filters = filters;
        this.imports = imports;
        this.isQuery = isQuery;
        this.isHeader = isHeader;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.generatorUtils = new GeneratorUtils();
        this.typeDefinitionNodeListWithAuth =  new ArrayList<>();
        this.remoteFunctionNameList = new ArrayList<>();
        this.serverURL = "/";
        this.ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator();
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
        this.typeDefinitionNodeListWithAuth =  new ArrayList<>();
        this.remoteFunctionNameList = new ArrayList<>();
        this.serverURL = "/";
        this.ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(false, false);
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return              - return Syntax tree for the ballerina code.
     * @throws IOException  - throws exception when function fail in process.
     * @throws BallerinaOpenApiException - throws exception when function fail in process.
     */
    private SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {

        SyntaxTree syntaxTree;
        // Create imports http
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        addConfigRecordToTypeDefinitionNodeList(openAPI, ballerinaAuthConfigGenerator);
        ClassDefinitionNode classDefinitionNode = getClassDefinitionNode();
        ModulePartNode modulePartNode;
        List<ModuleMemberDeclarationNode> nodes =  new ArrayList<>();
        nodes.addAll(typeDefinitionNodeListWithAuth);
        nodes.add(classDefinitionNode);
        if (isQuery) {
            ImportDeclarationNode url = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, "url");
            imports.add(url);
        }

        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        modulePartNode = createModulePartNode(importsList, createNodeList(nodes),
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from("");
        syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    public String getClient() throws BallerinaOpenApiException, FormatterException, IOException {
        String clientSyntaxTreeString = Formatter.format(generateSyntaxTree()).toString();
        ClassLoader classLoader = getClass().getClassLoader();
        if (isQuery) {
            InputStream inputStream = classLoader.getResourceAsStream("util_functions/getPathForQueryParam.txt");
            if (inputStream != null) {
                clientSyntaxTreeString = clientSyntaxTreeString + IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }
        if (isHeader) {
            InputStream inputStream = classLoader.getResourceAsStream("util_functions/getMapForHeader.txt");
            if (inputStream != null) {
                clientSyntaxTreeString = clientSyntaxTreeString + IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }
        return clientSyntaxTreeString;
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

        // Generate client class
        Token visibilityQualifier = createToken(PUBLIC_KEYWORD);
        Token isolatedToken = createToken(ISOLATED_KEYWORD);
        Token clientToken = createToken(CLIENT_KEYWORD);
        NodeList<Token> classTypeQualifiers = createNodeList(isolatedToken, clientToken);

        IdentifierToken classKeyWord = createIdentifierToken(GeneratorConstants.CLASS);
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        Token openBrace = createToken(OPEN_BRACE_TOKEN);
        //Fill the members for class definition node
        List<Node> memberNodeList =  new ArrayList<>();
        //Create class field
        List<ObjectFieldNode> fieldNodeList = getClassField(ballerinaAuthConfigGenerator);
        memberNodeList.addAll(fieldNodeList);
        //Create init function definition
        //Common Used
        NodeList<Token> qualifierList = createNodeList(createIdentifierToken(GeneratorConstants.PUBLIC_ISOLATED));
        IdentifierToken functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION);
        IdentifierToken functionName = createIdentifierToken("init");
        //Create function signature for client class init
        //Add parameters
        List<Node> parameters  = new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        // get service URL
        serverURL = getServerURL(openAPI.getServers());

        //Get config parameters relevant to the auth mechanism used
        parameters.addAll(ballerinaAuthConfigGenerator.getConfigParamForClassInit(serverURL));
        // Client init api documentation
        List<Node> docs = new ArrayList<>();
        String clientInitDocCommentLines = "Gets invoked to initialize the `connector`.\n";
        if (openAPI.getInfo().getExtensions() != null && !openAPI.getInfo().getExtensions().isEmpty()) {
            Map<String, Object> extensions = openAPI.getInfo().getExtensions();
            for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                if (extension.getKey().trim().equals("x-init-description")) {
                    clientInitDocCommentLines = clientInitDocCommentLines.concat(extension.getValue().toString());
                    break;
                }
            }
        }
        docs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(clientInitDocCommentLines, true));
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

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        //Create return type node for inti function
        IdentifierToken returnsKeyWord = createIdentifierToken(GeneratorConstants.RETURN);
        OptionalTypeDescriptorNode type = createOptionalTypeDescriptorNode(createIdentifierToken("error"),
                createIdentifierToken("?"));
        ReturnTypeDescriptorNode returnNode = createReturnTypeDescriptorNode(returnsKeyWord, annotationNodes, type);

        //Create function signature
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnNode);

        //Create function body node client init
        VariableDeclarationNode clientInitializationNode = ballerinaAuthConfigGenerator.getClientInitializationNode();

        //Assigment for client
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("clientEp")));

        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken("httpEp"));
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));
        AssignmentStatementNode assignmentStatementNodeApiKey = ballerinaAuthConfigGenerator.
                getApiKeyAssignmentNode();

        List<StatementNode> assignmentNodes = new ArrayList<>();
        assignmentNodes.add(clientInitializationNode);
        assignmentNodes.add(httpClientAssignmentStatementNode);
        if (assignmentStatementNodeApiKey != null) {
            assignmentNodes.add(assignmentStatementNodeApiKey);
        }
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        FunctionBodyNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
        FunctionDefinitionNode initFunctionNode = createFunctionDefinitionNode(null, clientInit,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(), functionSignatureNode
                , functionBodyNode);

        memberNodeList.add(initFunctionNode);
        // Generate remote function Nodes -- check the null of info and path
        memberNodeList.addAll(createRemoteFunctions(openAPI.getPaths(), filters));
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
        // Generate api doc
        List<Node> documentationLines = new ArrayList<>();
        if (openAPI.getInfo().getDescription() != null) {
            documentationLines.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    openAPI.getInfo().getDescription(), false));
        }
        MarkdownDocumentationNode apiDoc = createMarkdownDocumentationNode(createNodeList(documentationLines));
        metadataNode = metadataNode.modify(apiDoc, metadataNode.annotations());
        return createClassDefinitionNode(metadataNode, visibilityQualifier, classTypeQualifiers,
                classKeyWord, className, openBrace, createNodeList(memberNodeList),
                createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate Client class attributes.
     */
    private List<ObjectFieldNode> getClassField(BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator) {
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
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
        for (Map.Entry<String, PathItem> path : pathsItems) {
            if (!path.getValue().readOperationsMap().isEmpty()) {
                Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                for (Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iter = operationMap.entrySet().iterator();
                     iter.hasNext(); ) {
                    Map.Entry<PathItem.HttpMethod, Operation> operation = iter.next();
                    //Add filter availability
                    //1.Tag filter
                    //2.Operation filter
                    //3.Both tag and operation filter
                    List<String> filterTags = filter.getTags();
                    List<String> operationTags = operation.getValue().getTags();
                    List<String> filterOperations = filter.getOperations();
                    // Handle the display annotations
                    MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
                    Map<String, Object> extensions = operation.getValue().getExtensions();
                    if (extensions != null) {
                        for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                            if (extension.getKey().trim().equals("x-display")) {
                                metadataNode = DocCommentsGenerator.getMetadataNodeForDisplayAnnotation(extension);
                            }
                        }
                    }

                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                        if (operationTags != null || ((!filterOperations.isEmpty())
                                && (operation.getValue().getOperationId() != null))) {
                            if (generatorUtils.hasTags(operationTags, filterTags) ||
                                    ((operation.getValue().getOperationId() != null) &&
                                            filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                                // function call for generate function definition node.
                                FunctionDefinitionNode functionDefinitionNode =
                                        getFunctionDefinitionNode(metadataNode, path.getKey()
                                                , operation);
                                functionDefinitionNodeList.add(functionDefinitionNode);
                            }
                        }
                    } else {
                        FunctionDefinitionNode functionDefinitionNode = getFunctionDefinitionNode(metadataNode,
                                path.getKey(),
                                operation);
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
    private  FunctionDefinitionNode getFunctionDefinitionNode(MetadataNode metadataNode, String path,
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
     * Generate Auth related record s for the client.
     *
     * @param openAPI - OpenAPI specification.
     * @param ballerinaAuthConfigGenerator - {@Link BallerinaAuthConfigGenerator}
     */
    private void addConfigRecordToTypeDefinitionNodeList(OpenAPI openAPI,
                                                         BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator)
            throws BallerinaOpenApiException {
        TypeDefinitionNode configRecord = ballerinaAuthConfigGenerator.getConfigRecord(openAPI);
        if (configRecord != null) {
            typeDefinitionNodeListWithAuth.add(configRecord);
        }
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
