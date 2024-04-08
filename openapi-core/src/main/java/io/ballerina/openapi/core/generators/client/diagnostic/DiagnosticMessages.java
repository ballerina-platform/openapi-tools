package io.ballerina.openapi.core.generators.client.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public enum DiagnosticMessages {
    OAS_CLIENT_100("OAS_CLIENT_100",
            "invalid reference value : %s ballerina only supports local reference values.", DiagnosticSeverity.ERROR),
    OAS_CLIENT_101("OAS_CLIENT_101",
            "encounter unsupported path parameter data type for the parameter: '%s'", DiagnosticSeverity.ERROR),
    OAS_CLIENT_102("OAS_CLIENT_102",
            "encounter unsupported query parameter data type for the parameter: '%s'", DiagnosticSeverity.ERROR),
    OAS_CLIENT_103("OAS_CLIENT_103", "please define the array item type of the parameter : '%s'",
            DiagnosticSeverity.ERROR),
    OAS_CLIENT_104("OAS_CLIENT_104", "error occurred while generating query parameter node: '%s'",
            DiagnosticSeverity.ERROR),
    OAS_CLIENT_105("OAS_CLIENT_105", "provide non-empty value for server variable ", DiagnosticSeverity.WARNING),
    OAS_CLIENT_106("OAS_CLIENT_106", "failed to read endpoint details of the server: '%s'",
            DiagnosticSeverity.WARNING),
    OAS_CLIENT_107("OAS_CLIENT_107", "error while generating ",DiagnosticSeverity.WARNING),
    OAS_CLIENT_108("OAS_CLIENT_108", "encounter unsupported header parameter data type for the header: '%s'",
            DiagnosticSeverity.WARNING),
    OAS_CLIENT_109("OAS_CLIENT_109", "encounter issue while resolving reference: '%s'",
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
