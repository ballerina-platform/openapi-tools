/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package io.ballerina.openapi.bal.tool;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This class includes constants for ballerina package build generator.
 *
 * @since 1.9.0
 */
public class Constants {
    public static final String TAGS = "tags";
    public static final String OPERATIONS = "operations";
    public static final String NULLABLE = "nullable";
    public static final String IS_RESOURCE = "isResource";
    public static final String LICENSE = "license";
    public static final String TRUE = "true";
    public static final String MODE = "mode";
    public static final String CLIENT = "client";

    /**
     * Enum class for containing diagnostic messages.
     */
    public enum DiagnosticMessages {
        LICENSE_PATH_BLANK("OAS_IDL_CLIENT_01", "given license file path is an empty string.",
                DiagnosticSeverity.WARNING),
        ERROR_WHILE_READING_LICENSE_FILE("OAS_IDL_CLIENT_02", "unexpected error occurred while reading the license",
                DiagnosticSeverity.ERROR),
        ERROR_WHILE_GENERATING_CLIENT("OAS_IDL_CLIENT_03", "unexpected error occurred while generating the client",
                DiagnosticSeverity.ERROR),
        PARSER_ERROR("OAS_IDL_CLIENT_04", "", DiagnosticSeverity.ERROR),
        UNEXPECTED_EXCEPTIONS("OAS_IDK_CLIENT_05", "unexpected error occurred while reading the contract",
                DiagnosticSeverity.ERROR),
        EMPTY_CONTRACT_PATH("OAS_IDK_CLIENT_06", "given OpenAPI contract file path is an empty string.",
                DiagnosticSeverity.WARNING);

        private final String code;
        private final String description;
        private final DiagnosticSeverity severity;

        DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
            this.code = code;
            this.description = description;
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
}
