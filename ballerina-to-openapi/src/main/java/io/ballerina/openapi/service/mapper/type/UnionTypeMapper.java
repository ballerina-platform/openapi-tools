/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnionTypeMapper extends AbstractTypeMapper {

    private final boolean isEnumType;

    public UnionTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
        this.isEnumType = isEnumTypeDefinition(typeSymbol);
    }

    @Override
    public Schema getReferenceSchema(Components components) {
        Schema schema;
        if (isEnumType) {
            schema = getEnumTypeSchema((EnumSymbol) typeSymbol.definition());
        } else {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol.typeDescriptor();
            schema = getSchema(unionTypeSymbol, components, additionalData, false);
        }
        return Objects.nonNull(schema) ? schema.description(description) : null;
    }

    public static Schema getSchema(UnionTypeSymbol typeSymbol, Components components, AdditionalData additionalData,
                                   boolean skipNilType) {
        if (isUnionOfSingletons(typeSymbol)) {
            return getSingletonUnionTypeSchema(typeSymbol, additionalData.diagnostics());
        }
        List<TypeSymbol> memberTypeSymbols = typeSymbol.userSpecifiedMemberTypes();
        List<Schema> memberSchemas = new ArrayList<>();
        boolean nullable = !skipNilType && hasNilableType(typeSymbol);
        for (TypeSymbol memberTypeSymbol : memberTypeSymbols) {
            if (memberTypeSymbol.typeKind().equals(TypeDescKind.NIL)) {
                continue;
            }
            Schema schema = TypeMapper.getTypeSchema(memberTypeSymbol, components, additionalData, nullable);
            if (Objects.nonNull(schema)) {
                memberSchemas.add(schema);
            }
        }
        if (memberSchemas.isEmpty()) {
            return null;
        }
        Schema schema;
        if (memberSchemas.size() == 1) {
            schema = memberSchemas.get(0);
            if (Objects.nonNull(schema.get$ref()) && nullable) {
                schema = new ComposedSchema().allOf(List.of(schema));
            }
        } else {
            schema = new ComposedSchema().oneOf(memberSchemas);
        }
        if (nullable) {
            schema.setNullable(true);
        }
        return schema;
    }

    static boolean isUnionOfSingletons(UnionTypeSymbol typeSymbol) {
        List<TypeSymbol> memberTypeSymbols = typeSymbol.userSpecifiedMemberTypes();
        return memberTypeSymbols.stream().allMatch(symbol -> symbol.typeKind().equals(TypeDescKind.SINGLETON) ||
                symbol.typeKind().equals(TypeDescKind.NIL));
    }

    public static boolean hasNilableType(TypeSymbol typeSymbol) {
        return switch (typeSymbol.typeKind()) {
            case TYPE_REFERENCE -> hasNilableType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            case UNION -> {
                List<TypeSymbol> memberTypeSymbols = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors();
                yield memberTypeSymbols.stream().anyMatch(symbol -> symbol.typeKind().equals(TypeDescKind.NIL));
            }
            case NIL -> true;
            default -> false;
        };
    }

    static Schema getSingletonUnionTypeSchema(UnionTypeSymbol typeSymbol, List<OpenAPIMapperDiagnostic> diagnostics) {
        List<TypeSymbol> memberTypeSymbols = typeSymbol.userSpecifiedMemberTypes();
        List<String> enumValues = new ArrayList<>();
        boolean nullable = hasNilableType(typeSymbol);
        for (TypeSymbol memberTypeSymbol : memberTypeSymbols) {
            if (memberTypeSymbol.typeKind().equals(TypeDescKind.NIL)) {
                continue;
            } else if (!((SingletonTypeSymbol) memberTypeSymbol).originalType().
                    typeKind().equals(TypeDescKind.STRING)) {
                DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_122;
                ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                        message.getDescription(), null, MapperCommonUtils.getTypeName(typeSymbol));
                diagnostics.add(error);
                return null;
            }
            String signature = memberTypeSymbol.signature();
            enumValues.add(signature.substring(1, signature.length() - 1));
        }
        StringSchema schema = new StringSchema();
        schema.setEnum(enumValues);
        if (nullable) {
            schema.setNullable(true);
        }
        return schema;
    }

    static boolean isEnumTypeDefinition(TypeReferenceTypeSymbol typeSymbol) {
        Symbol definitionSymbol = typeSymbol.definition();
        return definitionSymbol.kind().equals(SymbolKind.ENUM);
    }

    static Schema getEnumTypeSchema(EnumSymbol typeSymbol) {
        List<ConstantSymbol> enumConstants = typeSymbol.members();
        List<String> enumValues = new ArrayList<>();
        for (ConstantSymbol constantSymbol : enumConstants) {
            Object enumValue = constantSymbol.constValue();
            enumValues.add(enumValue.toString());
        }
        return new StringSchema()._enum(enumValues);
    }
}
