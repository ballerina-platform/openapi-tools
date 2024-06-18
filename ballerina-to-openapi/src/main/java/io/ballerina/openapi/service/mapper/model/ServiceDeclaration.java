package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;

import java.util.Optional;

public class ServiceDeclaration implements Service {

    ServiceDeclarationNode serviceDeclarationNode;

    public ServiceDeclaration(ServiceDeclarationNode serviceNode) {
        serviceDeclarationNode = new ServiceDeclarationNode(serviceNode.internalNode(),
                serviceNode.position(), serviceNode.parent());
    }

    public Optional<MetadataNode> metadata() {
        return serviceDeclarationNode.metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(serviceDeclarationNode);
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        return serviceDeclarationNode.expressions();
    }

    public NodeList<Node> absoluteResourcePath() {
        return serviceDeclarationNode.absoluteResourcePath();
    }

    public NodeList<Node> members() {
        return serviceDeclarationNode.members();
    }

    public ChildNodeList children() {
        return serviceDeclarationNode.children();
    }
}
