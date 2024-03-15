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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.NEXT_SERVICE;

/**
 * This {@link Interceptor} class represents the abstract interceptor service.
 *
 * @since 1.9.0
 */
public abstract class Interceptor extends Service {

    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    protected final ClassSymbol serviceClass;
    private final ClassDefinitionNode serviceClassNode;
    protected ResourceMethodSymbol resourceMethod = null;
    protected boolean continueExecution = false;
    protected boolean hasNilReturn = false;
    private Interceptor nextInReqPath = null;
    protected Interceptor nextInResPath = null;

    protected Interceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                          ModuleMemberVisitor moduleMemberVisitor) throws InterceptorMapperException {
        super(semanticModel);
        String name = typeSymbol.getName().orElse("");
        if (typeSymbol.getName().isPresent() &&
                moduleMemberVisitor.getInterceptorServiceClassNode(name).isPresent()) {
            this.serviceClassNode = moduleMemberVisitor.getInterceptorServiceClassNode(name).get();
        } else {
            throw new InterceptorMapperException("no class definition found for the interceptor: " + name +
                    " within the package. Make sure that the interceptor return type is defined with the specific" +
                    " interceptor class type rather than the generic `http:Interceptor` type and the specific " +
                    "interceptor class is defined within the package");
        }
        this.serviceClass = typeSymbol.typeDescriptor() instanceof ClassSymbol ?
                (ClassSymbol) typeSymbol.typeDescriptor() : null;
        if (Objects.isNull(this.serviceClass)) {
            throw new InterceptorMapperException("no class definition found for the interceptor: " + name +
                    ". Make sure that the interceptor return type is defined with the specific interceptor class " +
                    "type rather than the generic `http:Interceptor` type");
        }
        extractInterceptorDetails(semanticModel);
    }

    protected abstract void extractInterceptorDetails(SemanticModel semanticModel);

    public abstract boolean isInvokable(TargetResource targetResource);

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

    private boolean isSubTypeOfDefaultInterceptorReturnType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName(BALLERINA, HTTP,
                EMPTY, NEXT_SERVICE);
        if (optNextServiceType.isEmpty() ||
                !(optNextServiceType.get() instanceof TypeDefinitionSymbol nextServiceType)) {
            return false;
        }
        UnionTypeSymbol defaultInterceptorReturnType = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                nextServiceType.typeDescriptor(), semanticModel.types().NIL).build();
        return defaultInterceptorReturnType.subtypeOf(typeSymbol);
    }

    private boolean isSubTypeOfHttpNextServiceType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optNextServiceType = semanticModel.types().getTypeByName(BALLERINA, HTTP,
                EMPTY, NEXT_SERVICE);
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

        if (MediaTypeUtils.getInstance(semanticModel).isSameMediaType(typeSymbol)) {
            return typeSymbol;
        }

        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            return getEffectiveReturnType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), semanticModel);
        } else if (typeSymbol instanceof UnionTypeSymbol) {
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

    public abstract InterceptorType getType();

    public boolean isContinueExecution() {
        return continueExecution;
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
