package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.SyntaxTree;

public interface DocCommentsGenerator {
    SyntaxTree updateSyntaxTreeWithDocComments();
}
