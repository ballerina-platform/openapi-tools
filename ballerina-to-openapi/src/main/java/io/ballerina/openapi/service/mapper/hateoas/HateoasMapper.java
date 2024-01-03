/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.service.mapper.hateoas;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ServiceNodeVisitor;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

public class HateoasMapper {

    private final SemanticModel semanticModel;
    private final ServiceNodeVisitor serviceNodeVisitor;

    public HateoasMapper(SemanticModel semanticModel, ServiceNodeVisitor serviceNodeVisitor) {
        this.semanticModel = semanticModel;
        this.serviceNodeVisitor = serviceNodeVisitor;
    }

//    public void mapHateoasLinks() {
//
//    }
}
