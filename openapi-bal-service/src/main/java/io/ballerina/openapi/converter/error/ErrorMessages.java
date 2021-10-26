package io.ballerina.openapi.converter.error;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public enum ErrorMessages {
    OAS_CONVERTOR_100("OAS_CONVERTOR_100", "Generated OpenAPI definition doesn't contain details for the `default` " +
            "resource method in the Ballerina service.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_101("OAS_CONVERTOR_101", "Unexpected value: " , DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_102("OAS_CONVERTOR_102", "Invalid mediaType : " , DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_103("OAS_CONVERTOR_103", "No related status code for : " , DiagnosticSeverity.ERROR);


    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    ErrorMessages(String code, String description, DiagnosticSeverity severity) {
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
