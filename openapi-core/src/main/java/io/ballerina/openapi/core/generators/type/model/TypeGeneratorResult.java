package io.ballerina.openapi.core.generators.type.model;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public record TypeGeneratorResult(Optional<TypeDescriptorNode> typeDescriptorNode, HashMap<String, TypeDefinitionNode> subtypeDefinitions, List<Diagnostic> diagnosticsq) {
}
