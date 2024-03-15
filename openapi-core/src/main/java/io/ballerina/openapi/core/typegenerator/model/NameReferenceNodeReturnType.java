package io.ballerina.openapi.core.typegenerator.model;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.HashMap;
import java.util.Optional;

public record NameReferenceNodeReturnType(Optional<SimpleNameReferenceNode> nameReferenceNode, HashMap<String, TypeDefinitionNode> subtypeDefinitions) {
}
