package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TargetResource {

    private final TypeSymbol effectiveReturnType;
    private TypeSymbol errorReturnType = null;
    private TypeSymbol nonErrorReturnType = null;
    private final SemanticModel semanticModel;

    public TargetResource(ResourceMethodSymbol resourceMethodSymbol, SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        Optional<TypeSymbol> optReturnType = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
        if (optReturnType.isEmpty()) {
            effectiveReturnType = semanticModel.types().NIL;
            return;
        }
        effectiveReturnType = optReturnType.get();
        extractErrorAndNonErrorReturnTypes(effectiveReturnType);
    }

    private void extractErrorAndNonErrorReturnTypes(TypeSymbol effectiveReturnType) {
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
            errorReturnType = errorTypes.isEmpty() ? null : semanticModel.types().builder().UNION_TYPE
                    .withMemberTypes(errorTypes.toArray(TypeSymbol[]::new)).build();
            nonErrorReturnType = nonErrorTypes.isEmpty() ? null : semanticModel.types().builder().UNION_TYPE
                    .withMemberTypes(nonErrorTypes.toArray(TypeSymbol[]::new)).build();
        } else {
            if (effectiveReturnType.subtypeOf(semanticModel.types().ERROR)) {
                errorReturnType = effectiveReturnType;
            } else {
                nonErrorReturnType = effectiveReturnType;
            }
        }
    }

    public TypeSymbol getEffectiveReturnType() {
        return effectiveReturnType;
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
