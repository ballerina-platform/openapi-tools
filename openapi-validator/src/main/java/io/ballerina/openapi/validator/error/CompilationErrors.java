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

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class for containing the error message related to openapi validator plugin.
 *
 * @since 2.0.0
 */
public enum CompilationErrors {
    INVALID_CONTRACT_PATH(ErrorCode.OPENAPI_VALIDATOR_001, ErrorMessage.ERROR_001,
            DiagnosticSeverity.ERROR),
    INVALID_CONTRACT_FORMAT(ErrorCode.OPENAPI_VALIDATOR_002, ErrorMessage.ERROR_002,
            DiagnosticSeverity.ERROR),
    EMPTY_CONTRACT_PATH(ErrorCode.OPENAPI_VALIDATOR_003, ErrorMessage.ERROR_003,
            DiagnosticSeverity.ERROR),
    NON_HTTP_SERVICE(ErrorCode.OPENAPI_VALIDATOR_004, ErrorMessage.ERROR_004,
            DiagnosticSeverity.WARNING),
    TYPE_MISMATCH_FIELD(ErrorCode.OPENAPI_VALIDATOR_005, ErrorMessage.ERROR_005,
            DiagnosticSeverity.ERROR),
    TYPE_MISMATCH_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_006, ErrorMessage.ERROR_006,
            DiagnosticSeverity.ERROR),
    MISSING_BRECORD_FIELD(ErrorCode.OPENAPI_VALIDATOR_007, ErrorMessage.ERROR_007,
            DiagnosticSeverity.ERROR),
    MISSING_OAS_PROPERTY(ErrorCode.OPENAPI_VALIDATOR_008, ErrorMessage.ERROR_008,
            DiagnosticSeverity.ERROR),
    UNDOCUMENTED_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_009, ErrorMessage.ERROR_009,
            DiagnosticSeverity.ERROR),
    UNIMPLEMENTED_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_010, ErrorMessage.ERROR_010,
            DiagnosticSeverity.ERROR);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    CompilationErrors(ErrorCode code, ErrorMessage description, DiagnosticSeverity severity) {

        this.code = code.name();
        this.description = description.getMessage();
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }
}

