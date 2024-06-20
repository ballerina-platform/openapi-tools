package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

import java.util.Optional;

public class ServiceDeclaration implements ServiceNode {

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

    public String absoluteResourcePath() {
        StringBuilder currentServiceName = new StringBuilder();
        NodeList<Node> serviceNameNodes = serviceDeclarationNode.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName.append(MapperCommonUtils.unescapeIdentifier(serviceBasedPathNode.toString()));
        }
        return currentServiceName.toString().trim();
    }

    public NodeList<Node> members() {
        return serviceDeclarationNode.members();
    }

    public Kind kind() {
        return Kind.SERVICE_DECLARATION;
    }
}
