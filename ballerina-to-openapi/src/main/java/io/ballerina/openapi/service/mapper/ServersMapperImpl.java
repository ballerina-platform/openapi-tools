/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor.VariableDeclaredValue;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.DEFAULT_LISTENER_FUNCTION_NAME;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.PORT;
import static io.ballerina.openapi.service.mapper.Constants.SERVER;

/**
 * This {@link ServersMapperImpl} class is the implementation of the {@link ServersMapper} interface.
 * This class provides the functionality for mapping Ballerina listener information to OpenAPI servers section.
 *
 * @since 1.0.0
 */
public class ServersMapperImpl implements ServersMapper {

    final OpenAPI openAPI;
    final Set<ListenerDeclarationNode> endpoints;
    final ServiceNode service;
    final ModuleMemberVisitor moduleMemberVisitor;
    final List<OpenAPIMapperDiagnostic> diagnostics;

    /**
     * @deprecated Use {@link #ServersMapperImpl(OpenAPI, AdditionalData, Set, ServiceNode)}
     * instead.
     */
    @Deprecated(forRemoval = true, since = "2.3.3")
    public ServersMapperImpl(OpenAPI openAPI, Set<ListenerDeclarationNode> endpoints, ServiceNode service) {
        this.openAPI = openAPI;
        this.endpoints = endpoints;
        this.service = service;
        this.moduleMemberVisitor = null;
        this.diagnostics = new ArrayList<>();
    }

    public ServersMapperImpl(OpenAPI openAPI, AdditionalData additionalData, Set<ListenerDeclarationNode> endpoints,
                             ServiceNode service) {
        this.openAPI = openAPI;
        this.endpoints = endpoints;
        this.service = service;
        this.moduleMemberVisitor = additionalData.moduleMemberVisitor();
        this.diagnostics = additionalData.diagnostics();
    }

    public void setServers() {
        if (service.kind().equals(ServiceNode.Kind.SERVICE_OBJECT_TYPE)) {
            Server defaultServer = getDefaultServerWithBasePath(service.absoluteResourcePath());
            openAPI.setServers(Collections.singletonList(defaultServer));
            return;
        }

        extractServerForExpressionNode();
        List<Server> servers = openAPI.getServers();
        //Handle ImplicitNewExpressionNode in listener
        if (!endpoints.isEmpty()) {
            for (ListenerDeclarationNode ep : endpoints) {
                SeparatedNodeList<ExpressionNode> exprNodes = service.expressions();
                for (ExpressionNode node : exprNodes) {
                    updateServerDetails(servers, ep, node);
                }
            }
        }
        if (hasEmptyServer()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_141));
            openAPI.setServers(null);
            return;
        }
        if (servers.size() > 1) {
            Server mainServer = addEnumValues(servers);
            openAPI.setServers(Collections.singletonList(mainServer));
        }
    }

    private boolean hasEmptyServer() {
        return openAPI.getServers().isEmpty() ||
                openAPI.getServers().stream()
                        .allMatch(server -> Objects.isNull(server.getUrl()) &&
                                (Objects.isNull(server.getVariables()) || server.getVariables().isEmpty()));
    }


    private void updateServerDetails(List<Server> servers, ListenerDeclarationNode endPoint, ExpressionNode expNode) {

        if (expNode instanceof QualifiedNameReferenceNode refNode) {
            //Handle QualifiedNameReferenceNode in listener
            if (refNode.identifier().text().trim().equals(endPoint.variableName().text().trim())) {
                String serviceBasePath = service.absoluteResourcePath();
                Server server = extractServer(endPoint, serviceBasePath);
                servers.add(server);
            }
        } else if (expNode.toString().trim().equals(endPoint.variableName().text().trim())) {
            String serviceBasePath = service.absoluteResourcePath();
            Server server = extractServer(endPoint, serviceBasePath);
            servers.add(server);
        }
    }

    private static Server addEnumValues(List<Server> servers) {

        Server mainServer = servers.get(0);
        List<Server> rotated = new ArrayList<>(servers);
        ServerVariables mainVariable = mainServer.getVariables();
        ServerVariable hostVariable = mainVariable.get(SERVER);
        ServerVariable portVariable = mainVariable.get(PORT);
        if (servers.size() > 1) {
            Collections.rotate(rotated, servers.size() - 1);
            for (Server server: rotated) {
                ServerVariables variables = server.getVariables();
                if (variables.get(SERVER) != null) {
                    hostVariable.addEnumItem(variables.get(SERVER).getDefault());
                }
                if (variables.get(PORT) != null) {
                    portVariable.addEnumItem(variables.get(PORT).getDefault());
                }
            }
        }
        return mainServer;
    }

    /**
     * Extract server URL from given listener node.
     */
    private Server extractServer(ListenerDeclarationNode ep, String serviceBasePath) {
        Node expression;
        if (ep.initializer().kind() == SyntaxKind.CHECK_EXPRESSION) {
            expression = ((CheckExpressionNode) ep.initializer()).expression();
        } else {
            expression = ep.initializer();
        }
        if (isHttpDefaultListener(expression)) {
            // Using the default configurations from the HTTP default listener
            // The default values can be overridden using a configuration file
            return getDefaultServerWithBasePath(serviceBasePath);
        }
        Optional<ParenthesizedArgList> list = extractListenerNodeType(expression);
        return generateServer(serviceBasePath, list);
    }

    private static Optional<ParenthesizedArgList> extractListenerNodeType(Node expression) {
        Optional<ParenthesizedArgList> list = Optional.empty();
        if (expression instanceof ExplicitNewExpressionNode newExpression) {
            list = Optional.ofNullable(newExpression.parenthesizedArgList());
        } else if (expression instanceof ImplicitNewExpressionNode newExpression) {
            list = newExpression.parenthesizedArgList();
        }
        return list;
    }

    private static boolean isHttpDefaultListener(Node expression) {
        if (expression.kind() != SyntaxKind.FUNCTION_CALL) {
            return false;
        }

        FunctionCallExpressionNode functionCall = (FunctionCallExpressionNode) expression;
        NameReferenceNode functionName = functionCall.functionName();

        return functionName instanceof QualifiedNameReferenceNode qualifiedName &&
               HTTP.equals(qualifiedName.modulePrefix().text()) &&
               DEFAULT_LISTENER_FUNCTION_NAME.equals(qualifiedName.identifier().text());
    }

    // Function to handle ExplicitNewExpressionNode in listener.
    private void extractServerForExpressionNode() {
        SeparatedNodeList<ExpressionNode> bTypeExplicit = service.expressions();
        String serviceBasePath = service.absoluteResourcePath();
        Optional<ParenthesizedArgList> list;
        List<Server> servers = new ArrayList<>();
        for (ExpressionNode expressionNode: bTypeExplicit) {
            if (expressionNode.kind().equals(SyntaxKind.EXPLICIT_NEW_EXPRESSION)) {
                ExplicitNewExpressionNode explicit = (ExplicitNewExpressionNode) expressionNode;
                list = Optional.ofNullable(explicit.parenthesizedArgList());
                Server server = generateServer(serviceBasePath, list);
                servers.add(server);
            }
        }
        openAPI.setServers(servers);
    }

    //Assign host and port values
    private Server generateServer(String serviceBasePath, Optional<ParenthesizedArgList> list) {
        String port = null;
        String host = null;
        ServerVariables serverVariables = new ServerVariables();
        Server server = new Server();

        if (list.isPresent()) {
            SeparatedNodeList<FunctionArgumentNode> arguments = list.get().arguments();
            int index = 0;
            for (; index < arguments.size(); index++) {
                FunctionArgumentNode arg = arguments.get(index);
                if (arg instanceof NamedArgumentNode) {
                    break;
                }
                if (index == 0) {
                    PositionalArgumentNode portArgument = (PositionalArgumentNode) arg;
                    port = getPortValue(portArgument.expression()).orElse(null);
                } else if (index == 1 && arg instanceof PositionalArgumentNode posArg &&
                        posArg.expression() instanceof MappingConstructorExpressionNode config &&
                        hasHostField(config)) {
                    host = extractHost(config);
                }
            }
            for (; index < arguments.size(); index++) {
                FunctionArgumentNode arg = arguments.get(index);
                if (arg instanceof NamedArgumentNode namedArg) {
                    String name = namedArg.argumentName().toString().trim();
                    ExpressionNode expr = namedArg.expression();
                    switch (name) {
                        case "port":
                            port = getPortValue(expr).orElse(null);
                            break;
                        case "host":
                            host = expr.toString().replaceAll("^\"|\"$", "");
                            break;
                        case "config":
                            if (expr instanceof MappingConstructorExpressionNode config &&
                                    hasHostField(config)) {
                                host = extractHost(config);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        // Set default values to host and port if values are not defined
        setServerVariableValues(serviceBasePath, port, host, serverVariables, server);
        return server;
    }

    private Optional<String> getPortValue(ExpressionNode expression) {
        return getPortValue(expression, false);
    }

    private Optional<String> getPortValue(ExpressionNode expression, boolean parentIsConfigurable) {
        if (expression.kind().equals(SyntaxKind.NUMERIC_LITERAL)) {
            BasicLiteralNode literal = (BasicLiteralNode) expression;
            return Optional.of(literal.literalToken().text());
        }

        if (!expression.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
            if (expression.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_142, expression.location()));
                return Optional.empty();
            }

            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_143, expression.location()));
            return Optional.empty();
        }

        SimpleNameReferenceNode simpleName = (SimpleNameReferenceNode) expression;
        String portVariableName = simpleName.name().text();

        // Added due the deprecated constructor
        if (moduleMemberVisitor == null) {
            return Optional.empty();
        }

        if (!moduleMemberVisitor.hasVariableDeclaration(portVariableName)) {
            // Cannot find the variable declaration in the module
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_144,
                    expression.location()));
            return Optional.empty();
        }

        VariableDeclaredValue variableDeclaredValue = moduleMemberVisitor.getVariableDeclaredValue(portVariableName);
        ExpressionNode portValue = variableDeclaredValue.value();

        if (portValue.kind().equals(SyntaxKind.REQUIRED_EXPRESSION)) {
            // This means the port value is a configurable variable without a default value.
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_146, portValue.location()));
            return Optional.empty();
        }

        if (portValue.kind().equals(SyntaxKind.NUMERIC_LITERAL)) {
            if (parentIsConfigurable || variableDeclaredValue.isConfigurable()) {
                // This means the port value is a configurable variable with a default value.
                diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_145, portValue.location()));
            }
            BasicLiteralNode literal = (BasicLiteralNode) portValue;
            return Optional.of(literal.literalToken().text());
        } else {
            return getPortValue(portValue, variableDeclaredValue.isConfigurable());
        }
    }

    private static boolean hasHostField(MappingConstructorExpressionNode config) {
        return config.fields().stream()
                .anyMatch(field -> field instanceof SpecificFieldNode specific &&
                        "host".equals(specific.fieldName().toString().trim()));
    }

    /**
     * Set server variables port and server.
     */
    private static void setServerVariableValues(String serviceBasePath, String port, String host,
                                                ServerVariables serverVariables, Server server) {

        String serverUrl;
        if (port == null) {
            return;
        }
        ServerVariable serverUrlVariable = new ServerVariable();
        if (host != null) {
            serverUrlVariable._default(host);
            ServerVariable portVariable =  new ServerVariable();
            portVariable._default(port);

            serverVariables.addServerVariable(SERVER, serverUrlVariable);
            serverVariables.addServerVariable(PORT, portVariable);
            serverUrl = String.format("{server}:{port}%s", serviceBasePath);
        } else {
            if (port.equals("443")) {
                serverUrlVariable._default("https://localhost");
                ServerVariable portVariable =  new ServerVariable();
                portVariable._default("443");

                serverVariables.addServerVariable(SERVER, serverUrlVariable);
                serverVariables.addServerVariable(PORT, portVariable);
            } else {
                serverUrlVariable._default("http://localhost");
                ServerVariable portVariable =  new ServerVariable();
                portVariable._default(port);

                serverVariables.addServerVariable(SERVER, serverUrlVariable);
                serverVariables.addServerVariable(PORT, portVariable);
            }
            serverUrl = "{server}:{port}" + serviceBasePath;
        }
        server.setUrl(serverUrl);
        server.setVariables(serverVariables);
    }

    // Extract host value for creating URL.
    private static String extractHost(MappingConstructorExpressionNode bLangRecordLiteral) {
        String host = "";
        if (bLangRecordLiteral.fields() != null && !bLangRecordLiteral.fields().isEmpty()) {
            SeparatedNodeList<MappingFieldNode> recordFields = bLangRecordLiteral.fields();
            host = concatenateServerURL(host, recordFields);
        }
        if (!host.isEmpty()) {
           host = host.replaceAll("\"", "");
        }
        return host;
    }

    private static String concatenateServerURL(String host, SeparatedNodeList<MappingFieldNode> recordFields) {

        for (MappingFieldNode filed: recordFields) {
            if (filed instanceof SpecificFieldNode) {
                Node fieldName = ((SpecificFieldNode) filed).fieldName();
                if (fieldName.toString().equals(Constants.ATTR_HOST)) {
                    if (((SpecificFieldNode) filed).valueExpr().isPresent()) {
                          host = ((SpecificFieldNode) filed).valueExpr().get().toString();
                    }
                }
            }
        }
        return host;
    }

    private static Server getDefaultServerWithBasePath(String serviceBasePath) {
        String serverUrl = String.format("http://{server}:{port}%s", serviceBasePath);
        ServerVariables serverVariables = new ServerVariables();

        ServerVariable serverUrlVariable = new ServerVariable();
        serverUrlVariable._default("localhost");
        serverVariables.addServerVariable(SERVER, serverUrlVariable);

        ServerVariable portVariable =  new ServerVariable();
        portVariable._default("9090");
        serverVariables.addServerVariable(PORT, portVariable);

        Server server = new Server();
        server.setUrl(serverUrl);
        server.setVariables(serverVariables);
        return server;
    }
}
