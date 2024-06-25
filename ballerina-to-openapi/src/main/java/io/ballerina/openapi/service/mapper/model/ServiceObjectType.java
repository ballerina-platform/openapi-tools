package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;

public class ServiceObjectType implements ServiceNode {

    private static final String DEFAULT_PATH = "/";
    ObjectTypeDescriptorNode serviceObjType;
    TypeDefinitionNode serviceTypeDefinition;

    public ServiceObjectType(TypeDefinitionNode serviceType) {
        serviceTypeDefinition = new TypeDefinitionNode(serviceType.internalNode(), serviceType.position(),
                serviceType.parent());
        ObjectTypeDescriptorNode serviceObjTypeDesc = (ObjectTypeDescriptorNode) serviceType.typeDescriptor();
        serviceObjType = new ObjectTypeDescriptorNode(serviceObjTypeDesc.internalNode(), serviceObjTypeDesc.position(),
                serviceObjTypeDesc.parent());
    }

    public Optional<MetadataNode> metadata() {
        return serviceTypeDefinition.metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(serviceObjType.parent());
    }

    public SeparatedNodeList<ExpressionNode> expressions() {
        return createSeparatedNodeList();
    }

    public String absoluteResourcePath() {
        Optional<MetadataNode> metadata = this.metadata();
        if (metadata.isEmpty()) {
            return DEFAULT_PATH;
        }

        Optional<AnnotationNode> serviceConfigAnnotation = getServiceConfigAnnotation(metadata.get());
        if (serviceConfigAnnotation.isEmpty()) {
            return DEFAULT_PATH;
        }

        Optional<MappingConstructorExpressionNode> serviceConfig = serviceConfigAnnotation.get().annotValue();
        if (serviceConfig.isEmpty()) {
            return DEFAULT_PATH;
        }

        for (MappingFieldNode field : serviceConfig.get().fields()) {
            if (field.kind() == SyntaxKind.SPECIFIC_FIELD) {
                if (((SpecificFieldNode) field).fieldName().toString().trim().equals("basePath")) {
                    Optional<ExpressionNode> expressionNode = ((SpecificFieldNode) field).valueExpr();
                    if (expressionNode.isPresent() && expressionNode.get() instanceof BasicLiteralNode literalNode) {
                        return literalNode.literalToken().text().trim().replaceAll("\"", "");
                    }
                }
            }
        }
        return DEFAULT_PATH;
    }

    private Optional<AnnotationNode> getServiceConfigAnnotation(MetadataNode metadataNode) {
        NodeList<AnnotationNode> annotations = metadataNode.annotations();
        for (AnnotationNode annotation : annotations) {
            Node annotReference = annotation.annotReference();
            String annotName = annotReference.toString();
            if (annotReference.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                continue;
            }
            String[] annotStrings = annotName.split(":");
            if ("ServiceConfig".equals(annotStrings[annotStrings.length - 1].trim()) &&
                    "http".equals(annotStrings[0].trim())) {
                return Optional.of(annotation);
            }
        }
        return Optional.empty();
    }

    public NodeList<Node> members() {
        return serviceObjType.members();
    }

    public Kind kind() {
        return Kind.SERVICE_OBJECT_TYPE;
    }

    public Location location() {
        return serviceObjType.location();
    }

    public int getServiceId() {
        return serviceObjType.hashCode();
    }

    public void updateAnnotations(NodeList<AnnotationNode> newAnnotations) {
        Optional<MetadataNode> metadataOpt = serviceTypeDefinition.metadata();
        MetadataNode updatedMetadata;
        if (metadataOpt.isEmpty()) {
            updatedMetadata = createMetadataNode(null, newAnnotations);
        } else {
            updatedMetadata = metadataOpt.get().modify().withAnnotations(newAnnotations).apply();
        }
        serviceTypeDefinition = serviceTypeDefinition.modify().withMetadata(updatedMetadata).apply();
    }

    public ModuleMemberDeclarationNode getInternalNode() {
        return serviceTypeDefinition;
    }
}
