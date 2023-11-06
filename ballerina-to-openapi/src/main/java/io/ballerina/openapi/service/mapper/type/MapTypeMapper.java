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
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

public class MapTypeMapper extends TypeMapper {

    public MapTypeMapper(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel) {
        super(typeSymbol, semanticModel);
    }

    @Override
    public Schema getReferenceTypeSchema(Map<String, Schema> components) {
        MapTypeSymbol referredType = (MapTypeSymbol) typeSymbol.typeDescriptor();
        return getSchema(referredType, components, semanticModel).description(description);
    }

    public static Schema getSchema(MapTypeSymbol typeSymbol, Map<String, Schema> components,
                                   SemanticModel semanticModel) {
        TypeSymbol memberType = typeSymbol.typeParam();
        return new ObjectSchema().additionalProperties(ComponentMapper.getTypeSchema(memberType,
                components, semanticModel));
    }
}
