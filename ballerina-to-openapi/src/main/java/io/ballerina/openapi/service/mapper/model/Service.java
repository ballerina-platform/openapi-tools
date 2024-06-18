package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;

import java.util.Optional;

public interface Service {

    Optional<MetadataNode> metadata();

    Optional<Symbol> getSymbol(SemanticModel semanticModel);

    SeparatedNodeList<ExpressionNode> expressions();

    NodeList<Node> absoluteResourcePath();

    NodeList<Node> members();

    ChildNodeList children();
}
