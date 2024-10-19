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
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This {@link TargetResource} class represents the target resource service.
 *
 * @since 1.9.0
 */
public class TargetResource extends Resource {

    private final TypeSymbol effectiveReturnType;

    public TargetResource(ResourceMethodSymbol resourceMethodSymbol, SemanticModel semanticModel) {
        super(semanticModel);
        this.resourceMethod = resourceMethodSymbol;
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

    public ResourceMethodSymbol getResourceMethodSymbol() {
        return resourceMethod;
    }

    @Override
    public boolean hasDataBinding() {
        return super.hasDataBinding() || hasPathParameter(resourceMethod.resourcePath());
    }

    private boolean hasPathParameter(ResourcePath path) {
        return switch (path.kind()) {
            case DOT_RESOURCE_PATH, PATH_REST_PARAM -> false;
            case PATH_SEGMENT_LIST -> {
                List<PathParameterSymbol> pathParams = ((PathSegmentList) path).pathParameters();;
                yield Objects.nonNull(pathParams) && !pathParams.isEmpty();
            }
        };
    }
}
