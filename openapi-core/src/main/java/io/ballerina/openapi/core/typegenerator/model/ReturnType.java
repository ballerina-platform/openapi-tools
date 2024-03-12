package io.ballerina.openapi.core.typegenerator.model;

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;

import java.util.Optional;

public record ReturnType(String recordName, Optional<TypeDescriptorNode> nameReferenceNode, Optional<TypeDefinitionNode> originalType) {
}
