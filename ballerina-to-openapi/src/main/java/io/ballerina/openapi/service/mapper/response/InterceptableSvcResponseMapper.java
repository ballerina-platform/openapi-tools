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
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static io.ballerina.openapi.service.mapper.interceptors.Constants.INTERCEPTABLE_SERVICE;
import static io.ballerina.openapi.service.mapper.interceptors.Constants.CREATE_INTERCEPTORS_FUNC;

/**
 * This {@link InterceptableSvcResponseMapper} class is the implementation of the {@link ResponseMapper} interface
 * which supports mapping Ballerina HTTP interceptable-service responses into OAS responses.
 *
 * @since 1.9.0
 */
public class InterceptableSvcResponseMapper extends AbstractResponseMapper {
    private final ServiceDeclarationNode serviceNode;
    private final List<Interceptor> interceptorPipeline = new ArrayList<>();

    public InterceptableSvcResponseMapper(TypeMapper typeMapper, SemanticModel semanticModel,
                                          ServiceDeclarationNode serviceNode) {
        super(typeMapper, semanticModel);
        this.serviceNode = serviceNode;
    }

    // Find out a given service is an interceptable service
    //  if (true)
    //      - get the return type of create interceptors function and construct the interceptor pipeline
    //      - for individual resource function, get the relevant return types by evaluating interceptor pipeline
    // else
    //      - call the default response mapper

    // todo: implement this properly
    @Override
    public List<TypeSymbol> getReturnTypes(FunctionDefinitionNode resource) {
        return List.of();
    }

    public void initializeResponseMapper(FunctionDefinitionNode resource) {
        if (isInterceptable()) {
            Optional<FunctionDefinitionNode> interceptorFunction = findCreateInterceptorsFunction(serviceNode.members());
            if (supportsTupleType(interceptorFunction.get())) {
                this.interceptorPipeline.addAll(getInterceptorPipeline(interceptorFunction.get()));
                effectiveInterceptorReturnTypes(resource);
            }
        } else {
            new DefaultResponseMapper(this.typeMapper, this.semanticModel);
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
//        Object returnInterceptors = resource.functionSignature()
//                .returnTypeDesc()
//                .map(ReturnTypeDescriptorNode::type).get();
        return List.of();
    }

    // todo: implement this method properly
    private List<TypeSymbol> effectiveInterceptorReturnTypes(FunctionDefinitionNode resource) {
        TypeSymbol typeSymbol = (TypeSymbol) (((ResourceMethodSymbol)semanticModel.symbol(resource).get()).typeDescriptor());
        return List.of(typeSymbol);
    }
}
