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
package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.List;

import static io.ballerina.openapi.core.generators.common.GeneratorUtils.generateStatusCodeTypeInclusionRecord;

/**
 * This class is used to generate the status code response return types.
 *
 * @since 1.9.0
 */
public class FunctionStatusCodeReturnTypeGenerator extends FunctionReturnTypeGeneratorImp {

    public FunctionStatusCodeReturnTypeGenerator(Operation operation, OpenAPI openAPI, String httpMethod) {
        super(operation, openAPI, httpMethod);
    }

    @Override
    protected boolean populateReturnType(String statusCode, ApiResponse response, List<TypeDescriptorNode> returnTypes) {
        boolean noContentResponseFoundSuper = super.populateReturnType(statusCode, response, returnTypes);
        returnTypes.add(generateStatusCodeTypeInclusionRecord(
                GeneratorConstants.HTTP_CODES_DES.get(statusCode), response, httpMethod , openAPI));
        return noContentResponseFoundSuper;
    }
}
