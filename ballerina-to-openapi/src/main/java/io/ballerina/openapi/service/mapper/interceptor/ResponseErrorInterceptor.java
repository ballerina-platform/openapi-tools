package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

public class ResponseErrorInterceptor extends ResponseInterceptor {
    public ResponseErrorInterceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                                    ModuleMemberVisitor moduleMemberVisitor) {
        super(typeSymbol, semanticModel, moduleMemberVisitor);
    }

    @Override
    public InterceptorType getType() {
        return InterceptorType.RESPONSE_ERROR;
    }

    @Override
    protected String getRemoteMethodName() {
        return "interceptResponseError";
    }
}
