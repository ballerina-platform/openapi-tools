package io.ballerina.openapi.core.generators.client.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public class ClientExceptionDiagnostic implements ClientDiagnostic {
    private final String code;
    private final String message;
    private final DiagnosticSeverity diagnosticSeverity;
    private final Location location;

    public ClientExceptionDiagnostic(String code, String message, Location location, String... args) {
        this.code = code;
        this.message = String.format(message, (Object[]) args);
        this.diagnosticSeverity = DiagnosticSeverity.ERROR;
        this.location = location;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public DiagnosticSeverity getDiagnosticSeverity() {
        return diagnosticSeverity;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }
}
