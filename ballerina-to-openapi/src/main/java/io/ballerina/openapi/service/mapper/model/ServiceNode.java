package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public interface ServiceNode {

    enum Kind {
        SERVICE_OBJECT_TYPE,
        SERVICE_DECLARATION
    }

    Optional<MetadataNode> metadata();

    Optional<Symbol> getSymbol(SemanticModel semanticModel);

    Optional<TypeSymbol> typeDescriptor(SemanticModel semanticModel);

    SeparatedNodeList<ExpressionNode> expressions();

    String absoluteResourcePath();

    NodeList<Node> members();

    Kind kind();

    Location location();

    int getServiceId();

    void updateAnnotations(NodeList<AnnotationNode> newAnnotations);

    ModuleMemberDeclarationNode getInternalNode();

    Optional<TypeSymbol> getInterceptorReturnType(SemanticModel semanticModel);
}
