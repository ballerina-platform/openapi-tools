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

import java.util.Objects;
import java.util.Optional;

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
        return returnTypes.fromTargetResource();
    }

    @Override
    protected void createResponseMapping(TypeSymbol returnType, String defaultStatusCode) {
        if (Objects.nonNull(returnTypes.fromInterceptors())) {
            super.createResponseMapping(returnTypes.fromInterceptors(), defaultStatusCode);
        }
        super.createResponseMapping(returnType, defaultStatusCode);
    }
}
