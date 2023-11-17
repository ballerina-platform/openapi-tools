// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TypeMapper {

    final String name;
    final TypeReferenceTypeSymbol typeSymbol;
    final String description;
    final SemanticModel semanticModel;
    final List<OpenAPIMapperDiagnostic> diagnostics;


    public TypeMapper(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                      List<OpenAPIMapperDiagnostic> diagnostics) {
        this.name = MapperCommonUtils.getTypeName(typeSymbol);
        this.typeSymbol = typeSymbol;
        this.description = MapperCommonUtils.getTypeDescription(typeSymbol);
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    abstract Schema getReferenceTypeSchema(Map<String, Schema> components);

    public void addToComponents(Map<String, Schema> components) {
        if (components.containsKey(name) && Objects.nonNull(components.get(name))) {
            return;
        }
        Schema schema = getReferenceTypeSchema(components);
        if (Objects.nonNull(schema)) {
            components.put(name, schema);
        }
    }
}
