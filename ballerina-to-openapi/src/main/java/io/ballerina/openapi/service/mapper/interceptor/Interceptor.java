package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.utils.MediaTypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Interceptor {

    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    private final String name;
    private TypeSymbol nonErrorReturnType = null;
    private TypeSymbol errorReturnType = null;
    protected final ClassSymbol serviceClass;
    public ClassDefinitionNode serviceClassNode = null;
    protected final SemanticModel semanticModel;
    protected ResourceMethodSymbol resourceMethod = null;
    protected boolean continueExecution = false;
    protected boolean hasNilReturn = false;
    private Interceptor nextInReqPath = null;
    protected Interceptor nextInResPath = null;

    public Interceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                       ModuleMemberVisitor moduleMemberVisitor) {
        this.semanticModel = semanticModel;
        this.name = typeSymbol.getName().orElse("");
        typeSymbol.getName().ifPresent(
                name -> this.serviceClassNode = moduleMemberVisitor.getInterceptorServiceClassNode(name));
        this.serviceClass = typeSymbol.typeDescriptor() instanceof ClassSymbol ?
                (ClassSymbol) typeSymbol.typeDescriptor() : null;
        extractInterceptorDetails(semanticModel);
    }

    protected abstract void extractInterceptorDetails(SemanticModel semanticModel);

    public boolean isInvokableFor(ResourceMethodSymbol targetResource) {
        return true;
    }

    protected void setReturnType(TypeSymbol returnType) {
        hasNilReturn = semanticModel.types().NIL.subtypeOf(returnType);
        TypeSymbol effectiveReturnType;
        if (isSubTypeOfDefaultInterceptorReturnType(returnType, semanticModel)) {
            continueExecution = true;
            effectiveReturnType = getEffectiveReturnType(returnType, semanticModel);
        } else {
            effectiveReturnType = returnType;
        }

        if (Objects.isNull(effectiveReturnType)) {
            return;
        }

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

    private boolean isSubTypeOfDefaultInterceptorReturnType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName("ballerina", "http",
                "", "NextService");
        if (optNextServiceType.isEmpty() ||
                !(optNextServiceType.get() instanceof TypeDefinitionSymbol nextServiceType)) {
            return false;
        }
        UnionTypeSymbol defaultInterceptorReturnType = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                nextServiceType.typeDescriptor(), semanticModel.types().NIL).build();
        return defaultInterceptorReturnType.subtypeOf(typeSymbol);
    }

    private boolean isSubTypeOfHttpNextServiceType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName("ballerina", "http",
                "", "NextService");
        if (optNextServiceType.isEmpty() ||
                !(optNextServiceType.get() instanceof TypeDefinitionSymbol nextServiceType)) {
            return false;
        }
        return typeSymbol.subtypeOf(nextServiceType.typeDescriptor());
    }

    private TypeSymbol getEffectiveReturnType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        if (isSubTypeOfHttpNextServiceType(typeSymbol, semanticModel) ||
                typeSymbol.subtypeOf(semanticModel.types().NIL)) {
            return null;
        }

        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            return getEffectiveReturnType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), semanticModel);
        } else if (typeSymbol instanceof UnionTypeSymbol) {
            if (MediaTypeUtils.isSameMediaType(typeSymbol, semanticModel)) {
                return typeSymbol;
            }
            List<TypeSymbol> memberTypes = ((UnionTypeSymbol) typeSymbol).userSpecifiedMemberTypes();
            List<TypeSymbol> effectiveMemberTypes = memberTypes.stream()
                    .map(memberType -> getEffectiveReturnType(memberType, semanticModel))
                    .filter(Objects::nonNull)
                    .toList();
            if (effectiveMemberTypes.isEmpty()) {
                return null;
            } else if (effectiveMemberTypes.size() == 1) {
                return effectiveMemberTypes.get(0);
            }
            return semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                    effectiveMemberTypes.toArray(TypeSymbol[]::new)).build();
        }
        return typeSymbol;
    }

    public String getName() {
        return name;
    }

    public TypeSymbol getNonErrorReturnType() {
        return nonErrorReturnType;
    }

    public abstract InterceptorType getType();

    public boolean isContinueExecution() {
        return continueExecution;
    }

    public boolean hasErrorReturn() {
        return Objects.nonNull(errorReturnType);
    }

    public TypeSymbol getErrorReturnType() {
        return errorReturnType;
    }

    public ClassDefinitionNode getServiceClassNode() {
        return serviceClassNode;
    }

    public void setNextInReqPath(Interceptor nextInReqPath) {
        this.nextInReqPath = nextInReqPath;
    }

    public void setNextInResPath(Interceptor nextInResPath) {
        this.nextInResPath = nextInResPath;
    }

    public Interceptor getNextInReqErrorPath() {
        if (Objects.isNull(nextInReqPath) || nextInReqPath.getType().equals(InterceptorType.REQUEST_ERROR)) {
            return nextInReqPath;
        }
        return nextInReqPath.getNextInReqErrorPath();
    }

    public Interceptor getNextInResErrorPath() {
        if (Objects.isNull(nextInResPath) || nextInResPath.getType().equals(InterceptorType.RESPONSE_ERROR)) {
            return nextInResPath;
        }
        return nextInResPath.getNextInResErrorPath();
    }

    public Interceptor getNextInReqPath() {
        if (Objects.isNull(nextInReqPath) || nextInReqPath.getType().equals(InterceptorType.REQUEST)) {
            return nextInReqPath;
        }
        return nextInReqPath.getNextInReqPath();
    }

    public Interceptor getNextInResPath() {
        if (Objects.isNull(nextInResPath) || nextInResPath.getType().equals(InterceptorType.RESPONSE)) {
            return nextInResPath;
        }
        return nextInResPath.getNextInResPath();
    }
}
