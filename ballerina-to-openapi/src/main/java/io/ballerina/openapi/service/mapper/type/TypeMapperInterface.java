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

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface TypeMapperInterface {

    Schema getTypeSchema(TypeSymbol typeSymbol);

    Map<String, Schema> mapRecordFields(Map<String, RecordFieldSymbol> recordFieldMap,
                                        Set<String> requiredFields, String recordName,
                                        boolean treatNilableAsOptional);

    TypeSymbol getReferredType(TypeSymbol typeSymbol);

    static void setDefaultValue(Schema schema, Object defaultValue) {
        if (Objects.isNull(defaultValue)) {
            return;
        }
        if (Objects.nonNull(schema.get$ref())) {
            Schema refSchema = new Schema<>();
            refSchema.set$ref(schema.get$ref());
            schema.set$ref(null);
            schema.addAllOfItem(refSchema);
            schema.setType(null);
        }
        schema.setDefault(defaultValue);
    }
}
