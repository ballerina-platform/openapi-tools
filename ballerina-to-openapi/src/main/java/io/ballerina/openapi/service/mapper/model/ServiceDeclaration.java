package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_SERVICE_CONTRACT;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_SERVICE_CONTRACT_INFO;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_DEFINITION;

public class ServiceDeclaration implements ServiceNode {

    ServiceDeclarationNode serviceDeclarationNode;
    int serviceId;
    TypeSymbol serviceContractType;

    public ServiceDeclaration(ServiceDeclarationNode serviceNode, SemanticModel semanticModel) {
        serviceId = serviceNode.hashCode();
        serviceDeclarationNode = new ServiceDeclarationNode(serviceNode.internalNode(),
                serviceNode.position(), serviceNode.parent());
        serviceContractType = extractServiceContractType(serviceNode, semanticModel);
    }

    private static TypeSymbol extractServiceContractType(ServiceDeclarationNode serviceNode,
                                                         SemanticModel semanticModel) {
        Optional<TypeDescriptorNode> typeDescriptorNode = serviceNode.typeDescriptor();
        if (typeDescriptorNode.isEmpty()) {
            return null;
        }

        Optional<Symbol> symbol = semanticModel.symbol(typeDescriptorNode.get());
        if (symbol.isEmpty() || !(symbol.get() instanceof TypeSymbol typeSymbol)) {
            return null;
        }

        Optional<Symbol> serviceContractType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                HTTP_SERVICE_CONTRACT);
        if (serviceContractType.isEmpty() ||
                !(serviceContractType.get() instanceof TypeDefinitionSymbol serviceContractTypeDef)) {
            return null;
        }

        return typeSymbol.subtypeOf(serviceContractTypeDef.typeDescriptor()) ? typeSymbol : null;
    }

    public Optional<MetadataNode> metadata() {
        return serviceDeclarationNode.metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(serviceDeclarationNode);
    }

    public Optional<TypeSymbol> typeDescriptor(SemanticModel semanticModel) {
        Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceDeclarationNode);
        if (serviceSymbol.isEmpty() || !(serviceSymbol.get() instanceof ServiceDeclarationSymbol serviceType)) {
            return Optional.empty();
        }
        return serviceType.typeDescriptor();
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

    public Location location() {
        return serviceDeclarationNode.location();
    }

    public int getServiceId() {
        return serviceId;
    }

    public void updateAnnotations(NodeList<AnnotationNode> newAnnotations) {
        Optional<MetadataNode> metadataOpt = serviceDeclarationNode.metadata();
        MetadataNode updatedMetadata;
        if (metadataOpt.isEmpty()) {
            updatedMetadata = createMetadataNode(null, newAnnotations);
        } else {
            updatedMetadata = metadataOpt.get().modify().withAnnotations(newAnnotations).apply();
        }
        serviceDeclarationNode = serviceDeclarationNode.modify().withMetadata(updatedMetadata).apply();
    }

    public ModuleMemberDeclarationNode getInternalNode() {
        return serviceDeclarationNode;
    }

    public Optional<TypeSymbol> getInterceptorReturnType(SemanticModel semanticModel) {
        Optional<Symbol> symbol = semanticModel.symbol(serviceDeclarationNode);
        if (symbol.isEmpty() || !(symbol.get() instanceof ServiceDeclarationSymbol serviceType)) {
            return Optional.empty();
        }

        MethodSymbol methodSymbol = serviceType.methods().get("createInterceptors");
        if (Objects.isNull(methodSymbol)) {
            return Optional.empty();
        }
        return methodSymbol.typeDescriptor().returnTypeDescriptor();
    }

    public boolean implementsServiceContract() {
        return Objects.nonNull(serviceContractType);
    }

    public Optional<OpenAPI> getOpenAPIFromServiceContract(SemanticModel semanticModel) {
        Optional<String> encodedOpenAPISpec = getEncodedOpenAPISpec(semanticModel);
        if (encodedOpenAPISpec.isEmpty()) {
            return Optional.empty();
        }

        byte[] openAPISpec = Base64.getDecoder().decode(encodedOpenAPISpec.get());
        String jsonOpenAPIString = new String(openAPISpec, StandardCharsets.UTF_8);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setFlatten(true);
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(jsonOpenAPIString, null, parseOptions);
        return Optional.of(parseResult.getOpenAPI());
    }

    private Optional<String> getEncodedOpenAPISpec(SemanticModel semanticModel) {
        if (Objects.isNull(serviceContractType) ||
                !(serviceContractType instanceof TypeReferenceTypeSymbol serviceContractTypeRef)) {
            return Optional.empty();
        }

        if (!(serviceContractTypeRef.definition() instanceof Annotatable annotatableServiceContractType)) {
            return Optional.empty();
        }

        Optional<Symbol> serviceContractInfoSymbol = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                HTTP_SERVICE_CONTRACT_INFO);
        if (serviceContractInfoSymbol.isEmpty() ||
                !(serviceContractInfoSymbol.get() instanceof TypeDefinitionSymbol serviceContractInfoType)) {
            return Optional.empty();
        }

        Optional<AnnotationAttachmentSymbol> serviceContractInfo = annotatableServiceContractType.annotAttachments()
                .stream()
                .filter(annotAttachment -> annotAttachment.typeDescriptor().typeDescriptor().isPresent() &&
                        annotAttachment.typeDescriptor().typeDescriptor().get().subtypeOf(
                                serviceContractInfoType.typeDescriptor()))
                .findFirst();
        if (serviceContractInfo.isEmpty()) {
            return Optional.empty();
        }

        Optional<ConstantValue> annotationValue = serviceContractInfo.get().attachmentValue();
        if (annotationValue.isEmpty() || !(annotationValue.get().value() instanceof HashMap<?, ?> annotationValueMap)) {
            return Optional.empty();
        }

        Object openAPIDef = annotationValueMap.get(OPENAPI_DEFINITION);
        if (Objects.isNull(openAPIDef) || !(openAPIDef instanceof ConstantValue openAPIDefValue)) {
            return Optional.empty();
        }

        return openAPIDefValue.value() instanceof String openAPIDefString ?
                Optional.of(openAPIDefString) : Optional.empty();
    }
}
