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
package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.Objects;
import java.util.Set;

/**
 * This {@link ReturnTypes} record stores the return types from the interceptors and the target resource.
 * @param fromInterceptors - The return types from the interceptors.
 * @param fromTargetResource - The return types from the target resource.
 *
 * @since 1.9.0
 */
public record ReturnTypes(Set<TypeSymbol> fromInterceptors, Set<TypeSymbol> fromTargetResource) {

    public TypeSymbol getTypeSymbolFromInterceptors(SemanticModel semanticModel) {
        return buildTypeSymbolFromList(fromInterceptors, semanticModel);
    }

    public TypeSymbol getTypeSymbolFromTargetResource(SemanticModel semanticModel) {
        return buildTypeSymbolFromList(fromTargetResource, semanticModel);
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
