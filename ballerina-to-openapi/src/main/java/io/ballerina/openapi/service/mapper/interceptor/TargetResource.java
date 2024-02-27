package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.Optional;

public class TargetResource extends Service {

    private final TypeSymbol effectiveReturnType;

    public TargetResource(ResourceMethodSymbol resourceMethodSymbol, SemanticModel semanticModel) {
        super(semanticModel);
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
}
