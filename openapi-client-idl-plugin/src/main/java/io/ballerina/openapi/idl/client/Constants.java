package io.ballerina.openapi.idl.client;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * Storing constant for client IDL generator.
 */
public class Constants {
    public static final String OPENAPI_CLIENT_REFERENCE = "openapi:ClientInfo";
    public static final String TAGS = "tags";
    public static final String OPERATIONS = "operations";
    public static final String NULLABLE = "nullable";
    public static final String IS_RESOURCE = "isResource";
    public static final String WITH_TESTS = "withTests";
    public static final String LICENSE = "license";
    public static final String TRUE = "true";

    /**
     * Enum class for contain diagnostic messages.
     */
    public enum DiagnosticMessages {
        LICENSE_PATH_BLANK("OAS_IDL_CLIENT_01", "given license file path is an empty string.",
                DiagnosticSeverity.WARNING),
        ERROR_WHILE_READING_LICENSE_FILE("OAS_IDL_CLIENT_02", "unexpected error occurred while reading the license",
                DiagnosticSeverity.ERROR),
        ERROR_WHILE_GENERATING_CLIENT("OAS_IDL_CLIENT_03", "unexpected error occurred while generating the client",
                                         DiagnosticSeverity.ERROR);


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
