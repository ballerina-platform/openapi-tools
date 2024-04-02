package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.swagger.v3.oas.models.OpenAPI;

public class ClientDocCommentGenerator implements DocCommentsGenerator {
    OpenAPI openAPI;
    SyntaxTree syntaxTree;

    public ClientDocCommentGenerator(SyntaxTree syntaxTree, OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
    }
    @Override
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        return null;
    }
}
