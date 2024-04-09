package io.ballerina.openapi.core.generators.service.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.List;

public class ServiceDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;

    public ServiceDiagnostic(ServiceDiagnosticMessages diagnostic, String... args) {
        this.diagnosticInfo = new DiagnosticInfo(diagnostic.getCode(), String.format(diagnostic.getDescription(),
                (Object[]) args), diagnostic.getSeverity());
    }

    public ServiceDiagnostic(String code, String diagnostic, DiagnosticSeverity severity) {
        this.diagnosticInfo = new DiagnosticInfo(code, diagnostic, severity);
    }

    @Override
    public Location location() {
        return new NullLocation();
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return diagnosticInfo;
    }

    @Override
    public String message() {
        return diagnosticInfo.messageFormat();
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return null;
    }

    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
