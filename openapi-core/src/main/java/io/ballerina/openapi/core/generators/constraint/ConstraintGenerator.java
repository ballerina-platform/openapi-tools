package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;

public interface ConstraintGenerator {
    ConstraintResult updateTypeDefinitionsWithConstraints();
    List<Diagnostic> getDiagnostics();
}
