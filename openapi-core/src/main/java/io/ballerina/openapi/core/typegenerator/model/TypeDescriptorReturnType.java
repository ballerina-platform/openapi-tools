package io.ballerina.openapi.core.typegenerator.model;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.HashMap;
import java.util.Optional;

public record TypeDescriptorReturnType(Optional<TypeDescriptorNode> typeDescriptorNode, HashMap<String, TypeDefinitionNode> subtypeDefinitions) {
}
