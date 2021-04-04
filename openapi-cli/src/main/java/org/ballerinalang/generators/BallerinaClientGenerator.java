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
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.generators.GeneratorUtils.getBallerinaOpenApiType;

/**
 * This Util class use for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    public static SyntaxTree generateSyntaxTree(Path definitionPath, Filter filter)
            throws IOException, BallerinaOpenApiException {
        // Summaries OpenAPI details
        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
        //Filter serverUrl
        List<Server> servers = openAPI.getServers();

        // 1. Load client template syntax tree
        SyntaxTree syntaxTree = null;
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp);
        ClassDefinitionNode classDefinitionNode = getClassDefinitionNode();

        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports,
                NodeFactory.createNodeList(classDefinitionNode), AbstractNodeFactory.createToken(SyntaxKind.EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }
    private static String getServerURL(Server server) {
        String serverURL;

        return  serverURL;
    }

    private static ClassDefinitionNode getClassDefinitionNode() {

        // Generate client class
        Token visibilityQualifier = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.PUBLIC);
        Token clientKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLIENT);
        NodeList<Token> classTypeQualifiers = NodeFactory.createNodeList(clientKeyWord);

        IdentifierToken classKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLASS);
        IdentifierToken className = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLIENT_CLASS);
        Token openBrace = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_BRACE);
        //Fill the members
        List<Node> memberNodeList =  new ArrayList<>();
        //Create class field
        ObjectFieldNode fieldNode = getClassField();
        memberNodeList.add(fieldNode);
        //Create init function definition
        //Common Used
        NodeList<Token> qualifierList =
                NodeFactory.createNodeList(AbstractNodeFactory.createIdentifierToken(GeneratorConstants.PUBLIC));
        IdentifierToken functionKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.FUNCTION);
        IdentifierToken functionName = AbstractNodeFactory.createIdentifierToken("init");
        //Create function signature
        IdentifierToken openParan = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_PRAN);
        //Add parameters
        List<Node> parameters  = new ArrayList<>();
        NodeList<AnnotationNode> annotationNodes = AbstractNodeFactory.createEmptyNodeList();
        BuiltinSimpleNameReferenceNode typeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                AbstractNodeFactory.createIdentifierToken("string"));
        IdentifierToken paramName = AbstractNodeFactory.createIdentifierToken("serviceUrl");
        IdentifierToken equalToken = AbstractNodeFactory.createIdentifierToken("=");
        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                AbstractNodeFactory.createIdentifierToken("http://localhost:9090/v1"));

        DefaultableParameterNode serviceUrl = NodeFactory.createDefaultableParameterNode(annotationNodes, typeName,
                paramName, equalToken, expression);
        IdentifierToken comma = AbstractNodeFactory.createIdentifierToken(",");
        parameters.add(serviceUrl);
        parameters.add(comma);

        QualifiedNameReferenceNode typeName1 =
                NodeFactory.createQualifiedNameReferenceNode(AbstractNodeFactory.createIdentifierToken(GeneratorConstants.HTTP),
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.COLON),
                        AbstractNodeFactory.createIdentifierToken("ClientConfiguration"));
        IdentifierToken paramName1 = AbstractNodeFactory.createIdentifierToken("httpClientConfig");
        BasicLiteralNode expression1 = NodeFactory.createBasicLiteralNode(null,
                AbstractNodeFactory.createIdentifierToken("{}"));

        DefaultableParameterNode clientConfig = NodeFactory.createDefaultableParameterNode(annotationNodes, typeName1
                , paramName1, equalToken, expression1);
        parameters.add(clientConfig);

        SeparatedNodeList<ParameterNode> parameterList = AbstractNodeFactory.createSeparatedNodeList(parameters);
        IdentifierToken closeParan = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLOSE_PRAN);

        //Return type
        IdentifierToken returnsKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.RETURN);
        OptionalTypeDescriptorNode type =
                NodeFactory.createOptionalTypeDescriptorNode(NodeFactory.createErrorTypeDescriptorNode(
                        AbstractNodeFactory.createIdentifierToken("error"), null),
                        AbstractNodeFactory.createIdentifierToken("?"));
        ReturnTypeDescriptorNode returnNode = NodeFactory.createReturnTypeDescriptorNode(returnsKeyWord,
                annotationNodes, type);

        //Create function signature
        FunctionSignatureNode functionSignatureNode = NodeFactory.createFunctionSignatureNode(openParan,
                parameterList, closeParan, returnNode);

        //Create function body
        Token openBraceFB = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_BRACE);
        //Create Statement
        List<StatementNode> statements = new ArrayList<>();

        // Variable Declaration Node
        NodeList<AnnotationNode> annotationNodes1 = AbstractNodeFactory.createEmptyNodeList();
        // Create type Binding
        QualifiedNameReferenceNode typeBindingPattern = NodeFactory.createQualifiedNameReferenceNode(
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.HTTP),
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.COLON),
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        CaptureBindingPatternNode bindingPattern =
                NodeFactory.createCaptureBindingPatternNode(AbstractNodeFactory.createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode =
                NodeFactory.createTypedBindingPatternNode(typeBindingPattern, bindingPattern);

        //Expression node
        Token newKeyWord = AbstractNodeFactory.createIdentifierToken("new");
        Token openParenArg = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_PRAN);
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        AbstractNodeFactory.createIdentifierToken("serviceUrl")));
        Token comma1 = AbstractNodeFactory.createIdentifierToken(",");
        PositionalArgumentNode positionalArgumentNode02 =
                NodeFactory.createPositionalArgumentNode(NodeFactory.createSimpleNameReferenceNode(
                        AbstractNodeFactory.createIdentifierToken("httpClientConfig")));

        argumentsList.add(positionalArgumentNode01);
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(argumentsList);
        Token closeParenArg = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = NodeFactory.createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = NodeFactory.createImplicitNewExpressionNode(newKeyWord,
                parenthesizedArgList);
        CheckExpressionNode initializer = NodeFactory.createCheckExpressionNode(null,
                AbstractNodeFactory.createToken(SyntaxKind.CHECK_KEYWORD), expressionNode);
        VariableDeclarationNode variableDeclarationNode = NodeFactory.createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN),
                initializer, AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN));
        statements.add(variableDeclarationNode);
        //Assigment
        FieldAccessExpressionNode varRef =
                NodeFactory.createFieldAccessExpressionNode(NodeFactory.createSimpleNameReferenceNode(
                        AbstractNodeFactory.createIdentifierToken("self")),
                        AbstractNodeFactory.createToken(SyntaxKind.DOT_TOKEN),
                        NodeFactory.createSimpleNameReferenceNode(
                                AbstractNodeFactory.createIdentifierToken("clientEp")));
        SimpleNameReferenceNode expr =
                NodeFactory.createSimpleNameReferenceNode(AbstractNodeFactory.createIdentifierToken("httpEp"));
        AssignmentStatementNode assignmentStatementNode = NodeFactory.createAssignmentStatementNode(varRef,
                AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), expr,
                AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN));

        NodeList<StatementNode> statementList = NodeFactory.createNodeList(variableDeclarationNode,
                assignmentStatementNode);

        Token closeBraceFB = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        FunctionBodyNode functionBodyNode = NodeFactory.createFunctionBodyBlockNode(openBraceFB, null,
                statementList, closeBraceFB);
        FunctionDefinitionNode functionDefinitionNode = NodeFactory.createFunctionDefinitionNode(null, null,
                qualifierList,functionKeyWord, functionName, NodeFactory.createEmptyNodeList(), functionSignatureNode
                , functionBodyNode);

        memberNodeList.add(functionDefinitionNode);

        return NodeFactory.createClassDefinitionNode(null, visibilityQualifier,
                classTypeQualifiers, classKeyWord, className, openBrace, NodeFactory.createNodeList(memberNodeList),
                AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
    }

    private static ObjectFieldNode getClassField() {

        Token visibilityQualifierAttribute = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.PUBLIC);
        NodeList<Token> qualifierList = AbstractNodeFactory.createEmptyNodeList();
        QualifiedNameReferenceNode typeName =
                NodeFactory.createQualifiedNameReferenceNode(AbstractNodeFactory.createIdentifierToken(
                        GeneratorConstants.HTTP),
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.COLON),
                        AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLIENT_CLASS));
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLIENT_EP);
        return NodeFactory.createObjectFieldNode(null, visibilityQualifierAttribute,
                qualifierList, typeName, fieldName, null, null,
                AbstractNodeFactory.createIdentifierToken(GeneratorConstants.SEMICOLON));
    }

}
