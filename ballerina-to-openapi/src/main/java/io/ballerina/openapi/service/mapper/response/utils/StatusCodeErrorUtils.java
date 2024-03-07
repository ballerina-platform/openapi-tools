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
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.response.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.swagger.v3.oas.models.headers.Header;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.HTTP_500;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_STATUS_CODE_ERRORS;

/**
 * This {@link StatusCodeErrorUtils} class provides functionalities for mapping the Ballerina HTTP status code
 * error to OpenAPI response.
 *
 * @since 1.9.0
 */
public final class StatusCodeErrorUtils extends StatusCodeTypeUtils {

    private StatusCodeErrorUtils() {
    }

    public static boolean isSubTypeOfHttpStatusCodeError(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return isSubTypeOfBallerinaModuleType("StatusCodeError", "http.httpscerr", typeSymbol, semanticModel);
    }

    public static ResponseInfo extractResponseInfo(TypeSymbol statusCodeErrorType, TypeMapper typeMapper,
                                                   SemanticModel semanticModel) {
        String statusCode = getResponseCode(statusCodeErrorType, semanticModel);
        Optional<RecordTypeSymbol> errorDetailRecordType = getErrorDetailTypeSymbol(statusCodeErrorType, typeMapper);
        if (errorDetailRecordType.isEmpty()) {
            return new ResponseInfo(statusCode, semanticModel.types().ANYDATA, new HashMap<>());
        }
        TypeSymbol bodyType = getBodyType(errorDetailRecordType.get(), semanticModel);
        Map<String, Header> headers = getHeaders(errorDetailRecordType.get(), typeMapper);
        return new ResponseInfo(statusCode, bodyType, headers);
    }

    private static Optional<RecordTypeSymbol> getErrorDetailTypeSymbol(TypeSymbol typeSymbol, TypeMapper typeMapper) {
        IntersectionTypeSymbol errorIntersectionType = typeMapper.getReferredIntersectionType(typeSymbol);
        if (Objects.isNull(errorIntersectionType) ||
                !(errorIntersectionType.effectiveTypeDescriptor() instanceof ErrorTypeSymbol errorTypeSymbol)) {
            return Optional.empty();
        }
        return getRecordTypeSymbol(errorTypeSymbol.detailTypeDescriptor(), typeMapper);
    }

    private static String getResponseCode(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        for (Map.Entry<String, String> entry : HTTP_STATUS_CODE_ERRORS.entrySet()) {
            if (isSubTypeOfBallerinaModuleType(entry.getKey(), "http.httpscerr", typeSymbol, semanticModel)) {
                return entry.getValue();
            }
        }
        return HTTP_500;
    }
}
