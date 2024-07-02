package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
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
    int serviceId;

    public ServiceObjectType(TypeDefinitionNode serviceType) {
        serviceId = serviceType.hashCode();
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
        return semanticModel.symbol(serviceTypeDefinition);
    }

    public Optional<TypeSymbol> typeDescriptor(SemanticModel semanticModel) {
        Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceTypeDefinition);
        if (serviceSymbol.isEmpty() || !(serviceSymbol.get() instanceof TypeDefinitionSymbol serviceType)) {
            return Optional.empty();
        }
        return Optional.of(serviceType.typeDescriptor());
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
            if (field.kind() == SyntaxKind.SPECIFIC_FIELD &&
                    ((SpecificFieldNode) field).fieldName().toString().trim().equals("basePath")) {
                Optional<ExpressionNode> expressionNode = ((SpecificFieldNode) field).valueExpr();
                if (expressionNode.isPresent() && expressionNode.get() instanceof BasicLiteralNode literalNode) {
                    return literalNode.literalToken().text().trim().replaceAll("\"", "");
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
        return serviceId;
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

    public Optional<TypeSymbol> getInterceptorReturnType(SemanticModel semanticModel) {
        Optional<Symbol> symbol = semanticModel.symbol(serviceTypeDefinition);
        if (symbol.isEmpty() || !(symbol.get() instanceof TypeDefinitionSymbol serviceTypeDef)) {
            return Optional.empty();
        }

        TypeSymbol serviceTypeSymbol = serviceTypeDef.typeDescriptor();
        if (!(serviceTypeSymbol instanceof ObjectTypeSymbol serviceType)) {
            return Optional.empty();
        }

        MethodSymbol methodSymbol = serviceType.methods().get("createInterceptors");
        if (methodSymbol == null) {
            return Optional.empty();
        }
        return methodSymbol.typeDescriptor().returnTypeDescriptor();
    }
}
