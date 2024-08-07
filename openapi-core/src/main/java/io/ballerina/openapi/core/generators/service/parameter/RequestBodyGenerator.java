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

package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;

public abstract class RequestBodyGenerator {

    final OASServiceMetadata oasServiceMetadata;
    final String path;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    RequestBodyGenerator (OASServiceMetadata oasServiceMetadata, String path) {
        this.oasServiceMetadata = oasServiceMetadata;
        this.path = path;
    }

    public static RequestBodyGenerator getRequestBodyGenerator(OASServiceMetadata oasServiceMetadata, String path) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceRequestBodyGenerator(oasServiceMetadata, path);
        }
        return new DefaultRequestBodyGenerator(oasServiceMetadata, path);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public abstract RequiredParameterNode createRequestBodyNode(RequestBody requestBody);
}
