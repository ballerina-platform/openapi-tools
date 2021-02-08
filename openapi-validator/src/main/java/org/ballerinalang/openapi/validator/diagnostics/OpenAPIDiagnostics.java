package org.ballerinalang.openapi.validator.diagnostics;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.diagnostics.properties.DiagnosticProperty;

import java.util.List;

public class OpenAPIDiagnostics extends Diagnostic {
    private Location location;
    private DiagnosticInfo diagnosticInfo;
    private String message;

    public OpenAPIDiagnostics(Location location, DiagnosticInfo diagnosticInfo, String message) {
        this.location = location;
        this.diagnosticInfo = diagnosticInfo;
        this.message = message;
    }

    @Override
    public Location location() {

        return this.location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {

        return this.diagnosticInfo;
    }

    @Override
    public String message() {

        return this.message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {

        return null;
    }
}
