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
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.service.mapper.type.TypeMapper;

import java.util.List;
import java.util.Optional;

/**
 * This {@link ResponseMapperImpl} class is the implementation of the {@link ResponseMapper} interface.
 * This class provides functionalities for mapping the Ballerina return type to OpenAPI
 * response.
 *
 * @since 1.9.0
 */
public class ResponseMapperImpl extends AbstractResponseMapper {

    public ResponseMapperImpl(TypeMapper typeMapper, SemanticModel semanticModel) {
        super(typeMapper, semanticModel);
    }

    @Override
    List<TypeSymbol> getReturnTypes(FunctionDefinitionNode resource) {
        Optional<Symbol> symbol = semanticModel.symbol(resource);
        if (symbol.isEmpty() || !(symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol)) {
            return List.of();
        }
        Optional<TypeSymbol> returnTypeOpt = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
        return returnTypeOpt.map(List::of).orElseGet(List::of);
    }
}
