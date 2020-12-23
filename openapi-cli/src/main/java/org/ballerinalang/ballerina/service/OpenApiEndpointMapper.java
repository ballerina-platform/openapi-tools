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


package org.ballerinalang.ballerina.service;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
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
     * Currently this method only selects last endpoint bound to {@code service}
     * as the Server for openapi definition. This is due to OAS2 not supporting
     * multiple Server definitions.
     *
     * @param endpoints all endpoints defined in ballerina source
     * @param service   service node with bound endpoints
     * @param openapi   openapi definition to attach extracted information
     * @return openapi definition with Server information
     */
    public Swagger convertBoundEndpointsToOpenApi(List<ListenerDeclarationNode> endpoints,
                                                  ServiceDeclarationNode service, Swagger openapi) {

        //TODO check absence of endpoints and services
//        if (endpoints == null || service == null || service.getAttachedExprs().isEmpty()
//                || (service.getAttachedExprs().get(0).getKind() != NodeKind.SIMPLE_VARIABLE_REF
//                && service.getAttachedExprs().get(0).getKind() != NodeKind.TYPE_INIT_EXPR)) {
//            return openapi;
//        }
        if (openapi == null) {
            return new Swagger();
        }
        for (ListenerDeclarationNode ep : endpoints) {
            // At the moment only the last bound endpoint will be populated in openapi
            // we need to move to OAS3 models to support multiple server support
             SeparatedNodeList<ExpressionNode> exprNodes = service.expressions();
             ExpressionNode node = exprNodes.get(0);
             if (node.toString().trim().equals(ep.variableName().text().trim())) {
                 extractServer(ep, openapi);
                 break;
                }
            }
        return openapi;
    }

    private void extractServer(ListenerDeclarationNode ep, Swagger openapi) {

        ImplicitNewExpressionNode  bTypeInit = (ImplicitNewExpressionNode) ep.initializer();
        Optional<ParenthesizedArgList> list = bTypeInit.parenthesizedArgList();
        List<Scheme> schemes = new ArrayList<>();
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
                //TODO: need to add check secure socket thing
//            Scheme scheme = configs.get(Constants.SECURE_SOCKETS) == null ? Scheme.HTTP : Scheme.HTTPS;
//            schemes.add(scheme);
            }
        }
        // Set default values to host and port if values are not defined
        if (host == null) {
            host = Constants.ATTR_DEF_HOST;
        }
        if (port != null) {
            host += ':' + port;
        }
        openapi.setHost(host);
        openapi.setSchemes(schemes);
    }

    private String extractHost(MappingConstructorExpressionNode bLangRecordLiteral) {
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
}
