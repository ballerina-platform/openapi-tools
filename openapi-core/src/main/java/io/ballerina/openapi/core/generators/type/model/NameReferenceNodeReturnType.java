package io.ballerina.openapi.core.generators.type.model;

import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.HashMap;
import java.util.Optional;

public record NameReferenceNodeReturnType(Optional<SimpleNameReferenceNode> nameReferenceNode, HashMap<String, TypeDefinitionNode> subtypeDefinitions) {
}
