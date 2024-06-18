package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;

import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;

public class ServiceObjectType implements Service {

    ObjectTypeDescriptorNode serviceObjType;

    public ServiceObjectType(ObjectTypeDescriptorNode serviceType) {
        serviceObjType = new ObjectTypeDescriptorNode(serviceType.internalNode(), serviceType.position(),
                serviceType.parent());
    }

    public Optional<MetadataNode> metadata() {
        return ((TypeDefinitionNode) serviceObjType.parent()).metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(serviceObjType);
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        // TODO: Need to check the usage of these expressions
        return createSeparatedNodeList();
    }

    public NodeList<Node> absoluteResourcePath() {
        // TODO: Get the basePath from the annotation
        return createEmptyNodeList();
    }

    public NodeList<Node> members() {
        return serviceObjType.members();
    }

    public ChildNodeList children() {
        return serviceObjType.children();
    }
}
