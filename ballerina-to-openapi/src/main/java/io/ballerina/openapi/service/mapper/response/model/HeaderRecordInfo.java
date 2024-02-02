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
package io.ballerina.openapi.service.mapper.response.model;

import io.ballerina.compiler.api.symbols.RecordTypeSymbol;

/**
 * This {@link HeaderRecordInfo} record stores the response header record information.
 * @param recordType - The record type of the response header.
 * @param recordName - The name of the record.
 *
 * @since 1.9.0
 */
public record HeaderRecordInfo(RecordTypeSymbol recordType,
                               String recordName) {
}
