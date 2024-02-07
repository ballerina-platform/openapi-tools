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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MemberTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TupleTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.service.mapper.type.TypeMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This {@link InterceptableSvcResponseMapper} class is the implementation of the {@link ResponseMapper} interface
 * which supports mapping Ballerina HTTP interceptable-service responses into OAS responses.
 *
 * @since 1.9.0
 */
public class InterceptableSvcResponseMapper extends AbstractResponseMapper {
    private final List<Interceptor> interceptorPipeline;

    public InterceptableSvcResponseMapper(TypeMapper typeMapper, SemanticModel semanticModel,
                                          TupleTypeDescriptorNode interceptorTypes) {
        super(typeMapper, semanticModel);
        this.interceptorPipeline = constructInterceptorPipeline(interceptorTypes);
    }

    private List<Interceptor> constructInterceptorPipeline(TupleTypeDescriptorNode interceptorTypes) {
        List<Interceptor> interceptors = new ArrayList<>();
        for (Node member: interceptorTypes.memberTypeDesc()) {
            TypeDescriptorNode memberTypeDescriptor = ((MemberTypeDescriptorNode) member).typeDescriptor();
            Optional<Symbol> memberTypeSymbol = semanticModel.symbol(memberTypeDescriptor);
            if (memberTypeSymbol.isEmpty()) {
                continue;
            }
            TypeSymbol memberType = ((TypeReferenceTypeSymbol) memberTypeSymbol.get()).typeDescriptor();
            if (!SymbolKind.CLASS.equals(memberType.kind())) {
                continue;
            }
            ClassSymbol classSymbol = (ClassSymbol) memberType;
            Optional<Interceptor.InterceptorType> interceptorType = getInterceptorType(classSymbol);
            if (interceptorType.isEmpty()) {
                continue;
            }
            MethodSymbol[] interceptorMethods = classSymbol.methods().values()
                    .toArray(new MethodSymbol[]{});
            for (MethodSymbol method: interceptorMethods) {
                if (SymbolKind.RESOURCE_METHOD.equals(method.kind())) {
                    ResourceMethodSymbol resourceMethod = (ResourceMethodSymbol) method;
                    FunctionTypeSymbol functionTypeSymbol = resourceMethod.typeDescriptor();
                    List<TypeSymbol> returnTypes = extractReturnTypes(functionTypeSymbol);
                    if (returnTypes.isEmpty()) {
                        continue;
                    }
                    Interceptor interceptor = new Interceptor(interceptorType.get());
                    interceptor.setResourceMethod(resourceMethod.getName().orElse("").trim());
                    interceptor.setRelativeResourcePath(getRelativeResourcePth(resourceMethod));
                    interceptor.setReturnTypes(returnTypes);
                    interceptors.add(interceptor);
                    continue;
                }
                String methodName = method.getName().orElse("");
                if (!"interceptResponse".equals(methodName) && !"interceptResponseError".equals(methodName)) {
                    continue;
                }
                FunctionTypeSymbol functionTypeSymbol = method.typeDescriptor();
                List<TypeSymbol> returnTypes = extractReturnTypes(functionTypeSymbol);
                Interceptor interceptor = new Interceptor(interceptorType.get());
                interceptor.setReturnTypes(returnTypes);
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }

    private Optional<Interceptor.InterceptorType> getInterceptorType(ClassSymbol classSymbol) {
        List<TypeSymbol> availableTypeInclusions = classSymbol.typeInclusions();
        if (availableTypeInclusions.isEmpty()) {
            return Optional.empty();
        }
        for (TypeSymbol type: availableTypeInclusions) {
            if (type instanceof TypeReferenceTypeSymbol typeRefSymbol) {
                Symbol definition = typeRefSymbol.definition();
                if (SymbolKind.TYPE_DEFINITION.equals(definition.kind())) {
                    String qualifiedTypeName = ((TypeDefinitionSymbol) definition).moduleQualifiedName();
                    if ("http:RequestInterceptor".equals(qualifiedTypeName)) {
                        return Optional.of(Interceptor.InterceptorType.REQUEST);
                    } else if ("http:RequestErrorInterceptor".equals(qualifiedTypeName)) {
                        return Optional.of(Interceptor.InterceptorType.REQUEST_ERROR);
                    } else if ("http:ResponseInterceptor".equals(qualifiedTypeName)) {
                        return Optional.of(Interceptor.InterceptorType.RESPONSE);
                    } else if ("http:ResponseErrorInterceptor".equals(qualifiedTypeName)) {
                        return Optional.of(Interceptor.InterceptorType.RESPONSE_ERROR);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private List<TypeSymbol> extractReturnTypes(FunctionTypeSymbol functionSymbol) {
        Optional<TypeSymbol> returnTypeOpt = functionSymbol.returnTypeDescriptor();
        if (returnTypeOpt.isEmpty()) {
            return Collections.emptyList();
        }
        TypeSymbol returnType = returnTypeOpt.get();
        if (TypeDescKind.UNION.equals(returnType.typeKind())) {
            return ((UnionTypeSymbol) returnType).memberTypeDescriptors().stream()
                    .filter(rt -> !isSubtypeOfHttpNextService(rt))
                    .collect(Collectors.toList());
        } else {
            if (!isSubtypeOfHttpNextService(returnType)) {
                return List.of(returnType);
            }
        }
        return Collections.emptyList();
    }

    private String getRelativeResourcePth(ResourceMethodSymbol resource) {
        return resource.resourcePath().signature();
    }

    private boolean isSubtypeOfHttpNextService(TypeSymbol typeSymbol) {
        Optional<Symbol> nextSvcTypeOpt = semanticModel.types()
                .getTypeByName("ballerina", "http", "", "NextService");
        if (nextSvcTypeOpt.isEmpty() || !(nextSvcTypeOpt.get() instanceof TypeDefinitionSymbol)) {
            return false;
        }
        TypeSymbol httpNextSvcType = ((TypeDefinitionSymbol) nextSvcTypeOpt.get()).typeDescriptor();
        return typeSymbol.subtypeOf(httpNextSvcType);
    }
    
    @Override
    List<TypeSymbol> getReturnTypes(FunctionDefinitionNode resource) {
        Optional<Symbol> symbol = semanticModel.symbol(resource);
        if (symbol.isEmpty() || !(symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol)) {
            return List.of();
        }
        String relativeResourcePath = resourceMethodSymbol.resourcePath().signature();
        String httpMethod = resource.functionName().toString().trim();
        Set<TypeSymbol> returnTypes = new HashSet<>();
        for (Interceptor interceptor: interceptorPipeline) {
            if (Interceptor.InterceptorType.REQUEST.equals(interceptor.getType())) {
                if (!relativeResourcePath.startsWith(interceptor.getRelativeResourcePath())) {
                    continue;
                }
                if (!httpMethod.equals(interceptor.getResourceMethod())) {
                    continue;
                }
            }
            // todo: identify the logic to be used for request-error interceptor
            if (Interceptor.InterceptorType.RESPONSE.equals(interceptor.getType())) {
                boolean hasNillableReturnType = interceptor.getReturnTypes().stream()
                        .anyMatch(t -> t.subtypeOf(semanticModel.types().NIL));
                if (!hasNillableReturnType) {
                    returnTypes.addAll(interceptor.getReturnTypes());
                    return returnTypes.stream().toList();
                }
            }
            // todo: identify the logic to be used for response-error interceptor
            returnTypes.addAll(interceptor.getReturnTypes());
        }
        resourceMethodSymbol.typeDescriptor().returnTypeDescriptor().ifPresent(returnTypes::add);
        return returnTypes.stream().toList();
    }
}
