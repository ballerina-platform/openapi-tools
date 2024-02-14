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
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.response.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.swagger.v3.oas.models.headers.Header;

import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.HTTP_CODES;

/**
 * This {@link StatusCodeResponseUtils} class provides functionalities for mapping the Ballerina HTTP status code
 * response to OpenAPI response.
 *
 * @since 1.9.0
 */
public final class StatusCodeResponseUtils extends StatusCodeTypeUtils {

    private StatusCodeResponseUtils() {
    }

    public static boolean isSubTypeOfHttpStatusCodeResponse(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return isSubTypeOfBallerinaModuleType("StatusCodeResponse", "http", typeSymbol, semanticModel);
    }

    public static ResponseInfo extractResponseInfo(TypeSymbol statusCodeResponseType, String defaultStatusCode,
                                                   TypeMapper typeMapper, SemanticModel semanticModel) {
        Optional<RecordTypeSymbol> statusCodeRecordType = getRecordTypeSymbol(statusCodeResponseType, typeMapper);
        String statusCode = getResponseCode(statusCodeResponseType, defaultStatusCode, semanticModel);
        if (statusCodeRecordType.isEmpty()) {
            return new ResponseInfo(statusCode, semanticModel.types().ANYDATA, Map.of());
        }
        TypeSymbol bodyType = getBodyType(statusCodeRecordType.get(), semanticModel);
        Map<String, Header> headers = getHeaders(statusCodeRecordType.get(), typeMapper);
        return new ResponseInfo(statusCode, bodyType, headers);
    }

    private static String getResponseCode(TypeSymbol typeSymbol, String defaultCode, SemanticModel semanticModel) {
        for (Map.Entry<String, String> entry : HTTP_CODES.entrySet()) {
            if (isSubTypeOfBallerinaModuleType(entry.getKey(), "http", typeSymbol, semanticModel)) {
                return entry.getValue();
            }
        }
        return defaultCode;
    }
}
