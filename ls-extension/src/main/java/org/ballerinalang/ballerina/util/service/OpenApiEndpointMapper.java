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


package org.ballerinalang.ballerina.util.service;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.ballerinalang.ballerina.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Extract OpenApi server information from and Ballerina endpoint.
 */
public class OpenApiEndpointMapper {

    /**
     * Convert endpoints bound to {@code service} openapi server information.
     *
     * @param openAPI   openapi definition to attach extracted information
     * @param endpoints all endpoints defined in ballerina source
     * @param service   service node with bound endpoints
     * @return openapi definition with Server information
     */
    public OpenAPI convertListenerEndPointToOpenAPI (OpenAPI openAPI, List<ListenerDeclarationNode> endpoints,
                                                     ServiceDeclarationNode service) {
        if (openAPI == null) {
            return new OpenAPI();
        }
        List<Server> servers = new ArrayList<>();
        for (ListenerDeclarationNode ep : endpoints) {
            SeparatedNodeList<ExpressionNode> exprNodes = service.expressions();
            for (ExpressionNode node : exprNodes) {
                if (node.toString().trim().equals(ep.variableName().text().trim())) {
                    String serviceBasePath = getServiceBasePath(service);
                    Server server = extractServer(ep, serviceBasePath);
                    servers.add(server);
                }
            }
        }
        openAPI.setServers(servers);
        return openAPI;
    }

    private static Server extractServer(ListenerDeclarationNode ep, String serviceBasePath) {

        ImplicitNewExpressionNode  bTypeInit = (ImplicitNewExpressionNode) ep.initializer();
        Optional<ParenthesizedArgList> list = bTypeInit.parenthesizedArgList();
        String port = null;
        String host = null;

        if (list != null && list.isPresent()) {
            SeparatedNodeList<FunctionArgumentNode> arg = (list.get()).arguments();
            port = arg.get(0).toString();
            if (arg.size() > 1) {
              ExpressionNode bLangRecordLiteral = ((NamedArgumentNode) arg.get(1)).expression();
              if (bLangRecordLiteral instanceof MappingConstructorExpressionNode) {
                  host = extractHost((MappingConstructorExpressionNode) bLangRecordLiteral);
              }
            }
        }
        // Set default values to host and port if values are not defined
        if (host == null) {
            host = Constants.ATTR_DEF_HOST;
        }
        if (port != null) {
            host += ':' + port;
        }
        if (!serviceBasePath.isBlank()) {
            host += serviceBasePath;
        }
        Server server = new Server();
        server.setUrl(host);
        return  server;
    }

    private static String extractHost(MappingConstructorExpressionNode bLangRecordLiteral) {
        String host = null;
        MappingConstructorExpressionNode recordConfig = bLangRecordLiteral;
        if (recordConfig.fields() != null && !recordConfig.fields().isEmpty()) {
            SeparatedNodeList<MappingFieldNode> recordFields = recordConfig.fields();
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
        }
        return host;
    }

    /**
     * Gets the base path of a service.
     *
     * @param serviceDefinition The service definition node.
     * @return The base path.
     */
    public static String getServiceBasePath(ServiceDeclarationNode serviceDefinition) {
        StringBuilder currentServiceName = new StringBuilder();
        NodeList<Node> serviceNameNodes = serviceDefinition.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName.append(serviceBasedPathNode.toString());
        }
        return currentServiceName.toString().trim();
    }
}
