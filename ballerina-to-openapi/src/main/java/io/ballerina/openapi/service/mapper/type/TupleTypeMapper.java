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

import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.AdditionalData;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TupleTypeMapper extends TypeMapper {

    public TupleTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
    }

    @Override
    public Schema getReferenceTypeSchema(Map<String, Schema> components) {
        TupleTypeSymbol referredType = (TupleTypeSymbol) typeSymbol.typeDescriptor();
        return getSchema(referredType, components, additionalData).description(description);
    }

    public static Schema getSchema(TupleTypeSymbol typeSymbol, Map<String, Schema> components, AdditionalData additionalData) {
        Optional<TypeSymbol> restTypeSymbol = typeSymbol.restTypeDescriptor();
        if (restTypeSymbol.isPresent()) {
            DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_123;
            ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                    message.getDescription(), null, MapperCommonUtils.getTypeName(typeSymbol));
            additionalData.diagnostics().add(error);
        }
        List<TypeSymbol> memberTypeSymbols = typeSymbol.memberTypeDescriptors();
        Schema memberSchema = new ComposedSchema().oneOf(memberTypeSymbols.stream().map(
                type -> ComponentMapper.getTypeSchema(type, components, additionalData)).toList());
        return new ArraySchema().items(memberSchema);
    }
}
