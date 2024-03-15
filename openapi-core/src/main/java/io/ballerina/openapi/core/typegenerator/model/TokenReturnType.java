package io.ballerina.openapi.core.typegenerator.model;

import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.HashMap;
import java.util.Optional;

public record TokenReturnType(Optional<Token> token, HashMap<String, TypeDefinitionNode> subtypeDefinitions) {
}
