package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResponseInterceptor extends Interceptor {

    public ResponseInterceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                               ModuleMemberVisitor moduleMemberVisitor) {
        super(typeSymbol, semanticModel, moduleMemberVisitor);
    }

    @Override
    protected void extractInterceptorDetails(SemanticModel semanticModel) {
        Map<String, MethodSymbol> serviceMethods = serviceClass.methods();
        MethodSymbol remoteMethod = serviceMethods.get(getRemoteMethodName());
        if (Objects.isNull(remoteMethod)) {
            return;
        }
        Optional<TypeSymbol> optReturnType = remoteMethod.typeDescriptor().returnTypeDescriptor();
        optReturnType.ifPresent(this::setReturnType);
    }

    @Override
    public boolean isNotInvokable(ResourceMethodSymbol targetResource) {
        return true;
    }

    @Override
    public InterceptorType getType() {
        return InterceptorType.RESPONSE;
    }

    @Override
    public boolean isContinueExecution() {
        return continueExecution || hasNilReturn;
    }

    protected String getRemoteMethodName() {
        return "interceptResponse";
    }

    @Override
    public void setNextInReqPath(Interceptor nextInReqPath) {
        super.setNextInReqPath(null);
    }
}
