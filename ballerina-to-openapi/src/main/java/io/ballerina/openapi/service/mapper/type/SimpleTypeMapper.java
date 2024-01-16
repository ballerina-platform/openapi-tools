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

import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.Objects;

import static io.ballerina.openapi.service.mapper.Constants.BYTE;
import static io.ballerina.openapi.service.mapper.Constants.DOUBLE;
import static io.ballerina.openapi.service.mapper.Constants.FLOAT;
import static io.ballerina.openapi.service.mapper.Constants.INT32;
import static io.ballerina.openapi.service.mapper.Constants.INT64;

public class SimpleTypeMapper extends AbstractTypeMapper {

    public SimpleTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
    }

    @Override
    public Schema getReferenceSchema(Components components) {
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        Schema schema = getTypeSchema(referredType, additionalData);
        return Objects.nonNull(schema) ? schema.description(description) : null;
    }

    public static Schema getTypeSchema(TypeSymbol typeSymbol, AdditionalData additionalData) {
        switch (typeSymbol.typeKind()) {
            case STRING:
            case STRING_CHAR:
                return new StringSchema();
            case BYTE:
                return new StringSchema().format(BYTE);
            case INT:
                return new IntegerSchema().format(INT64);
            case INT_SIGNED32:
                return new IntegerSchema().format(INT32);
            case INT_UNSIGNED32:
            case INT_UNSIGNED16:
            case INT_SIGNED16:
            case INT_UNSIGNED8:
            case INT_SIGNED8:
                return new IntegerSchema().format(null);
            case DECIMAL:
                return new NumberSchema().format(DOUBLE);
            case FLOAT:
                return new NumberSchema().format(FLOAT);
            case BOOLEAN:
                return new BooleanSchema();
            case JSON:
            case XML:
            case XML_ELEMENT:
            case XML_PROCESSING_INSTRUCTION:
            case XML_TEXT:
            case XML_COMMENT:
                return new ObjectSchema();
            case NIL:
                Schema schema = new Schema();
                schema.setNullable(true);
                return schema;
            case ANYDATA:
                return new Schema();
            default:
                DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_121;
                ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                        message.getDescription(), null, MapperCommonUtils.getTypeName(typeSymbol));
                additionalData.diagnostics().add(error);
                return null;
        }
    }
}
