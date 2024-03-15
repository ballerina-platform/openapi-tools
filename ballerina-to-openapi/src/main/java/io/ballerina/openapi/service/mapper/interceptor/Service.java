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
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.openapi.service.mapper.response.utils.StatusCodeResponseUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link Service} class represents the abstract service.
 *
 * @since 1.9.0
 */
public abstract class Service {

    private final List<TypeSymbol> errorReturnType = new ArrayList<>();
    private final List<TypeSymbol> nonErrorReturnType = new ArrayList<>();
    protected final SemanticModel semanticModel;

    protected Service(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    protected void extractErrorAndNonErrorReturnTypes(TypeSymbol effectiveReturnType) {
        List<TypeSymbol> baseTypes = new ArrayList<>();
        destructUnionType(effectiveReturnType, baseTypes);
        baseTypes.forEach(this::addBaseReturnType);
    }

    private void addBaseReturnType(TypeSymbol typeSymbol) {
        if (typeSymbol.subtypeOf(semanticModel.types().ERROR)) {
            errorReturnType.add(typeSymbol);
        } else {
            nonErrorReturnType.add(typeSymbol);
        }
    }

    private void destructUnionType(TypeSymbol typeSymbol, List<TypeSymbol> memberTypes) {
        if (typeSymbol.subtypeOf(semanticModel.types().ERROR) ||
                typeSymbol.subtypeOf(semanticModel.types().ANYDATA) ||
                StatusCodeResponseUtils.isSubTypeOfHttpStatusCodeResponse(typeSymbol, semanticModel)) {
            memberTypes.add(typeSymbol);
        } else if (typeSymbol instanceof UnionTypeSymbol) {
            ((UnionTypeSymbol) typeSymbol).userSpecifiedMemberTypes().forEach(
                    memberType -> destructUnionType(memberType, memberTypes));
        } else if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            destructUnionType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), memberTypes);
        } else {
            memberTypes.add(typeSymbol);
        }
    }

    public TypeSymbol getErrorReturnType() {
        return getTypeSymbol(errorReturnType);
    }

    public TypeSymbol getNonErrorReturnType() {
        return getTypeSymbol(nonErrorReturnType);
    }

    private TypeSymbol getTypeSymbol(List<TypeSymbol> listOfTypeSymbols) {
        if (listOfTypeSymbols.isEmpty()) {
            return null;
        }
        if (listOfTypeSymbols.size() == 1) {
            return listOfTypeSymbols.get(0);
        }
        return semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                listOfTypeSymbols.toArray(TypeSymbol[]::new)).build();
    }

    public boolean hasErrorReturn() {
        return !errorReturnType.isEmpty();
    }
}
