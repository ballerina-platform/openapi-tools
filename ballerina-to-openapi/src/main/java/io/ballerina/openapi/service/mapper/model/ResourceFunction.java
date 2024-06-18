package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public interface ResourceFunction {

    NodeList<Node> relativeResourcePath();

    String functionName();

    Location location();

    Optional<MetadataNode> metadata();

    Optional<Symbol> getSymbol(SemanticModel semanticModel);

    FunctionSignatureNode functionSignature();

    Optional<MetadataNode> parentMetaData();
}
