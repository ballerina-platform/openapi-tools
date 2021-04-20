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

package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
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
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
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
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PLUS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RIGHT_ARROW_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static org.ballerinalang.generators.GeneratorUtils.buildUrl;
import static org.ballerinalang.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static org.ballerinalang.generators.GeneratorUtils.escapeIdentifier;
import static org.ballerinalang.generators.GeneratorUtils.extractReferenceType;
import static org.ballerinalang.generators.GeneratorUtils.getBallerinaOpenApiType;

/**
 * This Util class use for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    private static Server server;
    private static Paths paths;
    private static Filter filters;
    private static List<ImportDeclarationNode> imports = new ArrayList<>();
    private static boolean isQuery = false;

    public static SyntaxTree generateSyntaxTree(Path definitionPath, Filter filter)
            throws IOException, BallerinaOpenApiException, FormatterException {
        imports.clear();
        // Summaries OpenAPI details
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        //Filter serverUrl
        List<Server> servers = openAPI.getServers();
        server = servers.get(0);

        paths = openAPI.getPaths();

        paths = setOperationId(paths);
        filters = filter;
        // 1. Load client template syntax tree
        SyntaxTree syntaxTree = null;
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        ClassDefinitionNode classDefinitionNode = getClassDefinitionNode();
        ModulePartNode modulePartNode;
        if (isQuery) {
            ImportDeclarationNode url = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, " url");
            ImportDeclarationNode string = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, "lang.'string");
            imports.add(url);
            imports.add(string);
            NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
            FunctionDefinitionNode queryParamFunction = getQueryParamPath();
            modulePartNode = NodeFactory.createModulePartNode(importsList,
                    AbstractNodeFactory.createNodeList(queryParamFunction),
                    createToken(EOF_TOKEN));
        } else {
            NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
            modulePartNode = NodeFactory.createModulePartNode(importsList,
                    AbstractNodeFactory.createNodeList(classDefinitionNode), createToken(EOF_TOKEN));
        }
        TextDocument textDocument = TextDocuments.from("");
        syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = syntaxTree.modifyWith(modulePartNode);
        System.out.println(Formatter.format(syntaxTree));
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
        NodeList<Token> classTypeQualifiers = NodeFactory.createNodeList(clientKeyWord);

        IdentifierToken classKeyWord = createIdentifierToken(GeneratorConstants.CLASS);
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        Token openBrace = createIdentifierToken(GeneratorConstants.OPEN_BRACE);
        //Fill the members
        List<Node> memberNodeList =  new ArrayList<>();
        //Create class field
        ObjectFieldNode fieldNode = getClassField();
        memberNodeList.add(fieldNode);
        //Create init function definition
        //Common Used
        NodeList<Token> qualifierList =
                NodeFactory.createNodeList(createIdentifierToken(GeneratorConstants.PUBLIC));
        IdentifierToken functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION);
        IdentifierToken functionName = createIdentifierToken("init");
        //Create function signature
        IdentifierToken openParen = createIdentifierToken(GeneratorConstants.OPEN_PRAN);
        //Add parameters
        List<Node> parameters  = new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode typeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("string"));
        IdentifierToken paramName = createIdentifierToken("serviceUrl");
        IdentifierToken equalToken = createIdentifierToken("=");
        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(STRING_LITERAL,
                createIdentifierToken('"' + getServerURL(server) + '"'));

        DefaultableParameterNode serviceUrl = NodeFactory.createDefaultableParameterNode(annotationNodes, typeName,
                paramName, equalToken, expression);
        IdentifierToken comma = createIdentifierToken(",");
        parameters.add(serviceUrl);
        parameters.add(comma);

        QualifiedNameReferenceNode typeName1 =
                NodeFactory.createQualifiedNameReferenceNode(
                        createIdentifierToken(GeneratorConstants.HTTP),
                        createIdentifierToken(GeneratorConstants.COLON),
                        createIdentifierToken("ClientConfiguration"));
        IdentifierToken paramName1 = createIdentifierToken(" httpClientConfig");
        BasicLiteralNode expression1 = NodeFactory.createBasicLiteralNode(null,
                createIdentifierToken(" {}"));

        DefaultableParameterNode clientConfig = NodeFactory.createDefaultableParameterNode(annotationNodes, typeName1
                , paramName1, equalToken, expression1);
        parameters.add(clientConfig);

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);
        IdentifierToken closeParan = createIdentifierToken(GeneratorConstants.CLOSE_PRAN);

        //Return type
        IdentifierToken returnsKeyWord = createIdentifierToken(GeneratorConstants.RETURN);
        OptionalTypeDescriptorNode type =
                NodeFactory.createOptionalTypeDescriptorNode(NodeFactory.createErrorTypeDescriptorNode(
                        createIdentifierToken("error"), null),
                        createIdentifierToken("?"));
        ReturnTypeDescriptorNode returnNode = NodeFactory.createReturnTypeDescriptorNode(returnsKeyWord,
                annotationNodes, type);

        //Create function signature
        FunctionSignatureNode functionSignatureNode = NodeFactory.createFunctionSignatureNode(openParen,
                parameterList, closeParan, returnNode);

        //Create function body
        Token openBraceFB = createIdentifierToken(GeneratorConstants.OPEN_BRACE);
        //Create Statement

        // Variable Declaration Node
        // Create type Binding
        QualifiedNameReferenceNode typeBindingPattern = NodeFactory.createQualifiedNameReferenceNode(
                        createIdentifierToken(GeneratorConstants.HTTP),
                        createIdentifierToken(GeneratorConstants.COLON),
                        createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        CaptureBindingPatternNode bindingPattern =
                NodeFactory.createCaptureBindingPatternNode(createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode =
                NodeFactory.createTypedBindingPatternNode(typeBindingPattern, bindingPattern);

        //Expression node
        Token newKeyWord = createIdentifierToken("new");
        Token openParenArg = createIdentifierToken(GeneratorConstants.OPEN_PRAN);
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("serviceUrl")));
        Token comma1 = createIdentifierToken(",");
        PositionalArgumentNode positionalArgumentNode02 =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("httpClientConfig")));

        argumentsList.add(positionalArgumentNode01);
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = NodeFactory.createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = NodeFactory.createImplicitNewExpressionNode(newKeyWord,
                parenthesizedArgList);
        CheckExpressionNode initializer = NodeFactory.createCheckExpressionNode(null,
                createToken(CHECK_KEYWORD), expressionNode);
        VariableDeclarationNode variableDeclarationNode = NodeFactory.createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN),
                initializer, createToken(SEMICOLON_TOKEN));
        //Assigment
        FieldAccessExpressionNode varRef =
                NodeFactory.createFieldAccessExpressionNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("self")),
                        createToken(DOT_TOKEN),
                        NodeFactory.createSimpleNameReferenceNode(
                                createIdentifierToken("clientEp")));
        SimpleNameReferenceNode expr =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("httpEp"));
        AssignmentStatementNode assignmentStatementNode = NodeFactory.createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), expr,
                createToken(SEMICOLON_TOKEN));

        NodeList<StatementNode> statementList = NodeFactory.createNodeList(variableDeclarationNode,
                assignmentStatementNode);

        Token closeBraceFB = createToken(CLOSE_BRACE_TOKEN);
        FunctionBodyNode functionBodyNode = NodeFactory.createFunctionBodyBlockNode(openBraceFB,
                null, statementList, closeBraceFB);
        FunctionDefinitionNode initFunctionNode = NodeFactory.createFunctionDefinitionNode(null, null,
                qualifierList, functionKeyWord, functionName, NodeFactory.createEmptyNodeList(), functionSignatureNode
                , functionBodyNode);

        memberNodeList.add(initFunctionNode);
        memberNodeList.addAll(createRemoteFunctions(paths, filters));

        return NodeFactory.createClassDefinitionNode(null, visibilityQualifier,
                classTypeQualifiers, classKeyWord, className, openBrace, NodeFactory.createNodeList(memberNodeList),
                createToken(CLOSE_BRACE_TOKEN));
    }

    private static ObjectFieldNode getClassField() {

        Token visibilityQualifierAttribute = createIdentifierToken(GeneratorConstants.PUBLIC);
        NodeList<Token> qualifierList = createEmptyNodeList();
        QualifiedNameReferenceNode typeName =
                NodeFactory.createQualifiedNameReferenceNode(createIdentifierToken(
                        GeneratorConstants.HTTP),
                        createIdentifierToken(GeneratorConstants.COLON),
                        createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.CLIENT_EP);
        return NodeFactory.createObjectFieldNode(null, visibilityQualifierAttribute,
                qualifierList, typeName, fieldName, null, null,
                createIdentifierToken(GeneratorConstants.SEMICOLON));
    }

    private static Paths setOperationId(Paths paths) {

        Set<Map.Entry<String, PathItem>> entries = paths.entrySet();
        for (Map.Entry<String, PathItem> entry: entries) {
            PathItem pathItem = entry.getValue();
            int countMissId = 0;
            for (Operation operation : entry.getValue().readOperations()) {
                if (operation.getOperationId() == null) {
                    //simplify here with 1++
                    countMissId = countMissId + 1;
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
        return operationId;
    }

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
                    //3. Both tag and operation filter
                    List<String> filterTags = filter.getTags();
                    List<String> operationTags = operation.getValue().getTags();
                    List<String> filterOperations  = filter.getOperations();
                    if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                        if (operationTags != null || ((!filterOperations.isEmpty())
                                && (operation.getValue().getOperationId() != null))) {
                            if (GeneratorUtils.hasTags(operationTags, filterTags) ||
                                    ((operation.getValue().getOperationId() != null) &&
                                            filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                                // function call
                                FunctionDefinitionNode functionDefinitionNode = getFunctionDefinitionNode(path.getKey()
                                        , operation);
                                functionDefinitionNodeList.add(functionDefinitionNode);
                            }
                        }
                    } else {
                        // function call
                        FunctionDefinitionNode functionDefinitionNode = getFunctionDefinitionNode(path.getKey(),
                                operation);
                        functionDefinitionNodeList.add(functionDefinitionNode);
                    }
                }
            }
        }
        return functionDefinitionNodeList;
    }

    private static FunctionDefinitionNode getFunctionDefinitionNode(String path, Map.Entry<PathItem.HttpMethod,
            Operation> operation) throws BallerinaOpenApiException {
        //Create qualifier list
        NodeList<Token> qualifierList =
                createNodeList(createToken(REMOTE_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getValue().getOperationId());
        NodeList<Node> relativeResourcePath = NodeFactory.createEmptyNodeList();

        FunctionSignatureNode functionSignatureNode = getFunctionSignatureNode(operation.getValue());

        // Create Function Body
        FunctionBodyNode functionBodyNode = getFunctionBodyNode(path, operation);

        FunctionDefinitionNode functionDefinitionNode = NodeFactory.createFunctionDefinitionNode(null,
                null, qualifierList, functionKeyWord, functionName, relativeResourcePath,
                functionSignatureNode, functionBodyNode);

        return functionDefinitionNode;
    }

    private static FunctionSignatureNode getFunctionSignatureNode(Operation operation)
            throws BallerinaOpenApiException {

        // Create Function Signature
        Token openParen = createToken(OPEN_PAREN_TOKEN);
        // Create Parameters - function with parameters
        // Function RequestBody
        List<Node> parameterList =  new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
        Token comma =  NodeFactory.createToken(COMMA_TOKEN);
        setFunctionParameters(operation, parameterList, comma);
        if (parameterList.size() >= 2) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        Token closeParen = createToken(CLOSE_PAREN_TOKEN);
        //Create Return type - function with response
        Token returnsKeyWord = createToken(RETURNS_KEYWORD);
        //Type Always Union
        String returnType = getReturnType(operation);
        // Default
        ReturnTypeDescriptorNode returnTypeDescriptorNode = NodeFactory.createReturnTypeDescriptorNode(returnsKeyWord,
                annotationNodes, NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(returnType)));

        return NodeFactory.createFunctionSignatureNode(openParen, parameters,
                closeParen, returnTypeDescriptorNode);
    }

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
                }
//            else if (in.equals("header")) {
//            }
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

    private static void setQueryParameters(List<Node> parameterList, Parameter parameter) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
        TypeDescriptorNode typeName;
        Schema parameterSchema = parameter.getSchema();
        String paramType = convertOpenAPITypeToBallerina(parameterSchema.getType().trim());

        if (parameterSchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) parameterSchema;
            String itemType = arraySchema.getItems().getType();
            if (itemType.equals("string") || itemType.equals("integer") || itemType.equals("boolean")
                    || itemType.equals("float") || itemType.equals("decimal")) {
                paramType = convertOpenAPITypeToBallerina(itemType) + "[]";
            }
        }
        if (parameter.getRequired()) {
             typeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(paramType));
        } else {
             typeName = NodeFactory.createOptionalTypeDescriptorNode(
                    NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(paramType)),
                    createToken(QUESTION_MARK_TOKEN));
        }
        IdentifierToken paramName =
                createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
        RequiredParameterNode queryParam = NodeFactory.createRequiredParameterNode(annotationNodes,
                typeName, paramName);
        parameterList.add(queryParam);
    }

    private static void setPathParameters(List<Node> parameterList, Parameter parameter) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();

        BuiltinSimpleNameReferenceNode typeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim())));
        IdentifierToken paramName =
                createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
        RequiredParameterNode pathParam = NodeFactory.createRequiredParameterNode(annotationNodes,
                typeName, paramName);
        parameterList.add(pathParam);
    }

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
                paramType = escapeIdentifier(extractReferenceType(schema.get$ref()));
            } else {
                String typeOfPayload = schema.getType().trim();
                paramType = convertOpenAPITypeToBallerina(typeOfPayload);
            }
            NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
            SimpleNameReferenceNode typeName =
                    NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(paramType));
            IdentifierToken paramName = createIdentifierToken("payload");
            RequiredParameterNode payload = NodeFactory.createRequiredParameterNode(annotationNodes, typeName
                    , paramName);
            parameterList.add(payload);
            break;
        }
    }

    private static String getReturnType(Operation operation) throws BallerinaOpenApiException {
        String returnType="http:Response | error";
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
                                type = escapeIdentifier(extractReferenceType(schema.get$ref()));
                            } else {
                                type = convertOpenAPITypeToBallerina(schema.getType());
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

    private static FunctionBodyNode getFunctionBodyNode(String path,
                                                        Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        Token openBrace = createToken(OPEN_BRACE_TOKEN);
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        // Create statements
        List<StatementNode> statementsList =  new ArrayList<>();
        // -- create variable declaration
        //string path - common
        TypedBindingPatternNode typedBindingPatternNode =
                NodeFactory.createTypedBindingPatternNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken("string ")),
                        NodeFactory.createCaptureBindingPatternNode(
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
        NodeList<Node> content = NodeFactory.createNodeList(NodeFactory.createLiteralValueToken(null, path,
                createEmptyMinutiaeList(), createEmptyMinutiaeList()));
        Token endBacktick = createToken(BACKTICK_TOKEN);
        TemplateExpressionNode initializer = NodeFactory.createTemplateExpressionNode(null, type, startBacktick,
                content, endBacktick);
        Token semicolon = createToken(SEMICOLON_TOKEN);

        VariableDeclarationNode pathInt = NodeFactory.createVariableDeclarationNode(annotationNodes, null,
                typedBindingPatternNode, equalToken, initializer, semicolon);

        statementsList.add(pathInt);

        //Handel optional query parameter
        List<StatementNode> queryStatement = new ArrayList<>();
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
//            int queryParamCount = 0;
            BuiltinSimpleNameReferenceNode mapType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken("map<anydata>"));
            CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                    createIdentifierToken( "queryParam"));
            TypedBindingPatternNode bindingPatternNode = NodeFactory.createTypedBindingPatternNode(mapType,
                    bindingPattern);

            List<Node> queryParams = new ArrayList();
            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals("query")) {
                    // Initializer
                    // Fill
                    IdentifierToken fieldName =
                            createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
                    Token colon = createToken(COLON_TOKEN);
                    SimpleNameReferenceNode valueExpr = NodeFactory.createSimpleNameReferenceNode(
                            createIdentifierToken(escapeIdentifier(parameter.getName().trim())));
                    SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null,
                            fieldName, colon, valueExpr);
                    queryParams.add(specificFieldNode);
                    queryParams.add(createToken(COMMA_TOKEN));




//                    queryParamCount = queryParamCount + 1;
//                    if (parameter.getRequired()) {
//                        String queryParam = getQueryParamBindingString(queryParamCount, parameter);
//                        path = path + queryParam;
//                    } else {
//                        IfElseStatementNode ifElseStatementNode = getIfElseStatementNode(queryParamCount, parameter);
//                        queryStatement.add(ifElseStatementNode);
//                    }
                }
            }
            if (!queryParams.isEmpty()) {
                queryParams.remove(queryParams.size() - 1);
                MappingConstructorExpressionNode initialize = NodeFactory.createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN),
                        NodeFactory.createSeparatedNodeList(queryParams),
                        createToken(CLOSE_BRACE_TOKEN));
                VariableDeclarationNode mapOfQueryParam =
                        NodeFactory.createVariableDeclarationNode(createEmptyNodeList(), null,
                                bindingPatternNode, createToken(EQUAL_TOKEN), initialize,
                                createToken(SEMICOLON_TOKEN));

                statementsList.add(mapOfQueryParam);

                // Add updated path
                VariableDeclarationNode updatedPath = getSimpleStatement("", "path",
                        "path + getPathForQueryParam(queryParam)");
                statementsList.add(updatedPath);
                isQuery = true;
            }
        }
//        statementsList.addAll(queryStatement);

        //Statement Generator for requestBody-
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
                            VariableDeclarationNode xmlBody = getSimpleStatement("xml", "xmlBody",
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
                        //Fill with other mime type
                    } else {
                        // add default value comment with
                        // TODO: Update the request as needed
                        ExpressionStatementNode expressionStatementNode = getSimpleExpressionStatementNode(
                                "TODO: Update the request as needed");
                        statementsList.add(expressionStatementNode);
                    }
                    VariableDeclarationNode requestStatement = getSimpleStatement("http:Response",
                            "response",  "check self.clientEp->post(path, request)");
                    statementsList.add(requestStatement);
                    Token returnKeyWord = createIdentifierToken("return");
                    SimpleNameReferenceNode returnType = NodeFactory.createSimpleNameReferenceNode(
                                    createIdentifierToken("response"));
                    ReturnStatementNode returnStatementNode = NodeFactory.createReturnStatementNode(returnKeyWord,
                            returnType, semicolon);
                    statementsList.add(returnStatementNode);
                    break;
                }
            }
        } else {
            //Create typeBind for return
            //default bind
            String responseType = "http:Response";
            SimpleNameReferenceNode resTypeBind =
                    NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(responseType));
            CaptureBindingPatternNode bindingPattern = NodeFactory.createCaptureBindingPatternNode(
                            createIdentifierToken("response"));
            TypedBindingPatternNode resTypedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(resTypeBind,
                    bindingPattern);

            //Need to focus check expression node with initializer
            Token check = createToken(CHECK_KEYWORD);

            FieldAccessExpressionNode fieldExpr =
                    NodeFactory.createFieldAccessExpressionNode(NodeFactory.createSimpleNameReferenceNode(
                            createIdentifierToken("self")),
                            createToken(DOT_TOKEN),
                            NodeFactory.createSimpleNameReferenceNode(
                                    createIdentifierToken("clientEp")));
            Token rightArrowToken = createToken(RIGHT_ARROW_TOKEN);
            //Method name
            SimpleNameReferenceNode methodName =
                    NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(
                            operation.getKey().name().trim().toLowerCase(Locale.ENGLISH)));
            Token openParen = createToken(OPEN_PAREN_TOKEN);
            //Argument Nodes
            List<Node> argNodes = new ArrayList<>();
            PositionalArgumentNode positionalArgumentNode = NodeFactory.createPositionalArgumentNode(NodeFactory
                    .createSimpleNameReferenceNode(createIdentifierToken("path")));

            argNodes.add(positionalArgumentNode);
            Token comma = createToken(COMMA_TOKEN);
            argNodes.add(comma);

            SimpleNameReferenceNode argNode = NodeFactory.createSimpleNameReferenceNode(
                            createIdentifierToken("targetType"));
            String returnType = getReturnType(operation.getValue()).split("\\|")[0];
            SimpleNameReferenceNode expression = NodeFactory.
                    createSimpleNameReferenceNode(createIdentifierToken(returnType));
            NamedArgumentNode targetTypeNode = NodeFactory.createNamedArgumentNode(argNode, equalToken, expression);

            argNodes.add(targetTypeNode);

            SeparatedNodeList<FunctionArgumentNode> argumentNodeList = NodeFactory.createSeparatedNodeList(argNodes);
            Token closeParen = createToken(CLOSE_PAREN_TOKEN);
            RemoteMethodCallActionNode actionNode = NodeFactory.createRemoteMethodCallActionNode(fieldExpr,
                    rightArrowToken, methodName, openParen, argumentNodeList, closeParen);
            CheckExpressionNode initRes = NodeFactory.createCheckExpressionNode(null, check, actionNode);
            VariableDeclarationNode checkVariable = NodeFactory.createVariableDeclarationNode(annotationNodes,
                    null, resTypedBindingPatternNode, equalToken, initRes, semicolon);
            statementsList.add(checkVariable);
            //Return Variable
//        Token returnKeyWord = AbstractNodeFactory.createToken(RETURN_STATEMENT);
            Token returnKeyWord = createIdentifierToken("return");
            SimpleNameReferenceNode returns = NodeFactory.createSimpleNameReferenceNode(
                    createIdentifierToken("response"));
            ReturnStatementNode returnStatementNode = NodeFactory.createReturnStatementNode(returnKeyWord, returns,
                    semicolon);
            statementsList.add(returnStatementNode);
        }


        //Create statements
        NodeList<StatementNode> statements = NodeFactory.createNodeList(statementsList);
        Token closeBrace = NodeFactory.createToken(CLOSE_BRACE_TOKEN);
        return NodeFactory.createFunctionBodyBlockNode(openBrace, null, statements,
                closeBrace);
    }

    private static IfElseStatementNode getIfElseStatementNode(int queryParamCount, Parameter parameter) {

        //Create IfElse statement
        Token ifKeyWord = createToken(IF_KEYWORD);
        SimpleNameReferenceNode queryParam = NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken(escapeIdentifier(parameter.getName())));

        Token isKeyWord = createToken(IS_KEYWORD);
        //Temporary assign to null while handlin other data
        TypeTestExpressionNode conditionNode = null;
        Schema paramSchema = parameter.getSchema();
        if (paramSchema instanceof ArraySchema) {
            ArraySchema schema = (ArraySchema) paramSchema;
            String itemType = schema.getItems().getType();
            if (itemType.equals("string") || itemType.equals("integer") || itemType.equals("boolean")
                    || itemType.equals("float") || itemType.equals("decimal")) {
                BuiltinSimpleNameReferenceNode queryParamType =
                        NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(
                                        convertOpenAPITypeToBallerina(itemType) + "[]"));
                conditionNode = NodeFactory.createTypeTestExpressionNode(queryParam, isKeyWord,
                        queryParamType);
            } else {

            }
        } else if (!(parameter.getSchema() instanceof ObjectSchema)) {
            BuiltinSimpleNameReferenceNode queryParamType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(convertOpenAPITypeToBallerina(
                            parameter.getSchema().getType().trim())));

            conditionNode = NodeFactory.createTypeTestExpressionNode(queryParam, isKeyWord, queryParamType);
        }
        BracedExpressionNode conditionStatement = NodeFactory.createBracedExpressionNode(null,
                createToken(OPEN_PAREN_TOKEN), conditionNode,
                createToken(CLOSE_PAREN_TOKEN));

        //If Body handle
        SimpleNameReferenceNode lhsExpr = NodeFactory.createSimpleNameReferenceNode(
                createIdentifierToken("path"));
        Token operator = createToken(PLUS_TOKEN);

        String contentPath = getQueryParamBindingString(queryParamCount, parameter);
        SimpleNameReferenceNode pathContent = NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken(contentPath));

        TemplateExpressionNode rhsExpr = NodeFactory.createTemplateExpressionNode(null, null,
                createToken(BACKTICK_TOKEN),
                NodeFactory.createNodeList(pathContent),
                createToken(BACKTICK_TOKEN));

        BinaryExpressionNode binaryExpr = NodeFactory.createBinaryExpressionNode(null, lhsExpr, operator, rhsExpr);
        AssignmentStatementNode ifBodyStatement = NodeFactory.createAssignmentStatementNode(
                        NodeFactory.createSimpleNameReferenceNode(
                                createIdentifierToken("path")),
                                createToken(EQUAL_TOKEN), binaryExpr,
                                createToken(SEMICOLON_TOKEN));

        NodeList<StatementNode> ifBodyContent = NodeFactory.createNodeList(ifBodyStatement);
        BlockStatementNode ifBlock = NodeFactory.createBlockStatementNode(
                createToken(OPEN_BRACE_TOKEN), ifBodyContent,
                createToken(CLOSE_BRACE_TOKEN));

        return NodeFactory.createIfElseStatementNode(ifKeyWord, conditionStatement, ifBlock, null);
    }

    private static String getQueryParamBindingString(int queryParamCount, Parameter parameter) {

        String queryParam;
        if (queryParamCount == 1) {
            queryParam =
                    "?" + parameter.getName().trim() + "=${" + escapeIdentifier(parameter.getName().trim()) + "}";
        } else {
            queryParam =
                    "&" + parameter.getName().trim() + "=${" + escapeIdentifier(parameter.getName().trim()) + "}";
        }
        return queryParam;
    }

    private static  VariableDeclarationNode getSimpleStatement(String responseType, String variable,
                                                               String initializer) {
        SimpleNameReferenceNode resTypeBind =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(responseType));
        CaptureBindingPatternNode bindingPattern =
                NodeFactory.createCaptureBindingPatternNode(createIdentifierToken(variable));
        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(resTypeBind,
                bindingPattern);
        SimpleNameReferenceNode init = NodeFactory.createSimpleNameReferenceNode(
                createIdentifierToken(initializer));

        return NodeFactory.createVariableDeclarationNode(NodeFactory.createEmptyNodeList(), null,
                typedBindingPatternNode, createToken(EQUAL_TOKEN), init,
                createToken(SEMICOLON_TOKEN));

    }

    private static ExpressionStatementNode getSimpleExpressionStatementNode(String expression) {
        SimpleNameReferenceNode expressionNode = NodeFactory.createSimpleNameReferenceNode(
                createIdentifierToken(expression));
        return NodeFactory.createExpressionStatementNode(null, expressionNode,
                        createToken(SEMICOLON_TOKEN));
    }

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
            // TO-DO: fill other types
        }
    }

    // Create queryPath param function
    private static FunctionDefinitionNode getQueryParamPath() {
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(" getPathForQueryParam");
        FunctionSignatureNode functionSignatureNode =
                NodeFactory.createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                        NodeFactory.createSeparatedNodeList(NodeFactory.createRequiredParameterNode(
                                NodeFactory.createEmptyNodeList(),
                                createIdentifierToken("map<anydata> "),
                                createIdentifierToken(" queryParam"))),
                        createToken(CLOSE_PAREN_TOKEN),
                        NodeFactory.createReturnTypeDescriptorNode(
                                createIdentifierToken(" returns "),
                                NodeFactory.createEmptyNodeList(),
                                NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                        createIdentifierToken("string"))));

        // FunctionBody
        List<StatementNode> statementNodes = new ArrayList<>();
        VariableDeclarationNode variable = getSimpleStatement("string[]", "param", "[]");
        statementNodes.add(variable);
        ExpressionStatementNode assign = getSimpleExpressionStatementNode("param[param.length()] = \"?\"");
        statementNodes.add(assign);

        // Create for each loop
        Token forEachKeyWord = createToken(FOREACH_KEYWORD);

        BuiltinSimpleNameReferenceNode type = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(" var"));
        ListBindingPatternNode bindingPattern =
                NodeFactory.createListBindingPatternNode(createToken(OPEN_BRACKET_TOKEN),
                NodeFactory.createSeparatedNodeList(NodeFactory.createCaptureBindingPatternNode(
                        createIdentifierToken("key")),
                        createToken(COMMA_TOKEN),
                        NodeFactory.createCaptureBindingPatternNode(
                                createIdentifierToken("value"))),
                        createToken(CLOSE_BRACKET_TOKEN));

        TypedBindingPatternNode typedBindingPatternNode = NodeFactory.createTypedBindingPatternNode(type,
                bindingPattern);

        Token inKeyWord = createToken(IN_KEYWORD);
//        ExpressionStatementNode actionOrExpr = getSimpleExpressionStatementNode(" queryParam.entries()");
        SimpleNameReferenceNode expr =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(" queryParam"));
        Token dotToken = NodeFactory.createToken(DOT_TOKEN);
        SimpleNameReferenceNode methodName =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken("entries"));
        MethodCallExpressionNode actionOrExpr = NodeFactory.createMethodCallExpressionNode(expr, dotToken, methodName
                , createToken(OPEN_PAREN_TOKEN), createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));
        // block statement
        // if-else statements
        Token ifKeyWord = createToken(IF_KEYWORD);
        // Create 'value is ()' statement
        SimpleNameReferenceNode expression = NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(" value "));
        Token isKeyWord = createToken(IS_KEYWORD);
        BuiltinSimpleNameReferenceNode typeCondition = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                NodeFactory.createIdentifierToken(" ()"));
        TypeTestExpressionNode mainCondition = NodeFactory.createTypeTestExpressionNode(expression, isKeyWord,
                typeCondition);
        // If body --
        ExpressionStatementNode assignStatement = getSimpleExpressionStatementNode("_ = queryParam.remove(key)");
        BlockStatementNode ifBlockStatementMain = NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        NodeFactory.createNodeList(assignStatement), createToken(CLOSE_BRACE_TOKEN));
        //else body
        Token elseKeyWord = createToken(ELSE_KEYWORD);
        //body statements
        FunctionCallExpressionNode condition =
                NodeFactory.createFunctionCallExpressionNode(NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken(" string:startsWith")), createToken(OPEN_PAREN_TOKEN),
                        NodeFactory.createSeparatedNodeList(
                                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(" key")),
                                createToken(COMMA_TOKEN), NodeFactory.createSimpleNameReferenceNode(
                                        createIdentifierToken("\"'\""))), createToken(CLOSE_PAREN_TOKEN));
        List<StatementNode> statements = new ArrayList<>();
        // if body-02
        ExpressionStatementNode ifBody02Statement = getSimpleExpressionStatementNode(" param[param.length()] = " +
                "string:substring(key, 1, key.length())");

        NodeList<StatementNode> statementNodesForIf02 = NodeFactory.createNodeList(ifBody02Statement);
        BlockStatementNode ifBlock02 = NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        statementNodesForIf02, createToken(CLOSE_BRACE_TOKEN));

        // else block-02
        // else body 02

        ExpressionStatementNode elseBody02Statement = getSimpleExpressionStatementNode("param[param.length()] = key");
        NodeList<StatementNode> statementNodesForElse02 = NodeFactory.createNodeList(elseBody02Statement);
        BlockStatementNode elseBlockNode02 =
                NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        statementNodesForElse02, createToken(CLOSE_BRACE_TOKEN));

        ElseBlockNode elseBlock02 = NodeFactory.createElseBlockNode(createToken(ELSE_KEYWORD),
                elseBlockNode02);

        IfElseStatementNode ifElseStatementNode02 = NodeFactory.createIfElseStatementNode(ifKeyWord, condition,
                ifBlock02, elseBlock02);
        statements.add(ifElseStatementNode02);

        ExpressionStatementNode assignment = getSimpleExpressionStatementNode("param[param.length()] = \"=\"");
        statements.add(assignment);

        //If block 03
        SimpleNameReferenceNode exprIf03 =
                NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(" value "));
        BuiltinSimpleNameReferenceNode typeCondition03 = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        NodeFactory.createIdentifierToken(" string"));
        TypeTestExpressionNode condition03 = NodeFactory.createTypeTestExpressionNode(exprIf03, isKeyWord,
                typeCondition03);

        ExpressionStatementNode variableIf03 = getSimpleExpressionStatementNode("string updateV =  checkpanic " +
                "url:encode(value, \"UTF-8\")");
        ExpressionStatementNode assignIf03 = getSimpleExpressionStatementNode("param[param.length()] = updateV");

        BlockStatementNode ifBody03 = NodeFactory.createBlockStatementNode(
                createToken(OPEN_BRACE_TOKEN), NodeFactory.createNodeList(variableIf03,
                        assignIf03), createToken(CLOSE_BRACE_TOKEN));
        BlockStatementNode elseBodyBlock03 = NodeFactory.createBlockStatementNode(
                createToken(OPEN_BRACE_TOKEN),
                NodeFactory.createNodeList(getSimpleExpressionStatementNode("param[param.length()] = value.toString()")),
                createToken(CLOSE_BRACE_TOKEN));
        ElseBlockNode elseBody03 = NodeFactory.createElseBlockNode(elseKeyWord, elseBodyBlock03);
        IfElseStatementNode ifElse03 = NodeFactory.createIfElseStatementNode(ifKeyWord, condition03, ifBody03,
                elseBody03);

        statements.add(ifElse03);

        ExpressionStatementNode andStatement = getSimpleExpressionStatementNode("param[param.length()] = \"&\"");
        statements.add(andStatement);

        NodeList<StatementNode> elseBodyStatements = NodeFactory.createNodeList(statements);

        BlockStatementNode elseBlock =
                NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        elseBodyStatements, createToken(CLOSE_BRACE_TOKEN));

        ElseBlockNode elseBlockMain = NodeFactory.createElseBlockNode(elseKeyWord, elseBlock);

        IfElseStatementNode mainIfElse = NodeFactory.createIfElseStatementNode(ifKeyWord, mainCondition, ifBlockStatementMain
                , elseBlockMain);

        //For each block statement
        BlockStatementNode forEachBlockStatement =
                NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        NodeFactory.createNodeList(mainIfElse), createToken(CLOSE_BRACE_TOKEN));

        ForEachStatementNode forEachStatementNode = NodeFactory.createForEachStatementNode(forEachKeyWord,
                typedBindingPatternNode, inKeyWord, actionOrExpr, forEachBlockStatement, null);

        statementNodes.add(forEachStatementNode);

        //remove last & statement
        ExpressionStatementNode assignLine02 = getSimpleExpressionStatementNode("_ = param.remove(param.length()-1)");
        statementNodes.add(assignLine02);

        //IfElseStatement
        SimpleNameReferenceNode lhs = NodeFactory.createSimpleNameReferenceNode(
                        createIdentifierToken(" param.length()"));
        Token equalToken = createIdentifierToken("==");
        BuiltinSimpleNameReferenceNode rhs = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        NodeFactory.createIdentifierToken(" 1"));
        TypeTestExpressionNode conditionForIfElse = NodeFactory.createTypeTestExpressionNode(lhs, equalToken, rhs);
        //if body block

        ExpressionStatementNode newAssign = getSimpleExpressionStatementNode("_ = param.remove(0)");
        BlockStatementNode ifBlock =
                NodeFactory.createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                        NodeFactory.createNodeList(newAssign), createToken(CLOSE_BRACE_TOKEN));
        IfElseStatementNode ifElseStatementNode = NodeFactory.createIfElseStatementNode(ifKeyWord, conditionForIfElse
                , ifBlock, null);
        statementNodes.add(ifElseStatementNode);

        statementNodes.add(getSimpleExpressionStatementNode("string restOfPath = string:'join(\"\", ...param)"));
        statementNodes.add(getSimpleExpressionStatementNode("return restOfPath"));
        FunctionBodyNode functionBodyNode =
                NodeFactory.createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null,
                        NodeFactory.createNodeList(statementNodes), createToken(CLOSE_BRACE_TOKEN));

        return NodeFactory.createFunctionDefinitionNode(null, null, NodeFactory.createEmptyNodeList(),
                functionKeyWord, functionName, NodeFactory.createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }
}
