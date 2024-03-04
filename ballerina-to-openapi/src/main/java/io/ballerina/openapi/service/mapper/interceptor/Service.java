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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This {@link Service} class represents the abstract service.
 *
 * @since 1.9.0
 */
public abstract class Service {

    private TypeSymbol errorReturnType = null;
    private TypeSymbol nonErrorReturnType = null;
    protected final SemanticModel semanticModel;

    protected Service(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    protected void extractErrorAndNonErrorReturnTypes(TypeSymbol effectiveReturnType) {
        if (effectiveReturnType instanceof TypeReferenceTypeSymbol) {
            extractErrorAndNonErrorReturnTypes(((TypeReferenceTypeSymbol) effectiveReturnType).typeDescriptor());
        } else if (effectiveReturnType instanceof UnionTypeSymbol) {
            List<TypeSymbol> memberTypes = ((UnionTypeSymbol) effectiveReturnType).userSpecifiedMemberTypes();
            List<TypeSymbol> errorTypes = new ArrayList<>();
            List<TypeSymbol> nonErrorTypes = new ArrayList<>();

            memberTypes.forEach(type -> {
                if (type.subtypeOf(semanticModel.types().ERROR)) {
                    errorTypes.add(type);
                } else {
                    nonErrorTypes.add(type);
                }
            });
            errorReturnType = errorTypes.isEmpty() ? null :
                    errorTypes.size() == 1 ? errorTypes.get(0) : semanticModel.types().builder().UNION_TYPE
                    .withMemberTypes(errorTypes.toArray(TypeSymbol[]::new)).build();
            nonErrorReturnType = nonErrorTypes.isEmpty() ? null :
                    nonErrorTypes.size() == 1 ? nonErrorTypes.get(0) : semanticModel.types().builder().UNION_TYPE
                    .withMemberTypes(nonErrorTypes.toArray(TypeSymbol[]::new)).build();
        } else {
            if (effectiveReturnType.subtypeOf(semanticModel.types().ERROR)) {
                errorReturnType = effectiveReturnType;
            } else {
                nonErrorReturnType = effectiveReturnType;
            }
        }
    }

    public TypeSymbol getErrorReturnType() {
        return errorReturnType;
    }

    public TypeSymbol getNonErrorReturnType() {
        return nonErrorReturnType;
    }

    public boolean hasErrorReturn() {
        return Objects.nonNull(errorReturnType);
    }
}
