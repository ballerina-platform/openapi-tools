/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

/**
 * Diagnostic codes for OpenAPI CLI tool.
 *
 * @since 2.4.0
 */
public enum OpenAPICmdToolDiagnostic {

    OAS_CLI_000("OAS_CLI_000", "error occurred while validating the Ballerina package path"),
    OAS_CLI_001("OAS_CLI_001", "provided directory: %s is a Ballerina workspace which is not supported by the tool." +
            " Please provide a Ballerina package directory"),
    OAS_CLI_002("OAS_CLI_002", "invalid Ballerina package directory: %s, cannot find 'Ballerina.toml' file");

    private final String code;
    private final String message;

    OpenAPICmdToolDiagnostic(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
