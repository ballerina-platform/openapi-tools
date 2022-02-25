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

public enum ErrorMessage {
    ERROR_001("OpenAPI contract doesn't exist in the given location: %s"),
    ERROR_002("Invalid file type. Provide either a .yaml or .json file."),
    ERROR_003("Given OpenAPI contract file path is an empty string."),
    ERROR_004("OpenAPI annotation won''t support for non http service."),
    ERROR_005(""),
    ERROR_006(""),
    ERROR_007(""),
    ERROR_008(""),
    ERROR_009(""),
    ERROR_010("");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
