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

package io.ballerina.openapi.generators.service;

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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static io.ballerina.openapi.generators.GeneratorConstants.CONFIG;
import static io.ballerina.openapi.generators.GeneratorConstants.HOST;
import static io.ballerina.openapi.generators.GeneratorConstants.NEW;
import static io.ballerina.openapi.generators.GeneratorUtils.getMinutiaes;

/**
 * This util class for processing the mapping in between openAPI server section with ballerina listeners.
 *
 * @since 2.0.0
 */
public class ListenerGenerator {
    private static final int httpPort = 80;
    private static final int httpsPort = 443;
    private String basePath = "/";

    public ListenerGenerator() {
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * Generate listener accoring to the given server details.
     *
     * @param servers  OAS server details
     * @return         {@link ListenerDeclarationNode} for server.
     * @throws BallerinaOpenApiException when process break with exception
     */
    public ListenerDeclarationNode getListenerDeclarationNodes(List<Server> servers) throws BallerinaOpenApiException {
        // Assign host port value to listeners
        String host;
        int port;
        Server server = servers.get(0);
        if (!server.getUrl().isBlank() && !"/".equals(server.getUrl())) {
            ServerVariables variables = server.getVariables();
            URL url;
            try {
                String resolvedUrl = GeneratorUtils.buildUrl(server.getUrl(), variables);
                url = new URL(resolvedUrl);
                host = url.getHost();
                this.basePath = url.getPath();
                port = url.getPort();
                boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());
                if (port < 0) {
                    port = isHttps ? httpsPort : httpPort;
                }
            } catch (MalformedURLException e) {
                throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
                        server.getUrl(), e);
            }
        } else {
            host = "localhost";
            port = 9090;
        }
        return getListenerDeclarationNode(port, host, "ep0");
    }

    public static ListenerDeclarationNode getListenerDeclarationNode(Integer port, String host, String ep) {
        // Take first server to Map
        Token listenerKeyword = AbstractNodeFactory.createIdentifierToken("listener", getMinutiaes(),
                getMinutiaes());
        // Create type descriptor
        Token modulePrefix = AbstractNodeFactory.createIdentifierToken("http", getMinutiaes(), getMinutiaes());
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Listener", getMinutiaes(),
                AbstractNodeFactory.createEmptyMinutiaeList());
        QualifiedNameReferenceNode typeDescriptor = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,
                AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN), identifier);
        // Create variable
        Token variableName = AbstractNodeFactory.createIdentifierToken(ep, getMinutiaes(),
                AbstractNodeFactory.createEmptyMinutiaeList());
        // Create initializer
        Token newKeyword = AbstractNodeFactory.createIdentifierToken(NEW);

        Token literalToken = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN
                , String.valueOf(port), getMinutiaes(), getMinutiaes());
        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, literalToken);

        PositionalArgumentNode portNode = NodeFactory.createPositionalArgumentNode(expression);

        Token name = AbstractNodeFactory.createIdentifierToken(CONFIG);
        SimpleNameReferenceNode argumentName = NodeFactory.createSimpleNameReferenceNode(name);

        Token fieldName = AbstractNodeFactory.createIdentifierToken(HOST);
        Token literalHostToken = AbstractNodeFactory.createIdentifierToken('"' + host + '"', getMinutiaes(),
                getMinutiaes());
        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                literalHostToken);
        MappingFieldNode hostNode = NodeFactory.createSpecificFieldNode(null, fieldName,
                AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN), valueExpr);
        SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(hostNode);

        MappingConstructorExpressionNode hostExpression = NodeFactory.createMappingConstructorExpressionNode(
                        AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        fields, AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        NamedArgumentNode namedArgumentNode = NodeFactory.createNamedArgumentNode(argumentName,
                AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), hostExpression);

        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(portNode,
                AbstractNodeFactory.createToken(SyntaxKind.COMMA_TOKEN), namedArgumentNode);

        ParenthesizedArgList parenthesizedArgList = NodeFactory.createParenthesizedArgList(
                AbstractNodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN), arguments,
                AbstractNodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
        ImplicitNewExpressionNode initializer = NodeFactory.createImplicitNewExpressionNode(newKeyword,
                parenthesizedArgList);

        return NodeFactory.createListenerDeclarationNode(null, null, listenerKeyword,
                typeDescriptor, variableName, AbstractNodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), initializer,
                AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN));
    }
}
