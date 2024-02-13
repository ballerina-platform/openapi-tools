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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;

import java.util.List;
import java.util.Optional;

public class Interceptor {
    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    private final InterceptorType type;
    private String relativeResourcePath;
    private String resourceMethod;
    private TypeSymbol returnType;
    private List<PathSegment> pathSegments;
    private ClassSymbol serviceClass;

    public Interceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel) {
        this.serviceClass = typeSymbol.typeDescriptor() instanceof ClassSymbol ?
                (ClassSymbol) typeSymbol.typeDescriptor() : null;
        this.type = getInterceptorType(typeSymbol, semanticModel);
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

    public String getRelativeResourcePath() {
        return relativeResourcePath;
    }

    public void setRelativeResourcePath(String relativeResourcePath) {
        this.relativeResourcePath = relativeResourcePath;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    public void setReturnType(TypeSymbol returnType) {
        this.returnType = returnType;
    }
}