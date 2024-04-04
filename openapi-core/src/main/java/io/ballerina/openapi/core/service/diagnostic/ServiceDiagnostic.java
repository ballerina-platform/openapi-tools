package io.ballerina.openapi.core.service.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

public class ServiceDiagnostic {
    private final String code;
    private final String message;
    private final DiagnosticSeverity diagnosticSeverity;

    public ServiceDiagnostic(ServiceDiagnosticMessages diagnostic, String... args) {
        this.code = diagnostic.getCode();
        this.message = String.format(diagnostic.getDescription(), (Object[]) args);
        this.diagnosticSeverity = diagnostic.getSeverity();
    }

    public String getCode() {
        return code;
    }

    public DiagnosticSeverity getDiagnosticSeverity() {
        return diagnosticSeverity;
    }

    public String getMessage() {
        return message;
    }

}
