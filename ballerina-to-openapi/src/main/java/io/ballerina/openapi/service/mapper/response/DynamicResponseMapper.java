/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.openapi.service.mapper.response;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.type.TypeMapper;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.ballerina.openapi.service.mapper.interceptors.Constants.CREATE_INTERCEPTORS_FUNC;
import static io.ballerina.openapi.service.mapper.interceptors.Constants.INTERCEPTABLE_SERVICE;

/**
 * This {@link DynamicResponseMapper} class is the implementation of the {@link ResponseMapper} interface
 * which supports dynamic response mapping functionality for both Ballerina HTTP interceptable-service or Ballerina
 * HTTP service responses into OAS responses.
 *
 * @since 1.9.0
 */
public class DynamicResponseMapper implements ResponseMapper {
    private final ResponseMapper responseMapper;

    public DynamicResponseMapper(TypeMapper typeMapper, SemanticModel semanticModel,
                                 ServiceDeclarationNode serviceNode) {
        this.responseMapper = getResponseMapper(typeMapper, semanticModel, serviceNode);
    }

    private ResponseMapper getResponseMapper(TypeMapper typeMapper, SemanticModel semanticModel,
                                             ServiceDeclarationNode serviceNode) {
        if (isInterceptableSvcType(serviceNode)) {
            Optional<ReturnTypeDescriptorNode> returnTypes = getCreateInterceptorsFunction(serviceNode.members())
                    .flatMap(createInterceptors -> createInterceptors.functionSignature().returnTypeDesc())
                    .filter(s -> SyntaxKind.TUPLE_TYPE_DESC.equals(s.type().kind()));
            if (returnTypes.isPresent()) {
                TupleTypeDescriptorNode interceptorTypes = (TupleTypeDescriptorNode) returnTypes.get().type();
                return new InterceptableSvcResponseMapper(typeMapper, semanticModel, interceptorTypes);
            }
        }
        return new DefaultResponseMapper(typeMapper, semanticModel);
    }

    private boolean isInterceptableSvcType(ServiceDeclarationNode serviceNode) {
        return serviceNode.typeDescriptor()
                .map(type -> type.toString().trim().equals(INTERCEPTABLE_SERVICE))
                .orElse(false);
    }

    private Optional<FunctionDefinitionNode> getCreateInterceptorsFunction(NodeList<Node> memberNodes) {
        return memberNodes.stream()
                .filter(function -> function instanceof FunctionDefinitionNode)
                .map(function -> (FunctionDefinitionNode) function)
                .filter(this::hasCreateInterceptorsFunction)
                .findFirst();
    }

    private boolean hasCreateInterceptorsFunction(FunctionDefinitionNode function) {
        return StreamSupport.stream(function.children().spliterator(), false)
                .anyMatch(child -> child.toString().equals(CREATE_INTERCEPTORS_FUNC));
    }

    @Override
    public void setApiResponses(OperationInventory operationInventory, FunctionDefinitionNode resource) {
        this.responseMapper.setApiResponses(operationInventory, resource);
    }
}
