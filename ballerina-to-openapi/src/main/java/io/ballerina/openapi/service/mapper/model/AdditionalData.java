/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;

import java.util.List;

/**
 * This {@link AdditionalData} record stores the additional data required for the openapi service mapper.
 * @param semanticModel - The semantic model of the ballerina project.
 * @param moduleMemberVisitor - The module member visitor.
 * @param diagnostics - The list of diagnostics.
 *
 * @since 1.9.0
 */
public record AdditionalData(SemanticModel semanticModel,
                             ModuleMemberVisitor moduleMemberVisitor,
                             List<OpenAPIMapperDiagnostic> diagnostics) {
}
