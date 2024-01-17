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
package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractTypeMapper {

    final String name;
    final TypeReferenceTypeSymbol typeSymbol;
    final String description;
    final AdditionalData additionalData;


    public AbstractTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        this.name = MapperCommonUtils.getTypeName(typeSymbol);
        this.typeSymbol = typeSymbol;
        this.description = MapperCommonUtils.getTypeDescription(typeSymbol);
        this.additionalData = additionalData;
    }

    abstract Schema getReferenceSchema(Components components);

    public void addToComponents(Components components) {
        Map<String, Schema> schemas = components.getSchemas();
        if (schemas.containsKey(name) && Objects.nonNull(schemas.get(name))) {
            return;
        }
        Schema schema = getReferenceSchema(components);
        if (Objects.nonNull(schema)) {
            components.addSchemas(name, schema);
        }
    }

    static boolean hasMapping(Components components, TypeSymbol typeSymbol) {
        Map<String, Schema> schemas = components.getSchemas();
        return schemas.containsKey(MapperCommonUtils.getTypeName(typeSymbol));
    }

    static boolean hasFullMapping(Components components, TypeSymbol typeSymbol) {
        Map<String, Schema> schemas = components.getSchemas();
        return schemas.containsKey(MapperCommonUtils.getTypeName(typeSymbol)) && Objects.nonNull(
                schemas.get(MapperCommonUtils.getTypeName(typeSymbol)));
    }
}
