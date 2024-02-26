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

package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * This util class for processing the mapping in between openAPI server section with ballerina listeners.
 *
 * @since 1.3.0
 */
public class ListenerGenerator {
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private String basePath = "/";

    public ListenerGenerator() {
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * Generate listener accoring to the given server details.
     *
     * @param servers OAS server details
     * @return {@link ListenerDeclarationNode} for server.
     * @throws BallerinaOpenApiException when process break with exception
     */
//    public ListenerDeclarationNode getListenerDeclarationNodes(List<Server> servers) throws BallerinaOpenApiException {
//        // Assign host port value to listeners
//        String host;
//        int port;
//        Server server = servers.get(0);
//        if (!server.getUrl().isBlank() && !"/".equals(server.getUrl())) {
//            ServerVariables variables = server.getVariables();
//            URL url;
//            try {
//                String resolvedUrl = GeneratorUtils.buildUrl(server.getUrl(), variables);
//                url = new URL(resolvedUrl);
//                host = url.getHost();
//                if (!url.getPath().isBlank()) {
//                    this.basePath = url.getPath();
//                }
//                port = url.getPort();
//                boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());
//                if (port < 0) {
//                    port = isHttps ? HTTPS_PORT : HTTP_PORT;
//                }
//            } catch (MalformedURLException e) {
//                throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
//                        server.getUrl(), e);
//            }
//        } else {
//            host = "localhost";
//            port = 9090;
//        }
//        return getListenerDeclarationNode(port, host, "ep0");
//    }
//
//    public static ListenerDeclarationNode getListenerDeclarationNode(Integer port, String host, String ep) {
//        // Take first server to Map
//        Token listenerKeyword = AbstractNodeFactory.createIdentifierToken("listener", GeneratorUtils.SINGLE_WS_MINUTIAE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE);
//        // Create type descriptor
//        Token modulePrefix = AbstractNodeFactory.createIdentifierToken("http", GeneratorUtils.SINGLE_WS_MINUTIAE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE);
//        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Listener",
//                GeneratorUtils.SINGLE_WS_MINUTIAE, AbstractNodeFactory.createEmptyMinutiaeList());
//        QualifiedNameReferenceNode typeDescriptor = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,
//                AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN), identifier);
//        // Create variable
//        Token variableName = AbstractNodeFactory.createIdentifierToken(ep, GeneratorUtils.SINGLE_WS_MINUTIAE,
//                AbstractNodeFactory.createEmptyMinutiaeList());
//        // Create initializer
//        Token newKeyword = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.NEW);
//
//        Token literalToken = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN
//                , String.valueOf(port), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
//        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, literalToken);
//
//        PositionalArgumentNode portNode = NodeFactory.createPositionalArgumentNode(expression);
//
//        Token name = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CONFIG);
//        SimpleNameReferenceNode argumentName = NodeFactory.createSimpleNameReferenceNode(name);
//
//        Token fieldName = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.HOST);
//        Token literalHostToken = AbstractNodeFactory.createIdentifierToken('"' + host + '"',
//                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
//        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
//                literalHostToken);
//        MappingFieldNode hostNode = NodeFactory.createSpecificFieldNode(null, fieldName,
//                AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN), valueExpr);
//        SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(hostNode);
//
//        MappingConstructorExpressionNode hostExpression = NodeFactory.createMappingConstructorExpressionNode(
//                AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN),
//                fields, AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
//
//        NamedArgumentNode namedArgumentNode = NodeFactory.createNamedArgumentNode(argumentName,
//                AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), hostExpression);
//
//        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(portNode,
//                AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN), namedArgumentNode);
//
//        ParenthesizedArgList parenthesizedArgList = NodeFactory.createParenthesizedArgList(
//                AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN), arguments,
//                AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
//        ImplicitNewExpressionNode initializer = NodeFactory.createImplicitNewExpressionNode(newKeyword,
//                parenthesizedArgList);
//
//        return NodeFactory.createListenerDeclarationNode(null, null, listenerKeyword,
//                typeDescriptor, variableName, AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), initializer,
//                AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN));
//    }
}
