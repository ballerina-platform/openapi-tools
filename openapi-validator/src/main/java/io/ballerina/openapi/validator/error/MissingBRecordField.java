/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator.error;

import io.ballerina.tools.diagnostics.Location;

/**
 * This class will contain the details regarding missing field in ballerina record.
 *
 * @since 2.0.0
 */
public class MissingBRecordField extends ValidationError {
    private final String fieldName;
    private final String type;
    private final String recordName;

    public MissingBRecordField(String fieldName, String type, String recordName, Location location) {
        super(location);
        this.fieldName = fieldName;
        this.type = type;
        this.recordName = recordName;
    }

    public String getFieldName() {
        return fieldName;
    }
    public String getType() {
        return type;
    }

    public String getRecordName() {
        return recordName;
    }
}
