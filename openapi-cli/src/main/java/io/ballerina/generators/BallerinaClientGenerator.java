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

package io.ballerina.generators;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListBindingPatternNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createElseBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createErrorTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createForEachStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createListBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNamedArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRemoteMethodCallActionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTemplateExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeTestExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELSE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FOREACH_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RIGHT_ARROW_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static org.ballerinalang.generators.GeneratorConstants.HTTP;
import static org.ballerinalang.generators.GeneratorConstants.OPEN_PRAN;
import static org.ballerinalang.generators.GeneratorUtils.buildUrl;
import static org.ballerinalang.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static org.ballerinalang.generators.GeneratorUtils.escapeIdentifier;
import static org.ballerinalang.generators.GeneratorUtils.extractReferenceType;
import static org.ballerinalang.generators.GeneratorUtils.getBallerinaMeidaType;
import static org.ballerinalang.generators.GeneratorUtils.getBallerinaOpenApiType;
import static io.ballerina.generators.GeneratorConstants.HTTP;
import static io.ballerina.generators.GeneratorConstants.OPEN_PRAN;

/**
 * This Util class use for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    private static Server server;
    private static Paths paths;
    private static Filter filters;
    private static List<ImportDeclarationNode> imports = new ArrayList<>();
    private static boolean isQuery;
    private static Info info;
    private static List<TypeDefinitionNode> typeDefinitionNodeList = new ArrayList<>();

    public static SyntaxTree generateSyntaxTree(Path definitionPath, Filter filter) throws IOException,
            BallerinaOpenApiException {
        imports.clear();
        typeDefinitionNodeList.clear();
        isQuery = false;
        // Summaries OpenAPI details
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        info = openAPI.getInfo();
        //Filter serverUrl
        List<Server> servers = openAPI.getServers();
        server = servers.get(0);

        paths = openAPI.getPaths();

        paths = setOperationId(paths);
        filters = filter;
        // 1. Load client template syntax tree
        SyntaxTree syntaxTree = null;
        // Create imports http
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        ClassDefinitionNode classDefinitionNode = getClassDefinitionNode();
        ModulePartNode modulePartNode;
        List<ModuleMemberDeclarationNode> nodes =  new ArrayList<>();
        nodes.addAll(typeDefinitionNodeList);
        nodes.add(classDefinitionNode);
        if (isQuery) {
            ImportDeclarationNode url = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, "url");
            ImportDeclarationNode string = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, "lang.'string");
            imports.add(url);
            imports.add(string);
            NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
            FunctionDefinitionNode queryParamFunction = getQueryParamPath();

            nodes.add(queryParamFunction);
            modulePartNode = createModulePartNode(importsList,
                    createNodeList(nodes),
                    createToken(EOF_TOKEN));

        } else {
            NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
            modulePartNode = createModulePartNode(importsList, createNodeList(nodes),
                    createToken(EOF_TOKEN));
        }
        TextDocument textDocument = TextDocuments.from("");
        syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generate serverUrl for client default value.
     */
    private static String getServerURL(Server server) throws BallerinaOpenApiException {
        String serverURL;
        if (server != null) {
            if (server.getUrl() == null) {
                serverURL = "http://localhost:9090/v1";
            } else if (server.getVariables() != null) {
                ServerVariables variables = server.getVariables();
                URL url;
                String resolvedUrl = buildUrl(server.getUrl(), variables);
                try {
                    url = new URL(resolvedUrl);
                    serverURL = url.toString();
                } catch (MalformedURLException e) {
                    throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
                            server.getUrl(), e);
                }
            } else {
                serverURL = server.getUrl();
            }
        } else {
            serverURL = "http://localhost:9090/v1";
        }
        return  serverURL;
    }

    /**
     * Generate Class definition Node.
     */
    private static ClassDefinitionNode getClassDefinitionNode() throws BallerinaOpenApiException {

        // Generate client class
        Token visibilityQualifier = createIdentifierToken(GeneratorConstants.PUBLIC);
        Token clientKeyWord = createIdentifierToken(GeneratorConstants.CLIENT);
        NodeList<Token> classTypeQualifiers = createNodeList(clientKeyWord);

        IdentifierToken classKeyWord = createIdentifierToken(GeneratorConstants.CLASS);
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        Token openBrace = createIdentifierToken(GeneratorConstants.OPEN_BRACE);
        //Fill the members for class definition node
        List<Node> memberNodeList =  new ArrayList<>();
        //Create class field
        ObjectFieldNode fieldNode = getClassField();
        memberNodeList.add(fieldNode);
        //Create init function definition
        //Common Used
        NodeList<Token> qualifierList = createNodeList(createIdentifierToken(GeneratorConstants.PUBLIC_ISOLATED));
        IdentifierToken functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION);
        IdentifierToken functionName = createIdentifierToken("init");
        //Create function signature
        //Add parameters
        List<Node> parameters  = new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("string"));
        IdentifierToken paramName = createIdentifierToken("serviceUrl");
        IdentifierToken equalToken = createIdentifierToken("=");
        BasicLiteralNode expression = createBasicLiteralNode(STRING_LITERAL,
                createIdentifierToken('"' + getServerURL(server) + '"'));

        DefaultableParameterNode serviceUrl = createDefaultableParameterNode(annotationNodes, typeName,
                paramName, equalToken, expression);
        parameters.add(serviceUrl);
        parameters.add(createToken(COMMA_TOKEN));

        QualifiedNameReferenceNode typeName1 = createQualifiedNameReferenceNode(
                        createIdentifierToken(GeneratorConstants.HTTP), createIdentifierToken(GeneratorConstants.COLON),
                        createIdentifierToken("ClientConfiguration"));

        IdentifierToken httpClientConfig = createIdentifierToken(" httpClientConfig");

        BasicLiteralNode expression1 = createBasicLiteralNode(null, createIdentifierToken(" {}"));

        DefaultableParameterNode clientConfig = createDefaultableParameterNode(annotationNodes, typeName1,
                httpClientConfig, equalToken, expression1);
        parameters.add(clientConfig);

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        //Create return type node for inti function
        IdentifierToken returnsKeyWord = createIdentifierToken(GeneratorConstants.RETURN);
        OptionalTypeDescriptorNode type = createOptionalTypeDescriptorNode(createErrorTypeDescriptorNode(
                        createIdentifierToken("error"), null), createIdentifierToken("?"));
        ReturnTypeDescriptorNode returnNode = createReturnTypeDescriptorNode(returnsKeyWord, annotationNodes, type);

        //Create function signature
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnNode);

        //Create function body node
        QualifiedNameReferenceNode typeBindingPattern = createQualifiedNameReferenceNode(
                        createIdentifierToken(GeneratorConstants.HTTP),
                        createIdentifierToken(GeneratorConstants.COLON),
                        createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        //Expression node
        Token newKeyWord = createIdentifierToken("new");
        Token openParenArg = createIdentifierToken(OPEN_PRAN);
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken("serviceUrl")));
        Token comma1 = createIdentifierToken(",");
        PositionalArgumentNode positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken("httpClientConfig")));

        argumentsList.add(positionalArgumentNode01);
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(newKeyWord,
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        VariableDeclarationNode variableDeclarationNode = createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer,
                createToken(SEMICOLON_TOKEN));
        //Assigment for client
        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken("clientEp")));

        SimpleNameReferenceNode expr = createSimpleNameReferenceNode(createIdentifierToken("httpEp"));
        AssignmentStatementNode assignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr, createToken(SEMICOLON_TOKEN));

        NodeList<StatementNode> statementList = createNodeList(variableDeclarationNode, assignmentStatementNode);

        FunctionBodyNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
        FunctionDefinitionNode initFunctionNode = createFunctionDefinitionNode(null, null,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(), functionSignatureNode
                , functionBodyNode);

        memberNodeList.add(initFunctionNode);
        // Generate remote function Nodes
        memberNodeList.addAll(createRemoteFunctions(paths, filters));
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        if (info.getExtensions() != null) {
            Map<String, Object> extensions = info.getExtensions();
            if (!extensions.isEmpty()) {
                for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                    if (extension.getKey().trim().equals("x-display")) {
                        metadataNode = getMetadataNodeForDisplayAnnotation(extension);
                    }
                }
            }
        }
        return createClassDefinitionNode(metadataNode, visibilityQualifier, classTypeQualifiers,
                classKeyWord, className, openBrace, createNodeList(memberNodeList),
                createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate Client class attributes.
     */
    private static ObjectFieldNode getClassField() {
        Token visibilityQualifierAttribute = createIdentifierToken(GeneratorConstants.PUBLIC);
        NodeList<Token> qualifierList = createEmptyNodeList();
        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(HTTP),
                        createToken(COLON_TOKEN), createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.CLIENT_EP);
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        return createObjectFieldNode(metadataNode, visibilityQualifierAttribute,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
    }

    /*
     * Generate remote function method name , when operation ID is not available for given operation.
     */
    private static Paths setOperationId(Paths paths) {
        Set<Map.Entry<String, PathItem>> entries = paths.entrySet();
        for (Map.Entry<String, PathItem> entry: entries) {
            PathItem pathItem = entry.getValue();
            int countMissId = 0;
            for (Operation operation : entry.getValue().readOperations()) {
                if (operation.getOperationId() == null) {
                    //simplify here with 1++
                    countMissId = countMissId + 1;
                } else {
                    String operationId = operation.getOperationId();
                    operation.setOperationId(Character.toLowerCase(operationId.charAt(0)) + operationId.substring(1));
                }
            }

            if (pathItem.getGet() != null) {
                Operation getOp = pathItem.getGet();
                if (getOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "get");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    getOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPut() != null) {
                Operation putOp = pathItem.getPut();
                if (putOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "put");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    putOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPost() != null) {
                Operation postOp = pathItem.getPost();
                if (postOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "post");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    postOp.setOperationId(operationId);
                }
            }
            if (pathItem.getDelete() != null) {
                Operation deleteOp = pathItem.getDelete();
                if (deleteOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "delete");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    deleteOp.setOperationId(operationId);
                }
            }
            if (pathItem.getOptions() != null) {
                Operation optionOp = pathItem.getOptions();
                if (optionOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "options");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    optionOp.setOperationId(operationId);
                }
            }
            if (pathItem.getHead() != null) {
                Operation headOp = pathItem.getHead();
                if (headOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "head");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    headOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPatch() != null) {
                Operation patchOp = pathItem.getPatch();
                if (patchOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "patch");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    patchOp.setOperationId(operationId);
                }
            }
            if (pathItem.getTrace() != null) {
                Operation traceOp = pathItem.getTrace();
                if (traceOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "trace");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    traceOp.setOperationId(operationId);
                }
            }
        }
        return paths;
    }

    private static String getOperationId(String[] split, String method) {
        String operationId;
        String regEx = "\\{([^}]*)\\}";
        Matcher matcher = Pattern.compile(regEx).matcher(split[split.length - 1]);
        if (matcher.matches()) {
            operationId = method + split[split.length - 2] + "By" + matcher.group(1);
        } else {
            operationId = method + split[split.length - 1];
        }
        return Character.toLowerCase(operationId.charAt(0)) + operationId.substring(1);
    }

    /*
     * Generate remote functions for OpenAPI operations.
     */
    private static List<FunctionDefinitionNode> createRemoteFunctions (Paths paths, Filter filter)
            throws BallerinaOpenApiException {
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
        Iterator<Map.Entry<String, PathItem>> pathItr = pathsItems.iterator();
        while (pathItr.hasNext()) {
            Map.Entry<String, PathItem> path = pathItr.next();
            if (!path.getValue().readOperationsMap().isEmpty()) {
                Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
                    //Add filter availability
                    //1.Tag filter
                    //2.Operation filter
                    //3.Both tag and operation filter
                    List<String> filterTags = filter.getTags();
                    List<String> operationTags = operation.getValue().getTags();
                    List<String> filterOperations  = filter.getOperations();
                    // Handle the display annotations
                    MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
                    Map<String, Object> extensions = operation.getValue().getExtensions();
                    if (extensions != null) {
                        for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                            if (extension.getKey().trim().equals("x-display")) {
                                metadataNode = getMetadataNodeForDisplayAnnotation(extension);
                            }
                        }
                    }

                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                        if (operationTags != null || ((!filterOperations.isEmpty())
                                && (operation.getValue().getOperationId() != null))) {
                            if (GeneratorUtils.hasTags(operationTags, filterTags) ||
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
     */
    private static FunctionDefinitionNode getFunctionDefinitionNode(MetadataNode metadataNode, String path,
                                                                    Map.Entry<PathItem.HttpMethod,
            Operation> operation) throws BallerinaOpenApiException {
        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createIdentifierToken("remote isolated"));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getValue().getOperationId());
        NodeList<Node> relativeResourcePath = createEmptyNodeList();

        FunctionSignatureNode functionSignatureNode = getFunctionSignatureNode(operation.getValue());

        // Create Function Body
        FunctionBodyNode functionBodyNode = getFunctionBodyNode(path, operation);

        FunctionDefinitionNode functionDefinitionNode = createFunctionDefinitionNode(null,
                metadataNode, qualifierList, functionKeyWord, functionName, relativeResourcePath,
                functionSignatureNode, functionBodyNode);

        return functionDefinitionNode;
    }

    private static FunctionSignatureNode getFunctionSignatureNode(Operation operation)
            throws BallerinaOpenApiException {
        // Create Parameters - function with parameters
        // Function RequestBody
        List<Node> parameterList =  new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        Token comma =  createToken(COMMA_TOKEN);
        setFunctionParameters(operation, parameterList, comma);
        if (parameterList.size() >= 2) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        //Create Return type - function with response
        Token returnsKeyWord = createToken(RETURNS_KEYWORD);
        //Type Always Union
        String returnType = getReturnType(operation);
        // Default
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(returnsKeyWord,
                annotationNodes, createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(returnType)));

        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameters, createToken(CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }

    /*
     * Generate function parameters.
     */
    private static void setFunctionParameters(Operation operation, List<Node> parameterList, Token comma)
            throws BallerinaOpenApiException {

        List<Parameter> parameters = operation.getParameters();
        if (parameters != null) {
            for (Parameter parameter: parameters) {
                String in = parameter.getIn();
                if (in.equals("path")) {
                    setPathParameters(parameterList, parameter);
                    parameterList.add(comma);
                } else if (in.equals("query")) {
                    setQueryParameters(parameterList, parameter);
                    parameterList.add(comma);
                } else if (in.equals("header")) {
                    setHeaderParameter(parameterList, parameter);
                    parameterList.add(comma);
                }
            }
        }
        //Handle RequestBody
        if (operation.getRequestBody() != null) {
            RequestBody requestBody = operation.getRequestBody();
            if (requestBody.getContent() != null) {
                setRequestBodyParameters(parameterList, requestBody);
                parameterList.add(comma);
            }
        }
    }

    /*
     * Create query parameters.
     */
    private static void setQueryParameters(List<Node> parameterList, Parameter parameter)
            throws BallerinaOpenApiException {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        TypeDescriptorNode typeName;
        if (parameter.getExtensions() != null) {
            for (Map.Entry<String, Object> extension: parameter.getExtensions().entrySet()) {
                if (extension.getKey().trim().equals("x-display")) {
                    AnnotationNode annotationNode = getAnnotationNode(extension);
                    annotationNodes = createNodeList(annotationNode);
                }
            }
        }

        Schema parameterSchema = parameter.getSchema();
        String paramType = convertOpenAPITypeToBallerina(parameterSchema.getType().trim());

        if (parameterSchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) parameterSchema;
            if (arraySchema.getItems().getType() != null) {
                String itemType = arraySchema.getItems().getType();
                if (itemType.equals("string") || itemType.equals("integer") || itemType.equals("boolean")
                        || itemType.equals("float") || itemType.equals("decimal")) {
                    paramType = convertOpenAPITypeToBallerina(itemType) + "[]";
                }
            } else if (arraySchema.getItems().get$ref() != null) {
                paramType = extractReferenceType(arraySchema.getItems().get$ref().trim()) + "[]";
            }
        }
        if (parameter.getRequired()) {
             typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(paramType));
        } else {
             typeName = createOptionalTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(paramType)), createToken(QUESTION_MARK_TOKEN));
        }
        IdentifierToken paramName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
        RequiredParameterNode queryParam = createRequiredParameterNode(annotationNodes, typeName, paramName);
        parameterList.add(queryParam);
    }

    /*
     * Create path parameters.
     */
    private static void setPathParameters(List<Node> parameterList, Parameter parameter) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        if (parameter.getExtensions() != null) {
            for (Map.Entry<String, Object> extension: parameter.getExtensions().entrySet()) {
                if (extension.getKey().trim().equals("x-display")) {
                    AnnotationNode annotationNode = getAnnotationNode(extension);
                    annotationNodes = createNodeList(annotationNode);
                }
            }
        }

        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim())));
        IdentifierToken paramName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
        RequiredParameterNode pathParam = createRequiredParameterNode(annotationNodes, typeName, paramName);
        parameterList.add(pathParam);
    }

    /*
     * Create header parameter.
     */
    private static void setHeaderParameter(List<Node> parameterList, Parameter parameter)
            throws BallerinaOpenApiException {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        if (parameter.getExtensions() != null) {
            for (Map.Entry<String, Object> extension: parameter.getExtensions().entrySet()) {
                if (extension.getKey().trim().equals("x-display")) {
                    AnnotationNode annotationNode = getAnnotationNode(extension);
                    annotationNodes = createNodeList(annotationNode);
                }
            }
        }
        if (parameter.getRequired()) {
            String type = convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim());
            Schema schema = parameter.getSchema();
            if (schema instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) schema;
                if (arraySchema.getItems().get$ref() != null) {
                    type = extractReferenceType(arraySchema.getItems().get$ref()) + "[]";
                } else {
                    type = convertOpenAPITypeToBallerina(arraySchema.getItems().getType().trim()) + "[]";
                }
            }
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(type));
            IdentifierToken paramName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
            RequiredParameterNode pathParam = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameterList.add(pathParam);
        } else {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(convertOpenAPITypeToBallerina(
                            parameter.getSchema().getType().trim()) + "?"));
            IdentifierToken paramName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
            RequiredParameterNode pathParam = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameterList.add(pathParam);
        }
    }

    /*
     * Create request body parameter.
     */
    private static void setRequestBodyParameters(List<Node> parameterList, RequestBody requestBody)
            throws BallerinaOpenApiException {

        Content content = requestBody.getContent();
        Iterator<Map.Entry<String, MediaType>> iterator = content.entrySet().iterator();
        while (iterator.hasNext()) {
            // This implementation currently for first content type
            Map.Entry<String, MediaType> next = iterator.next();
            Schema schema = next.getValue().getSchema();
            String paramType;
            //Take payload type
            if (schema.get$ref() != null) {
                paramType = extractReferenceType(schema.get$ref().trim());
            } else if (schema.getType() != null) {
                String typeOfPayload = schema.getType().trim();
                paramType = convertOpenAPITypeToBallerina(typeOfPayload);
            } else if (schema instanceof ArraySchema) {
                //ToDo: handle nested array
                ArraySchema arraySchema = (ArraySchema) schema;
                if (arraySchema.getItems().getType() != null) {
                    paramType = convertOpenAPITypeToBallerina(arraySchema.getItems().getType()) + "[]";
                } else if (arraySchema.getItems().get$ref() != null) {
                    paramType = extractReferenceType(arraySchema.getItems().get$ref()) + "[]";
                } else {
                    paramType = getBallerinaMeidaType(next.getKey().trim()) + "[]";
                }
            } else {
                paramType = getBallerinaMeidaType(next.getKey());
            }
            NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
            if (requestBody.getExtensions() != null) {
                for (Map.Entry<String, Object> extension: requestBody.getExtensions().entrySet()) {
                    if (extension.getKey().trim().equals("x-display")) {
                        AnnotationNode annotationNode = getAnnotationNode(extension);
                        annotationNodes = createNodeList(annotationNode);
                    }
                }
            }
            SimpleNameReferenceNode typeName = createSimpleNameReferenceNode(createIdentifierToken(paramType));
            IdentifierToken paramName = createIdentifierToken("payload");
            RequiredParameterNode payload = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameterList.add(payload);
            break;
        }
    }

    /*
     * Create request body parameter.
     */
    private static String getReturnType(Operation operation) throws BallerinaOpenApiException {
        String returnType = "http:Response | error";
        if (operation.getResponses() != null) {
            ApiResponses responses = operation.getResponses();
            Collection<ApiResponse> values = responses.values();
            Iterator<ApiResponse> iteratorRes = values.iterator();
            while (iteratorRes.hasNext()) {
                ApiResponse response = iteratorRes.next();
                if (response.getContent() != null) {
                    Content content = response.getContent();
                    Set<Map.Entry<String, MediaType>> mediaTypes = content.entrySet();
                    Iterator<Map.Entry<String, MediaType>> iteratorMedia = mediaTypes.iterator();
                    while (iteratorMedia.hasNext()) {
                        Map.Entry<String, MediaType> media = iteratorMedia.next();
                        String type;
                        if (media.getValue().getSchema() != null) {
                            Schema schema = media.getValue().getSchema();
                            if (schema.get$ref() != null) {
                                type = extractReferenceType(schema.get$ref());
                            } else if (schema instanceof ArraySchema) {
                                ArraySchema arraySchema = (ArraySchema) schema;
                                // TODO: Nested array when response has
                                if (arraySchema.getItems().get$ref() != null) {
                                    type = extractReferenceType(arraySchema.getItems().get$ref()) + "[]";
                                    String typeName = extractReferenceType(arraySchema.getItems().get$ref()) + "Arr";
                                    TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null, null,
                                            createIdentifierToken("type"),
                                            createIdentifierToken(typeName),
                                            createSimpleNameReferenceNode(createIdentifierToken(type)),
                                            createToken(SEMICOLON_TOKEN));
                                    // need to check already typedecripor has same name
                                    if (!typeDefinitionNodeList.isEmpty()) {
                                        boolean isExit = false;
                                        for (TypeDefinitionNode typeNode: typeDefinitionNodeList) {
                                            if (typeNode.typeName().toString().trim().equals(typeName)) {
                                                isExit = true;
                                            }
                                        }
                                        if (!isExit) {
                                            typeDefinitionNodeList.add(typeDefNode);
                                        }
                                    } else {
                                        typeDefinitionNodeList.add(typeDefNode);
                                    }
                                    type = typeName;
                                } else {
                                    String typeName = convertOpenAPITypeToBallerina(arraySchema.getItems().getType()) +
                                            "Arr";
                                    type = convertOpenAPITypeToBallerina(arraySchema.getItems().getType()) + "[]";
                                    TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null,
                                            null, createIdentifierToken("type"),
                                            createIdentifierToken(typeName),
                                            createSimpleNameReferenceNode(createIdentifierToken(type)),
                                            createToken(SEMICOLON_TOKEN));
                                    typeDefinitionNodeList.add(typeDefNode);
                                    if (!typeDefinitionNodeList.isEmpty()) {
                                        boolean isExit = false;
                                        for (TypeDefinitionNode typeNode: typeDefinitionNodeList) {
                                            if (typeNode.typeName().toString().trim().equals(typeName)) {
                                                isExit = true;
                                            }
                                        }
                                        if (!isExit) {
                                            typeDefinitionNodeList.add(typeDefNode);
                                        }
                                    } else {
                                        typeDefinitionNodeList.add(typeDefNode);
                                    }
                                    type = typeName;
                                }
                            } else if (schema.getType() != null) {
                                type = convertOpenAPITypeToBallerina(schema.getType());
                            } else {
                                type = getBallerinaMeidaType(media.getKey().trim());
                            }
                        } else {
                            type = getMediaType(media.getKey().trim());
                        }

                        StringBuilder builder = new StringBuilder();
                        builder.append(type);
                        builder.append("|");
                        builder.append("error");
                        returnType = builder.toString();
                        // Currently support for first media type
                        break;
                    }
                }
                // Currently support for first response.
                break;
            }
        }
        return returnType;
    }

    /**
     * Generate function body node.
     * @param path - remote function path
     * @param operation - opneapi operation
     * @return - function body node
     * @throws BallerinaOpenApiException
     */
    private static FunctionBodyNode getFunctionBodyNode(String path,
                                                        Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        boolean isHeader = false;
        // Create statements
        List<StatementNode> statementsList =  new ArrayList<>();
        // -- create variable declaration
        //string path - common for every remote functions
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(createSimpleNameReferenceNode(
                        createIdentifierToken("string ")), createCaptureBindingPatternNode(
                                createIdentifierToken("path")));
        Token equalToken = createToken(EQUAL_TOKEN);
        // Create initializer
        Token type = createToken(STRING_KEYWORD);
        Token startBacktick = createToken(BACKTICK_TOKEN);
        //content  should decide with /pet and /pet/{pet}
        if (path.contains("{")) {
            path = path.replaceAll("[{]", "\\${");
        }
        //String path generator
        NodeList<Node> content = createNodeList(createLiteralValueToken(null, path, createEmptyMinutiaeList(),
                createEmptyMinutiaeList()));
        Token endBacktick = createToken(BACKTICK_TOKEN);
        TemplateExpressionNode initializer = createTemplateExpressionNode(null, type, startBacktick, content,
                endBacktick);
        Token semicolon = createToken(SEMICOLON_TOKEN);
        VariableDeclarationNode pathInt = createVariableDeclarationNode(annotationNodes, null,
                typedBindingPatternNode, equalToken, initializer, semicolon);

        statementsList.add(pathInt);

        //Handel query parameter map
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            List<Parameter> queryParameters = new ArrayList<>();
            List<Parameter> headerParameters = new ArrayList<>();

            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals("query")) {
                    queryParameters.add(parameter);
                } else if (parameter.getIn().trim().equals("header")) {
                    headerParameters.add(parameter);
                }
            }
            if (!queryParameters.isEmpty()) {
                statementsList.add(getMapForParameters(queryParameters, "map<anydata>", "queryParam"));
                // Add updated path
                ExpressionStatementNode updatedPath = getSimpleExpressionStatementNode("path = path + " +
                        "getPathForQueryParam(queryParam)");
                statementsList.add(updatedPath);
                isQuery = true;
            }
            if (!headerParameters.isEmpty()) {
                statementsList.add(getMapForParameters(headerParameters, "map<string|string[]>",
                        "accHeaders"));
                isHeader = true;
            }
        }
        //Statement Generator for requestBody
        String method = operation.getKey().name().trim().toLowerCase(Locale.ENGLISH);
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            if (requestBody.getContent() != null) {
                Content rbContent = requestBody.getContent();
                Set<Map.Entry<String, MediaType>> entries = rbContent.entrySet();
                Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
                //currently align with first content
                while (iterator.hasNext()) {
                    //Create Request statement
                    Map.Entry<String, MediaType> next = iterator.next();
                    VariableDeclarationNode requestVariable = getSimpleStatement("http:Request",
                            "request", "new");
                    statementsList.add(requestVariable);
                    if (next.getValue().getSchema() != null) {
                        if (next.getKey().contains("json")) {
                            VariableDeclarationNode jsonVariable = getSimpleStatement("json",
                                    "jsonBody", "check payload.cloneWithType(json)");
                            statementsList.add(jsonVariable);
                            ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                                    "request.setPayload(jsonBody)");
                            statementsList.add(expressionStatementNode);
                        } else if (next.getKey().contains("xml")) {
                            ImportDeclarationNode xmlImport = GeneratorUtils.getImportDeclarationNode(
                                    GeneratorConstants.BALLERINA, "xmldata");
                            imports.add(xmlImport);
                            VariableDeclarationNode jsonVariable = getSimpleStatement("json",
                                    "jsonBody", "check payload.cloneWithType(json)");
                            statementsList.add(jsonVariable);
                            VariableDeclarationNode xmlBody = getSimpleStatement("xml?", "xmlBody",
                                    "check xmldata:fromJson(jsonBody)");
                            statementsList.add(xmlBody);
                            ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                                    "request.setPayload(xmlBody)");
                            statementsList.add(expressionStatementNode);
                        } else if (next.getKey().contains("plain")) {
                            ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                                    "request.setPayload(payload)");
                            statementsList.add(expressionStatementNode);
                        }
                        // TODO:Fill with other mime type
                    } else {
                        // Add default value comment
                        ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                                "TODO: Update the request as needed");
                        statementsList.add(expressionStatementNode);
                    }
//                    if (operation.getValue().getResponses() != null) {
                        // POST, PUT, PATCH, DELETE, EXECUTE
                        VariableDeclarationNode requestStatement =
                                getSimpleStatement(getReturnType(operation.getValue()).split("\\|")[0],
                                "response", "check self.clientEp->" + method + "(path, request)");
//                    }

                    if (isHeader) {
                        if (method.equals("post") || method.equals("put") || method.equals("patch") || method.equals(
                                "delete") || method.equals("execute")) {
                            requestStatement = getSimpleStatement("http:Response",
                                    "response", "check self.clientEp->"
                                            + method + "(path, request, headers = accHeaders)");

                        }
                    }

                    statementsList.add(requestStatement);
                    Token returnKeyWord = createIdentifierToken("return");
                    SimpleNameReferenceNode returnType = createSimpleNameReferenceNode(
                            createIdentifierToken("response"));
                    ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returnType,
                            semicolon);
                    statementsList.add(returnStatementNode);
                    break;
                }
            }
        } else {
            String responseType = "http:Response";
            //Need to focus check expression node with initializer
            Token check = createToken(CHECK_KEYWORD);

            FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken("clientEp")));

            Token rightArrowToken = createToken(RIGHT_ARROW_TOKEN);
            //Method name
            SimpleNameReferenceNode methodName = createSimpleNameReferenceNode(createIdentifierToken(method));
            //Argument Nodes
            List<Node> argNodes = new ArrayList<>();
            PositionalArgumentNode positionalArgumentNode = createPositionalArgumentNode(
                    createSimpleNameReferenceNode(createIdentifierToken("path")));

            if (isHeader) {
                if (method.equals("post") || method.equals("put") || method.equals("patch") || method.equals(
                        "delete") || method.equals("execute")) {
                    ExpressionStatementNode requestStatementNode = getSimpleExpressionStatementNode(
                            "http:Request request = new");
                    statementsList.add(requestStatementNode);
                    ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                            "//TODO: Update the request as needed");
                    statementsList.add(expressionStatementNode);

                    positionalArgumentNode = createPositionalArgumentNode(createSimpleNameReferenceNode(
                            createIdentifierToken("path, request, headers = accHeaders")));
                } else {
                    positionalArgumentNode = createPositionalArgumentNode(
                            createSimpleNameReferenceNode(createIdentifierToken("path, accHeaders")));
                }
            } else if (method.equals("post") || method.equals("put") || method.equals("patch") || method.equals(
                    "delete") || method.equals("execute")) {
                ExpressionStatementNode requestStatementNode = getSimpleExpressionStatementNode(
                        "http:Request request = new");
                statementsList.add(requestStatementNode);
                ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                        "//TODO: Update the request as needed");
                statementsList.add(expressionStatementNode);

                positionalArgumentNode = createPositionalArgumentNode(
                        createSimpleNameReferenceNode(createIdentifierToken("path, request")));
            }

            argNodes.add(positionalArgumentNode);
            argNodes.add(createToken(COMMA_TOKEN));

            SimpleNameReferenceNode argNode = createSimpleNameReferenceNode(createIdentifierToken("targetType"));
            String returnType = getReturnType(operation.getValue()).split("\\|")[0];
            responseType = returnType;
            SimpleNameReferenceNode expression = createSimpleNameReferenceNode(createIdentifierToken(returnType));

            NamedArgumentNode targetTypeNode = createNamedArgumentNode(argNode, equalToken, expression);
            argNodes.add(targetTypeNode);

            //Create typeBind for return
            SimpleNameReferenceNode resTypeBind = createSimpleNameReferenceNode(createIdentifierToken(responseType));
            CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken(
                    "response"));
            TypedBindingPatternNode resTypedBindingPatternNode = createTypedBindingPatternNode(resTypeBind,
                    bindingPattern);


            SeparatedNodeList<FunctionArgumentNode> argumentNodeList = createSeparatedNodeList(argNodes);
            RemoteMethodCallActionNode actionNode = createRemoteMethodCallActionNode(fieldExpr, rightArrowToken,
                    methodName, createToken(OPEN_PAREN_TOKEN), argumentNodeList, createToken(CLOSE_PAREN_TOKEN));
            CheckExpressionNode initRes = createCheckExpressionNode(null, check, actionNode);
            VariableDeclarationNode checkVariable = createVariableDeclarationNode(annotationNodes, null,
                    resTypedBindingPatternNode, equalToken, initRes, semicolon);
            statementsList.add(checkVariable);
            //Return Variable
            Token returnKeyWord = createIdentifierToken("return");
            SimpleNameReferenceNode returns = createSimpleNameReferenceNode(createIdentifierToken("response"));
            ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returns, semicolon);
            statementsList.add(returnStatementNode);
        }
        //Create statements
        NodeList<StatementNode> statements = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null, statements,
                createToken(CLOSE_BRACE_TOKEN));
    }

/*
 * Generate variableDeclarationNode.
 */
    private static  VariableDeclarationNode getSimpleStatement(String responseType, String variable,
                                                               String initializer) {
        SimpleNameReferenceNode resTypeBind = createSimpleNameReferenceNode(createIdentifierToken(responseType));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken(variable));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(resTypeBind, bindingPattern);
        SimpleNameReferenceNode init = createSimpleNameReferenceNode(createIdentifierToken(initializer));

        return createVariableDeclarationNode(createEmptyNodeList(), null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), init, createToken(SEMICOLON_TOKEN));
    }

    private static VariableDeclarationNode getMapForParameters(List<Parameter> parameters, String mapDataType,
                                                            String mapName) {
        List<Node> filedOfMap = new ArrayList();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(mapDataType));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(mapName));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);

        for (Parameter parameter: parameters) {
            // Initializer
            IdentifierToken fieldName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
            Token colon = createToken(COLON_TOKEN);
            SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(
                    createIdentifierToken(escapeIdentifier(parameter.getName().trim())));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    fieldName, colon, valueExpr);
            filedOfMap.add(specificFieldNode);
            filedOfMap.add(createToken(COMMA_TOKEN));
        }

        filedOfMap.remove(filedOfMap.size() - 1);
        MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(filedOfMap),
                createToken(CLOSE_BRACE_TOKEN));
        return createVariableDeclarationNode(createEmptyNodeList(),
                null, bindingPatternNode, createToken(EQUAL_TOKEN), initialize,
                createToken(SEMICOLON_TOKEN));
    }

/*
 * Generate expressionStatementNode.
 */
    private static ExpressionStatementNode getSimpleExpressionStatementNode(String expression) {
        SimpleNameReferenceNode expressionNode = createSimpleNameReferenceNode(
                createIdentifierToken(expression));
        return createExpressionStatementNode(null, expressionNode, createToken(SEMICOLON_TOKEN));
    }

/*
 * Filter the mediaType.
 */
    private static String getMediaType(String media) {
        switch (media) {
            case "application/json":
                return SyntaxKind.JSON_KEYWORD.toString();
            case "application/xml":
                return SyntaxKind.XML_KEYWORD.toString();
            case "text/plain":
                return STRING_KEYWORD.toString();
            case "application/octet-stream":
                return SyntaxKind.BYTE_ARRAY_LITERAL.toString();
            default:
                return SyntaxKind.JSON_KEYWORD.toString();
            // TODO: fill other types
        }
    }

    // Create queryPath param function
    private static FunctionDefinitionNode getQueryParamPath() {
        Token functionKeyWord = createIdentifierToken("isolated function");
        IdentifierToken functionName = createIdentifierToken(" getPathForQueryParam");
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                        createSeparatedNodeList(createRequiredParameterNode(createEmptyNodeList(),
                                createIdentifierToken("map<anydata> "),
                                createIdentifierToken(" queryParam"))),
                        createToken(CLOSE_PAREN_TOKEN),
                        createReturnTypeDescriptorNode(createIdentifierToken(" returns "),
                                createEmptyNodeList(), createBuiltinSimpleNameReferenceNode(
                                        null, createIdentifierToken("string"))));

        // FunctionBody
        List<StatementNode> statementNodes = new ArrayList<>();
        VariableDeclarationNode variable = getSimpleStatement("string[]", "param", "[]");
        statementNodes.add(variable);
        ExpressionStatementNode assign = getSimpleExpressionStatementNode("param[param.length()] = \"?\"");
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
        ExpressionStatementNode assignStatement = getSimpleExpressionStatementNode("_ = queryParam.remove(key)");
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
        ExpressionStatementNode ifBody02Statement = getSimpleExpressionStatementNode(" param[param.length()] = " +
                "string:substring(key, 1, key.length())");

        NodeList<StatementNode> statementNodesForIf02 = createNodeList(ifBody02Statement);
        BlockStatementNode ifBlock02 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        statementNodesForIf02, createToken(CLOSE_BRACE_TOKEN));

        // else block-02
        // else body 02

        ExpressionStatementNode elseBody02Statement = getSimpleExpressionStatementNode("param[param.length()] = key");
        NodeList<StatementNode> statementNodesForElse02 = createNodeList(elseBody02Statement);
        BlockStatementNode elseBlockNode02 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        statementNodesForElse02, createToken(CLOSE_BRACE_TOKEN));

        ElseBlockNode elseBlock02 = createElseBlockNode(createToken(ELSE_KEYWORD), elseBlockNode02);
        IfElseStatementNode ifElseStatementNode02 = createIfElseStatementNode(ifKeyWord, condition, ifBlock02,
                elseBlock02);
        statements.add(ifElseStatementNode02);

        ExpressionStatementNode assignment = getSimpleExpressionStatementNode("param[param.length()] = \"=\"");
        statements.add(assignment);

        //If block 03
        SimpleNameReferenceNode exprIf03 = createSimpleNameReferenceNode(createIdentifierToken(" value "));
        BuiltinSimpleNameReferenceNode typeCondition03 = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(" string"));
        TypeTestExpressionNode condition03 = createTypeTestExpressionNode(exprIf03, isKeyWord, typeCondition03);

        ExpressionStatementNode variableIf03 = getSimpleExpressionStatementNode("string updateV =  checkpanic " +
                "url:encode(value, \"UTF-8\")");
        ExpressionStatementNode assignIf03 = getSimpleExpressionStatementNode("param[param.length()] = updateV");

        BlockStatementNode ifBody03 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                createNodeList(variableIf03, assignIf03), createToken(CLOSE_BRACE_TOKEN));
        BlockStatementNode elseBodyBlock03 = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), createNodeList(
                        getSimpleExpressionStatementNode("param[param.length()] = value.toString()")),
                createToken(CLOSE_BRACE_TOKEN));
        ElseBlockNode elseBody03 = createElseBlockNode(elseKeyWord, elseBodyBlock03);
        IfElseStatementNode ifElse03 = createIfElseStatementNode(ifKeyWord, condition03, ifBody03, elseBody03);

        statements.add(ifElse03);

        ExpressionStatementNode andStatement = getSimpleExpressionStatementNode("param[param.length()] = \"&\"");
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
        ExpressionStatementNode assignLine02 = getSimpleExpressionStatementNode("_ = param.remove(param.length()-1)");
        statementNodes.add(assignLine02);

        //IfElseStatement
        SimpleNameReferenceNode lhs = createSimpleNameReferenceNode(createIdentifierToken(" param.length()"));
        Token equalToken = createIdentifierToken("==");
        BuiltinSimpleNameReferenceNode rhs = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(" 1"));
        TypeTestExpressionNode conditionForIfElse = createTypeTestExpressionNode(lhs, equalToken, rhs);
        //if body block

        ExpressionStatementNode newAssign = getSimpleExpressionStatementNode("_ = param.remove(0)");
        BlockStatementNode ifBlock = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        createNodeList(newAssign), createToken(CLOSE_BRACE_TOKEN));
        IfElseStatementNode ifElseStatementNode = createIfElseStatementNode(ifKeyWord, conditionForIfElse, ifBlock,
                null);
        statementNodes.add(ifElseStatementNode);

        statementNodes.add(getSimpleExpressionStatementNode("string restOfPath = string:'join(\"\", ...param)"));
        statementNodes.add(getSimpleExpressionStatementNode("return restOfPath"));
        FunctionBodyNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, createNodeList(statementNodes), createToken(CLOSE_BRACE_TOKEN));

        return createFunctionDefinitionNode(FUNCTION_DEFINITION, null,
                createEmptyNodeList(), functionKeyWord, functionName, createEmptyNodeList(),
                functionSignatureNode, functionBodyNode);
    }

    /*
     * Generate metaDataNode with display annotation.
     */
    private static MetadataNode getMetadataNodeForDisplayAnnotation(Map.Entry<String, Object> extension) {

        MetadataNode metadataNode;
        AnnotationNode annotationNode = getAnnotationNode(extension);
        metadataNode = createMetadataNode(null, createNodeList(annotationNode));
        return metadataNode;
    }

    private static AnnotationNode getAnnotationNode(Map.Entry<String, Object> extension) {

        LinkedHashMap<String, String> extFields = (LinkedHashMap<String, String>) extension.getValue();
        List<Node> annotFields = new ArrayList<>();
        if (!extFields.isEmpty()) {
            for (Map.Entry<String, String> field: extFields.entrySet()) {

                BasicLiteralNode valueExpr = createBasicLiteralNode(STRING_LITERAL,
                        createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                                '"' + field.getValue().trim() + '"',
                                createEmptyMinutiaeList(),
                                createEmptyMinutiaeList()));
                SpecificFieldNode fields = createSpecificFieldNode(null,
                        createIdentifierToken(field.getKey().trim()),
                        createToken(COLON_TOKEN), valueExpr);
                annotFields.add(fields);
                annotFields.add(createToken(COMMA_TOKEN));
            }
            if (annotFields.size() == 2) {
                annotFields.remove(1);
            }
        }

        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken("display"));

        return createAnnotationNode(createToken(SyntaxKind.AT_TOKEN)
                , annotateReference, annotValue);
    }
}
