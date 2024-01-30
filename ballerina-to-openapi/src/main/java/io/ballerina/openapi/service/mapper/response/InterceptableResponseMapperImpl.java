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
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerinalang.model.symbols.TypeSymbol;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.ballerina.openapi.service.mapper.interceptors.Constants.INTERCEPTABLE_SERVICE;
import static io.ballerina.openapi.service.mapper.interceptors.Constants.CREATE_INTERCEPTORS_FUNC;

public class InterceptableResponseMapperImpl implements ResponseMapper {
    private final ApiResponses apiResponses = new ApiResponses();
    private final OperationInventory operationInventory;
    private final SemanticModel semanticModel;
    private final ServiceDeclarationNode serviceNode;
    private final Components components;
    private final AdditionalData additionalData;

    public InterceptableResponseMapperImpl(ServiceDeclarationNode serviceNode, FunctionDefinitionNode resource,
                                           OperationInventory operationInventory, Components components,
                                           SemanticModel semanticModel, AdditionalData additionalData) {
        this.operationInventory = operationInventory;
        this.semanticModel = semanticModel;
        this.serviceNode = serviceNode;
        this.components = components;
        this.additionalData = additionalData;
        initializeResponseMapper(resource);
    }

    // Find out a given service is an interceptable service
    //  if (true)
    //      - get the return type of create interceptors function and construct the interceptor pipeline
    //      - for individual resource function, get the relevant return types by evaluating interceptor pipeline
    // else
    //      - call the default response mapper

    @Override
    public void setApiResponses() {
        operationInventory.setApiResponses(apiResponses);
    }

    @Override
    public void initializeResponseMapper(FunctionDefinitionNode resource) {
        if (isInterceptable()) {
            Optional<FunctionDefinitionNode> interceptorFunction = findCreateInterceptorsFunction(serviceNode.members());
            if (supportsTupleType(interceptorFunction.get())) {
                getInterceptorPipeline(interceptorFunction.get());
                effectiveInterceptorReturnTypes(resource);
            }
        } else {
            new ResponseMapperImpl(resource, operationInventory, components, additionalData);
        }
    }

    private boolean isInterceptable() {
        if (isInterceptableServiceType()) {
            return findCreateInterceptorsFunction(serviceNode.members()).isPresent();
        }
        return false;
    }

    private boolean isInterceptableServiceType() {
        return serviceNode.typeDescriptor()
                .map(type -> type.toString().trim().equals(INTERCEPTABLE_SERVICE))
                .orElse(false);
    }

    private Optional<FunctionDefinitionNode> findCreateInterceptorsFunction(NodeList<Node> functions) {
        return functions.stream()
                .filter(function -> function instanceof FunctionDefinitionNode)
                .map(function -> (FunctionDefinitionNode) function)
                .filter(this::hasCreateInterceptorsFunction)
                .findFirst();
    }

    private boolean hasCreateInterceptorsFunction(FunctionDefinitionNode function) {
        return StreamSupport.stream(function.children().spliterator(), false)
                .anyMatch(child -> child.toString().equals(CREATE_INTERCEPTORS_FUNC));
    }

    private boolean supportsTupleType(FunctionDefinitionNode resource) {
        return resource.functionSignature()
                .returnTypeDesc()
                .map(returnType -> returnType.type().kind().equals(SyntaxKind.TUPLE_TYPE_DESC))
                .orElse(false);
    }

    // todo: implement this method properly
    private List<Interceptor> getInterceptorPipeline(FunctionDefinitionNode resource) {
        Object returnInterceptors = resource.functionSignature()
                .returnTypeDesc()
                .map(ReturnTypeDescriptorNode::type).get();
        return List.of();
    }

    // todo: implement this method properly
    private List<TypeSymbol> effectiveInterceptorReturnTypes(FunctionDefinitionNode resource) {
        TypeSymbol typeSymbol = (TypeSymbol) (((ResourceMethodSymbol)semanticModel.symbol(resource).get()).typeDescriptor());
        return List.of();
    }
}
