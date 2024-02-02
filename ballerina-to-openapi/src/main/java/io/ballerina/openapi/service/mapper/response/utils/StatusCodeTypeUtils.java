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
package io.ballerina.openapi.service.mapper.response.utils;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.response.model.HeaderRecordInfo;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This {@link StatusCodeTypeUtils} class provides common functionalities for mapping the Ballerina HTTP status code
 * response and error to OpenAPI response.
 *
 * @since 1.9.0
 */
public abstract class StatusCodeTypeUtils {

     static boolean isSubTypeOfBallerinaModuleType(String type, String moduleName, TypeSymbol typeSymbol,
                                                   SemanticModel semanticModel) {
        Optional<Symbol> optionalRecordSymbol = semanticModel.types().getTypeByName("ballerina", moduleName,
                "", type);
        if (optionalRecordSymbol.isPresent() &&
                optionalRecordSymbol.get() instanceof TypeDefinitionSymbol recordSymbol) {
            return typeSymbol.subtypeOf(recordSymbol.typeDescriptor());
        }
        return false;
    }

    static RecordTypeSymbol getRecordTypeSymbol(TypeSymbol typeSymbol, TypeMapper typeMapper) {
        TypeSymbol statusCodeResType = typeMapper.getReferredType(typeSymbol);
        RecordTypeSymbol statusCodeRecordType = null;
        if (statusCodeResType instanceof TypeReferenceTypeSymbol statusCodeResRefType &&
                statusCodeResRefType.typeDescriptor() instanceof RecordTypeSymbol recordTypeSymbol) {
            statusCodeRecordType = recordTypeSymbol;
        } else if (statusCodeResType instanceof RecordTypeSymbol recordTypeSymbol) {
            statusCodeRecordType = recordTypeSymbol;
        }
        return statusCodeRecordType;
    }

    static TypeSymbol getBodyType(RecordTypeSymbol responseRecordType, SemanticModel semanticModel) {
        if (Objects.nonNull(responseRecordType) && responseRecordType.fieldDescriptors().containsKey("body")) {
            return responseRecordType.fieldDescriptors().get("body").typeDescriptor();
        }
        return semanticModel.types().ANYDATA;
    }

    static Map<String, Header> getHeaders(RecordTypeSymbol responseRecordType,
                                                 TypeMapper typeMapper) {
        if (Objects.isNull(responseRecordType)) {
            return new HashMap<>();
        }

        HeaderRecordInfo headersInfo = getHeadersInfo(responseRecordType, typeMapper);
        if (Objects.isNull(headersInfo)) {
            return new HashMap<>();
        }

        Map<String, RecordFieldSymbol> recordFieldMap = new HashMap<>(headersInfo.recordType().
                fieldDescriptors());
        Map<String, Schema> recordFieldsMapping = typeMapper.getSchemaForRecordFields(recordFieldMap, new HashSet<>(),
                headersInfo.recordName(), false);
        return mapRecordFieldToHeaders(recordFieldsMapping);
    }

    private static HeaderRecordInfo getHeadersInfo(RecordTypeSymbol responseRecordType, TypeMapper typeMapper) {
        if (responseRecordType.fieldDescriptors().containsKey("headers")) {
            TypeSymbol headersType = typeMapper.getReferredType(
                    responseRecordType.fieldDescriptors().get("headers").typeDescriptor());
            if (Objects.nonNull(headersType) && headersType instanceof TypeReferenceTypeSymbol headersRefType &&
                    headersRefType.typeDescriptor() instanceof RecordTypeSymbol recordType) {
                return new HeaderRecordInfo(recordType, MapperCommonUtils.getTypeName(headersType));
            } else if (Objects.nonNull(headersType) && headersType instanceof RecordTypeSymbol recordType) {
                return new HeaderRecordInfo(recordType, MapperCommonUtils.getTypeName(recordType));
            }
        }
        return null;
    }

    private static Map<String, Header> mapRecordFieldToHeaders(Map<String, Schema> recordFields) {
        Map<String, Header> headers = new HashMap<>();
        for (Map.Entry<String, Schema> entry : recordFields.entrySet()) {
            Header header = new Header();
            header.setSchema(entry.getValue());
            headers.put(entry.getKey(), header);
        }
        return headers;
    }
}
