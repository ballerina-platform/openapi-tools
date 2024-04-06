package io.ballerina.openapi.core.generators.type.model;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.HashMap;
import java.util.Optional;

public record TypeGeneratorResult(Optional<TypeDescriptorNode> typeDescriptorNode, HashMap<String,
        TypeDefinitionNode> subtypeDefinitions) {
}
