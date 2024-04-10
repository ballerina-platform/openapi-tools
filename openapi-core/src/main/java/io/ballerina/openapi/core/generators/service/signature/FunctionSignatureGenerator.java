/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.service.signature;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FunctionSignatureGenerator {

    final OASServiceMetadata oasServiceMetadata;
    boolean isNullableRequired;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public FunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public static FunctionSignatureGenerator getFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceFunctionSignatureGenerator(oasServiceMetadata);
        }
        return new DefaultFunctionSignatureGenerator(oasServiceMetadata);
    }

    public abstract FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                               String path) throws BallerinaOpenApiException;

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
