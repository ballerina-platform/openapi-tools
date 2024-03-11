package io.ballerina.openapi.core.generators.client.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public enum DiagnosticMessages {
    OAS_CLIENT_100("OAS_CLIENT_100",
            "invalid reference value : %s ballerina only supports local reference values.", DiagnosticSeverity.ERROR),
    OAS_CLIENT_101("OAS_CLIENT_101",
            "encounter unsupported path parameter data type for the parameter: '%s'", DiagnosticSeverity.ERROR);
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
