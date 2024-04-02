package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.HashMap;
import java.util.List;

public record ConstraintResult(HashMap<String, TypeDefinitionNode> typeDefinitionNodeHashMap,
                               boolean isConstraintAvailable, List<Diagnostic> diagnostics) {
}
