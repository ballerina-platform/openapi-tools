package io.ballerina.openapi.core.generators.common.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.List;

public class CommonDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;

    public CommonDiagnostic(CommonDiagnosticMessages diagnostic, String... args) {
        this.diagnosticInfo = new DiagnosticInfo(diagnostic.getCode(), String.format(diagnostic.getDescription(),
                (Object[]) args), diagnostic.getSeverity());
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

    private class NullLocation implements Location {

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
