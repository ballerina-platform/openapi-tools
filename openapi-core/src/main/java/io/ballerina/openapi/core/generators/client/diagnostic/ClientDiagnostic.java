package io.ballerina.openapi.core.generators.client.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public interface ClientDiagnostic {

    DiagnosticSeverity getDiagnosticSeverity();

    String getMessage();

    Optional<Location> getLocation();

    String getCode();
}
