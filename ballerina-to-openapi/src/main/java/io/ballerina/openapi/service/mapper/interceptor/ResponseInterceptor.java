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
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This {@link RequestErrorInterceptor} class represents the response interceptor service.
 *
 * @since 1.9.0
 */
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
