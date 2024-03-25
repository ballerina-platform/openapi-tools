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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.service.mapper.ServiceMapperFactory;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.interceptor.model.RequestParameterInfo;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;

import java.util.List;
import java.util.Map;

/**
 * This {@link ParameterMapperWithInterceptors} class is the implementation of the {@link ParameterMapper} interface.
 * This class provides functionalities for mapping the Ballerina request parameters to OpenAPI parameters when the
 * service has interceptors.
 *
 * @since 1.9.0
 */
public class ParameterMapperWithInterceptors extends DefaultParameterMapper {

    private final RequestParameterInfo requestParameterInfo;

    public ParameterMapperWithInterceptors(FunctionDefinitionNode functionDefinitionNode,
                                           OperationInventory operationInventory, Map<String, String> apiDocs,
                                           AdditionalData additionalData, Boolean treatNilableAsOptional,
                                           RequestParameterInfo requestParameterInfo,
                                           ServiceMapperFactory serviceMapperFactory) {
        super(functionDefinitionNode, operationInventory, apiDocs, additionalData, treatNilableAsOptional,
                serviceMapperFactory);
        this.requestParameterInfo = requestParameterInfo;
        addReqParametersFromTargetResource(additionalData.diagnostics());
    }

    private void addReqParametersFromTargetResource(List<OpenAPIMapperDiagnostic> diagnostics) {
        requestParameterInfo.addReqParameters(super.getParameterNodes(), diagnostics, true);
    }

    @Override
    protected Iterable<ParameterNode> getParameterNodes() {
        return requestParameterInfo.getReqParameterNodes();
    }
}
