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
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.Optional;

/**
 * This {@link TargetResource} class represents the target resource service.
 *
 * @since 1.9.0
 */
public class TargetResource extends Service {

    private final TypeSymbol effectiveReturnType;
    private final boolean hasDataBinding;
    private final ResourceMethodSymbol resourceMethodSymbol;

    public TargetResource(ResourceMethodSymbol resourceMethodSymbol, boolean hasDataBinding,
                          SemanticModel semanticModel) {
        super(semanticModel);
        this.hasDataBinding = hasDataBinding;
        this.resourceMethodSymbol = resourceMethodSymbol;
        Optional<TypeSymbol> optReturnType = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
        if (optReturnType.isEmpty()) {
            effectiveReturnType = semanticModel.types().NIL;
            return;
        }
        effectiveReturnType = optReturnType.get();
        extractErrorAndNonErrorReturnTypes(effectiveReturnType);
    }

    public TypeSymbol getEffectiveReturnType() {
        return effectiveReturnType;
    }

    @Override
    public boolean hasErrorReturn() {
        return hasDataBinding || super.hasErrorReturn();
    }

    public ResourceMethodSymbol getResourceMethodSymbol() {
        return resourceMethodSymbol;
    }
}
