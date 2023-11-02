/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.service.mapper.type.TypeSchemaGenerator.createComponentMapping;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class OpenAPIComponentMapper {

    private final Components components;
    private final List<OpenAPIMapperDiagnostic> diagnostics;
    private final SemanticModel semanticModel;

    public OpenAPIComponentMapper(Components components, SemanticModel semanticModel) {
        this.components = components;
        this.diagnostics = new ArrayList<>();
        this.semanticModel = semanticModel;
    }

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
      * This function for doing the mapping with ballerina record to object schema.
      *
      * @param typeSymbol Record Name as a TypeSymbol
      */
    public void createComponentSchema(TypeSymbol typeSymbol) {
        Map<String, Schema> schema = components.getSchemas();
        if (schema == null) {
            schema = new HashMap<>();
        }
        Map<String, Schema> componentsSchema = createComponentMapping((TypeReferenceTypeSymbol) typeSymbol, schema,
                semanticModel);
        components.setSchemas(componentsSchema);
    }
}
