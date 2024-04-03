/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExternalFunctionBodyNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EXTERNAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OBJECT_METHOD_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;

/**
 * This class is used to generate the external remote function for the client.
 *
 * @since 1.9.0
 */
public class RemoteExternalFunctionGenerator extends RemoteFunctionGenerator {


    RemoteExternalFunctionGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation, OpenAPI openAPI,
                                    AuthConfigGeneratorImp authConfigGeneratorImp,
                                    BallerinaUtilGenerator ballerinaUtilGenerator) {
        super(path, operation, openAPI, authConfigGeneratorImp, ballerinaUtilGenerator);
    }

    @Override
    protected Optional<FunctionDefinitionNode> getFunctionDefinitionNode(NodeList<Token> qualifierList,
                                                                         Token functionKeyWord,
                                                                         IdentifierToken functionName,
                                                                         RemoteFunctionSignatureGenerator
                                                                                 signatureGenerator,
                                                                         FunctionBodyNode functionBodyNode) {
        BasicLiteralNode implFuncName = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"" + operation.getValue().getOperationId() +  "Impl\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode implFuncNameField = createSpecificFieldNode(null, createIdentifierToken("name"),
                createToken(COLON_TOKEN), implFuncName);
        MappingConstructorExpressionNode implFunctionMap = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(implFuncNameField), createToken(CLOSE_BRACE_TOKEN));
        SimpleNameReferenceNode annotationRef = createSimpleNameReferenceNode(createIdentifierToken("MethodImpl"));
        AnnotationNode implAnnotation = createAnnotationNode(createToken(AT_TOKEN), annotationRef, implFunctionMap);
        MetadataNode metadataNode = createMetadataNode(null, createNodeList(implAnnotation));
        return Optional.of(NodeFactory.createFunctionDefinitionNode(OBJECT_METHOD_DEFINITION, metadataNode,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(),
                signatureGenerator.generateFunctionSignature().get(), functionBodyNode));
    }

    @Override
    protected Optional<FunctionBodyNode> getFunctionBodyNode() {
        QualifiedNameReferenceNode javaMethodToken = createQualifiedNameReferenceNode(
                createIdentifierToken("java"), createToken(COLON_TOKEN), createIdentifierToken("Method"));
        BasicLiteralNode classValueExp = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"io.ballerina.openapi.client.GeneratedClient\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        BasicLiteralNode methodValueExp = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"invoke\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode classFieldNode = createSpecificFieldNode(null, createIdentifierToken("'class"),
                createToken(COLON_TOKEN), classValueExp);
        SpecificFieldNode methodFieldNode = createSpecificFieldNode(null, createIdentifierToken("name"),
                createToken(COLON_TOKEN), methodValueExp);
        MappingConstructorExpressionNode methodMapExp = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(classFieldNode, createToken(COMMA_TOKEN), methodFieldNode), createToken(CLOSE_BRACE_TOKEN));
        AnnotationNode javaMethodAnnot = createAnnotationNode(createToken(AT_TOKEN), javaMethodToken, methodMapExp);
        return Optional.of(createExternalFunctionBodyNode(createToken(EQUAL_TOKEN),
                createNodeList(javaMethodAnnot), createToken(EXTERNAL_KEYWORD), createToken(SEMICOLON_TOKEN)));
    }

    @Override
    protected RemoteFunctionSignatureGenerator getSignatureGenerator() {
        return new RemoteExternalFunctionSignatureGenerator(operation.getValue(), openAPI, operation.getKey().toString().toLowerCase());
    }
}
