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

import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    final ServiceDeclarationNode service;

    public ServersMapperImpl(OpenAPI openAPI, Set<ListenerDeclarationNode> endpoints,
                             ServiceDeclarationNode service) {
        this.openAPI = openAPI;
        this.endpoints = endpoints;
        this.service = service;
    }

    public void setServers() {
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
        if (servers.size() > 1) {
            Server mainServer = addEnumValues(servers);
            openAPI.setServers(Collections.singletonList(mainServer));
        }
    }


    private void updateServerDetails(List<Server> servers, ListenerDeclarationNode endPoint, ExpressionNode expNode) {

        if (expNode instanceof QualifiedNameReferenceNode refNode) {
            //Handle QualifiedNameReferenceNode in listener
            if (refNode.identifier().text().trim().equals(endPoint.variableName().text().trim())) {
                String serviceBasePath = ServersMapper.getServiceBasePath(service);
                Server server = extractServer(endPoint, serviceBasePath);
                servers.add(server);
            }
        } else if (expNode.toString().trim().equals(endPoint.variableName().text().trim())) {
            String serviceBasePath = ServersMapper.getServiceBasePath(service);
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
    private static Server extractServer(ListenerDeclarationNode ep, String serviceBasePath) {
        Optional<ParenthesizedArgList> list;
        if (ep.initializer().kind() == SyntaxKind.CHECK_EXPRESSION) {
            ExpressionNode expression = ((CheckExpressionNode) ep.initializer()).expression();
            list = extractListenerNodeType(expression);
        } else {
            list = extractListenerNodeType(ep.initializer());
        }
        return generateServer(serviceBasePath, list);
    }

    private static Optional<ParenthesizedArgList> extractListenerNodeType(Node expression2) {
        Optional<ParenthesizedArgList> list = Optional.empty();
        if (expression2.kind() == SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            ExplicitNewExpressionNode bTypeExplicit = (ExplicitNewExpressionNode) expression2;
            list = Optional.ofNullable(bTypeExplicit.parenthesizedArgList());
        } else if (expression2.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION) {
            ImplicitNewExpressionNode bTypeInit = (ImplicitNewExpressionNode) expression2;
            list = bTypeInit.parenthesizedArgList();
        }
        return list;
    }

    // Function to handle ExplicitNewExpressionNode in listener.
    private void extractServerForExpressionNode() {
        SeparatedNodeList<ExpressionNode> bTypeExplicit = service.expressions();
        String serviceBasePath = ServersMapper.getServiceBasePath(service);
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
    private static Server generateServer(String serviceBasePath, Optional<ParenthesizedArgList> list) {

        String port = null;
        String host = null;
        ServerVariables serverVariables = new ServerVariables();
        Server server = new Server();

        if (list.isPresent()) {
            SeparatedNodeList<FunctionArgumentNode> arg = (list.get()).arguments();
            port = arg.get(0).toString();
            if (arg.size() > 1 && (arg.get(1) instanceof NamedArgumentNode)) {
                ExpressionNode bLangRecordLiteral = ((NamedArgumentNode) arg.get(1)).expression();
                if (bLangRecordLiteral instanceof MappingConstructorExpressionNode) {
                    host = extractHost((MappingConstructorExpressionNode) bLangRecordLiteral);
                }
            }
        }
        // Set default values to host and port if values are not defined
        setServerVariableValues(serviceBasePath, port, host, serverVariables, server);
        return server;
    }

    /**
     * Set server variables port and server.
     */
    private static void setServerVariableValues(String serviceBasePath, String port, String host,
                                                ServerVariables serverVariables, Server server) {

        String serverUrl;
        if (host != null && port != null) {
            ServerVariable serverUrlVariable = new ServerVariable();
            serverUrlVariable._default(host);
            ServerVariable portVariable =  new ServerVariable();
            portVariable._default(port);

            serverVariables.addServerVariable(SERVER, serverUrlVariable);
            serverVariables.addServerVariable(PORT, portVariable);
            serverUrl = String.format("{server}:{port}%s", serviceBasePath);
            server.setUrl(serverUrl);
            server.setVariables(serverVariables);
        } else if (host != null) {
            ServerVariable serverUrlVariable = new ServerVariable();
            serverUrlVariable._default(host);

            serverVariables.addServerVariable(SERVER, serverUrlVariable);
            serverUrl = "{server}" + serviceBasePath;
            server.setUrl(serverUrl);
            server.setVariables(serverVariables);

        } else if (port != null) {
            ServerVariable serverUrlVariable = new ServerVariable();
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
            server.setUrl(serverUrl);
            server.setVariables(serverVariables);
        }
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
}
