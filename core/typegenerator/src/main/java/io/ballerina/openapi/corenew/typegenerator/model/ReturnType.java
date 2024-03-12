package io.ballerina.openapi.corenew.typegenerator.model;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.Optional;

public record ReturnType(String recordName, Optional<TypeDescriptorNode> nameReferenceNode, Optional<TypeDefinitionNode> originalType) {
}
