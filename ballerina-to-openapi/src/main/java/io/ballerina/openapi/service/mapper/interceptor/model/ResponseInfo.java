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
package io.ballerina.openapi.service.mapper.interceptor.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This {@link ResponseInfo} class represents the response information collected from the interceptors.
 *
 * @since 1.9.0
 */
public class ResponseInfo {

    private final Set<TypeSymbol> returnTypesFromInterceptors;
    private final Set<TypeSymbol> returnTypesFromTargetResource;
    private boolean hasUnhandledDataBindingErrors;

    public ResponseInfo(boolean hasDataBinding) {
        returnTypesFromInterceptors = new LinkedHashSet<>();
        returnTypesFromTargetResource = new LinkedHashSet<>();
        hasUnhandledDataBindingErrors = hasDataBinding;
    }

    public void markDataBindingError() {
        hasUnhandledDataBindingErrors = true;
    }

    public void markDataBindingErrorHandled() {
        hasUnhandledDataBindingErrors = false;
    }

    public boolean hasUnhandledDataBindingErrors() {
        return hasUnhandledDataBindingErrors;
    }

    public void addReturnTypeFromInterceptors(TypeSymbol typeSymbol) {
        returnTypesFromInterceptors.add(typeSymbol);
    }

    public void addReturnTypeFromTargetResource(TypeSymbol typeSymbol) {
        returnTypesFromTargetResource.add(typeSymbol);
    }

    public boolean hasReturnTypesFromInterceptors() {
        return !returnTypesFromInterceptors.isEmpty();
    }

    public TypeSymbol getReturnTypesFromInterceptors(SemanticModel semanticModel) {
        return buildTypeSymbolFromList(returnTypesFromInterceptors, semanticModel);
    }

    public TypeSymbol getReturnTypesFromTargetResource(SemanticModel semanticModel) {
        return buildTypeSymbolFromList(returnTypesFromTargetResource, semanticModel);
    }

    private static TypeSymbol buildTypeSymbolFromList(Set<TypeSymbol> typeSymbols, SemanticModel semanticModel) {
        if (Objects.isNull(typeSymbols) || typeSymbols.isEmpty()) {
            return null;
        }
        if (typeSymbols.size() == 1) {
            return typeSymbols.iterator().next();
        }
        return semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                typeSymbols.toArray(TypeSymbol[]::new)).build();
    }
}
