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
package io.ballerina.openapi.service.mapper.interceptor.types;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.interceptor.InterceptorMapperException;
import io.ballerina.openapi.service.mapper.interceptor.resource.ResourceMatcher;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.Map;
import java.util.Optional;

/**
 * This {@link RequestInterceptor} class represents the request interceptor service.
 *
 * @since 1.9.0
 */
public class RequestInterceptor extends Interceptor {

    public RequestInterceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                              ModuleMemberVisitor moduleMemberVisitor) throws InterceptorMapperException {
        super(typeSymbol, semanticModel, moduleMemberVisitor);
    }

    @Override
    protected void extractInterceptorDetails(SemanticModel semanticModel) {
        Map<String, MethodSymbol> serviceMethods = serviceClass.methods();
        Optional<ResourceMethodSymbol> resourceMethodOpt = serviceMethods.values().stream()
                .filter(methodSymbol -> methodSymbol instanceof ResourceMethodSymbol)
                .map(methodSymbol -> (ResourceMethodSymbol) methodSymbol)
                .findFirst();
        if (resourceMethodOpt.isEmpty()) {
            return;
        }
        ResourceMethodSymbol resourceMethod = resourceMethodOpt.get();
        Optional<TypeSymbol> optReturnType = resourceMethod.typeDescriptor().returnTypeDescriptor();
        this.resourceMethod = resourceMethod;
        optReturnType.ifPresent(this::setReturnType);
    }

    @Override
    public InterceptorType getType() {
        return InterceptorType.REQUEST;
    }

    @Override
    public boolean isInvokable(TargetResource targetResource) {
        return ResourceMatcher.match(resourceMethod, targetResource.getResourceMethodSymbol(), semanticModel);
    }

    @Override
    protected FunctionDefinitionNode getFunctionDefinitionNode() {
        return (FunctionDefinitionNode) serviceClassNode.members().stream().filter(
                node -> node.kind().equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)
        ).findFirst().orElse(null);
    }
}
