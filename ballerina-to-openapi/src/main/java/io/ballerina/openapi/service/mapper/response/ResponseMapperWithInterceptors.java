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
package io.ballerina.openapi.service.mapper.response;

import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.service.mapper.ServiceMapperFactory;
import io.ballerina.openapi.service.mapper.interceptor.InterceptorPipeline;
import io.ballerina.openapi.service.mapper.interceptor.ReturnTypes;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;

import java.util.Optional;

/**
 * This {@link ResponseMapperWithInterceptors} class is the implementation of the {@link ResponseMapper} interface.
 * This class provides functionalities for mapping the Ballerina return type to OpenAPI response when the service
 * has interceptors.
 *
 * @since 1.9.0
 */
public class ResponseMapperWithInterceptors extends DefaultResponseMapper {

    private final ReturnTypes returnTypes;

    public ResponseMapperWithInterceptors(FunctionDefinitionNode resourceNode, OperationInventory operationInventory,
                                          AdditionalData additionalData, InterceptorPipeline interceptorPipeline,
                                          ServiceMapperFactory serviceMapperFactory) {
        super(resourceNode, operationInventory, additionalData, serviceMapperFactory);
        Optional<Symbol> symbol = additionalData.semanticModel().symbol(resourceNode);
        if (symbol.isPresent() && symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol) {
            returnTypes = interceptorPipeline.getEffectiveReturnType(resourceMethodSymbol);
        } else {
            returnTypes = new ReturnTypes(null, null);
        }
    }

    @Override
    protected TypeSymbol getReturnTypeSymbol(FunctionDefinitionNode resourceNode) {
        return returnTypes.getTypeSymbolFromTargetResource(semanticModel);
    }

    @Override
    protected void createResponseMapping(TypeSymbol returnType, String defaultStatusCode) {
        if (!returnTypes.fromInterceptors().isEmpty()) {
            super.createResponseMapping(returnTypes.getTypeSymbolFromInterceptors(semanticModel), defaultStatusCode);
        }
        super.createResponseMapping(returnType, defaultStatusCode);
    }
}
