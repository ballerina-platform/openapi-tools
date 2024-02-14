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

package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Interceptor {
    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    private final InterceptorType type;
    private String resourceMethod;
    private TypeSymbol returnType;
    private ResourcePath resourcePath = null;
    private ClassSymbol serviceClass;
    private ClassDefinitionNode serviceClassNode = null;

    public Interceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                       ModuleMemberVisitor moduleMemberVisitor) {
        typeSymbol.getName().ifPresent(
                name -> this.serviceClassNode = moduleMemberVisitor.getInterceptorServiceClassNode(name));
        this.serviceClass = typeSymbol.typeDescriptor() instanceof ClassSymbol ?
                (ClassSymbol) typeSymbol.typeDescriptor() : null;
        this.type = getInterceptorType(typeSymbol, semanticModel);
        extractInterceptorDetails(semanticModel);
    }

    private void extractInterceptorDetails(SemanticModel semanticModel) {
        if (Objects.isNull(type)) {
            return;
        }

        Map<String, MethodSymbol> serviceMethods = serviceClass.methods();
        Optional<TypeSymbol> optReturnType;
        if (type.equals(InterceptorType.REQUEST) || type.equals(InterceptorType.REQUEST_ERROR)) {
            // Request and Request Error Interceptors has only one resource method`
            Optional<ResourceMethodSymbol> resourceMethodOpt = serviceMethods.values().stream()
                    .filter(methodSymbol -> methodSymbol instanceof ResourceMethodSymbol)
                    .map(methodSymbol -> (ResourceMethodSymbol) methodSymbol)
                    .findFirst();
            if (resourceMethodOpt.isEmpty()) {
                return;
            }
            ResourceMethodSymbol resourceMethod = resourceMethodOpt.get();
            optReturnType = resourceMethod.typeDescriptor().returnTypeDescriptor();
            this.resourcePath = resourceMethod.resourcePath();
        } else if (type.equals(InterceptorType.RESPONSE)){
            // Response Interceptor has a remote function called `interceptResponse`
            MethodSymbol remoteMethod = serviceMethods.get("interceptResponse");
            if (Objects.isNull(remoteMethod)) {
                return;
            }
            optReturnType = remoteMethod.typeDescriptor().returnTypeDescriptor();
        } else {
            // Response Error Interceptor has a remote function called `interceptResponseError`
            MethodSymbol remoteMethod = serviceMethods.get("interceptResponseError");
            if (Objects.isNull(remoteMethod)) {
                return;
            }
            optReturnType = remoteMethod.typeDescriptor().returnTypeDescriptor();
        }
        if (optReturnType.isEmpty()) {
            return;
        }
        if (isSubTypeOfDefaultInterceptorReturnType(optReturnType.get(), semanticModel)) {
            this.returnType = getEffectiveReturnType(optReturnType.get(), semanticModel);
        } else {
            this.returnType = optReturnType.get();
        }
    }

    private boolean isSubTypeOfDefaultInterceptorReturnType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName("ballerina", "http", "", "NextService");
        if (optNextServiceType.isEmpty() ||
                !(optNextServiceType.get() instanceof TypeDefinitionSymbol nextServiceType)) {
            return false;
        }
        UnionTypeSymbol defaultInterceptorReturnType = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                nextServiceType.typeDescriptor(), semanticModel.types().NIL).build();
        return defaultInterceptorReturnType.subtypeOf(typeSymbol);
    }

    private boolean isSubTypeOfHttpNextServiceType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName("ballerina", "http", "", "NextService");
        if (optNextServiceType.isEmpty() ||
                !(optNextServiceType.get() instanceof TypeDefinitionSymbol nextServiceType)) {
            return false;
        }
        return typeSymbol.subtypeOf(nextServiceType.typeDescriptor());
    }

    private TypeSymbol getEffectiveReturnType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        if (isSubTypeOfHttpNextServiceType(typeSymbol, semanticModel) ||
                typeSymbol.subtypeOf(semanticModel.types().NIL)) {
            return null;
        }

        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            return getEffectiveReturnType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), semanticModel);
        } else if (typeSymbol instanceof UnionTypeSymbol) {
            List<TypeSymbol> memberTypes = ((UnionTypeSymbol) typeSymbol).userSpecifiedMemberTypes();
            // Stream effective types and for each compute the effective type and filter out non-null types
            List<TypeSymbol> effectiveMemberTypes = memberTypes.stream()
                    .map(memberType -> getEffectiveReturnType(memberType, semanticModel))
                    .filter(Objects::nonNull)
                    .toList();
            if (effectiveMemberTypes.isEmpty()) {
                return null;
            } else if (effectiveMemberTypes.size() == 1) {
                return effectiveMemberTypes.get(0);
            }
            return semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                    effectiveMemberTypes.toArray(TypeSymbol[]::new)).build();
        }
        return typeSymbol;
    }

    private InterceptorType getInterceptorType(TypeSymbol interceptorType, SemanticModel semanticModel) {
        if (isSubTypeOf(interceptorType, "RequestInterceptor", semanticModel)) {
            return InterceptorType.REQUEST;
        } else if (isSubTypeOf(interceptorType, "RequestErrorInterceptor", semanticModel)) {
            return InterceptorType.REQUEST_ERROR;
        } else if (isSubTypeOf(interceptorType, "ResponseInterceptor", semanticModel)) {
            return InterceptorType.RESPONSE;
        } else if (isSubTypeOf(interceptorType, "ResponseErrorInterceptor", semanticModel)) {
            return InterceptorType.RESPONSE_ERROR;
        }
        return null;
    }

    private boolean isSubTypeOf(TypeSymbol typeSymbol, String typeName, SemanticModel semanticModel) {
        Optional<Symbol> optType = semanticModel.types().getTypeByName("ballerina", "http", "", typeName);
        if (optType.isEmpty() || !(optType.get() instanceof TypeDefinitionSymbol typeDef)) {
            return false;
        }
        return typeSymbol.subtypeOf(typeDef.typeDescriptor());
    }


    public Interceptor(InterceptorType type) {
        this.type = type;
    }

    public InterceptorType getType() {
        return type;
    }

    public String getResourceMethod() {
        return resourceMethod;
    }

    public void setResourceMethod(String resourceMethod) {
        this.resourceMethod = resourceMethod;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    public void setReturnType(TypeSymbol returnType) {
        this.returnType = returnType;
    }
}