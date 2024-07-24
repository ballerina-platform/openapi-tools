package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_SERVICE_CONTRACT;
import static io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper.generateOasFroServiceNode;

public class ServiceDeclaration implements ServiceNode {

    ServiceDeclarationNode serviceDeclarationNode;
    int serviceId;
    TypeReferenceTypeSymbol serviceContractType;

    public ServiceDeclaration(ServiceDeclarationNode serviceNode, SemanticModel semanticModel) {
        serviceId = serviceNode.hashCode();
        serviceDeclarationNode = new ServiceDeclarationNode(serviceNode.internalNode(),
                serviceNode.position(), serviceNode.parent());
        serviceContractType = extractServiceContractType(serviceNode, semanticModel);
    }

    private static TypeReferenceTypeSymbol extractServiceContractType(ServiceDeclarationNode serviceNode,
                                                                      SemanticModel semanticModel) {
        Optional<TypeDescriptorNode> typeDescriptorNode = serviceNode.typeDescriptor();
        if (typeDescriptorNode.isEmpty()) {
            return null;
        }

        Optional<Symbol> symbol = semanticModel.symbol(typeDescriptorNode.get());
        if (symbol.isEmpty() || !(symbol.get() instanceof TypeReferenceTypeSymbol typeSymbol)) {
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

    public Optional<OpenAPI> getOpenAPIFromServiceContract(Package pkg, SemanticModel semanticModel,
                                                           Set<ServiceContractType> serviceContractTypes,
                                                           List<OpenAPIMapperDiagnostic> diagnostics) {
        Optional<String> jsonOpenApi = getOpenAPISpecFromResources(pkg, semanticModel, serviceContractTypes,
                diagnostics);
        if (jsonOpenApi.isEmpty()) {
            return Optional.empty();
        }

        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setFlatten(true);
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(jsonOpenApi.get(), null, parseOptions);
        return Optional.of(parseResult.getOpenAPI());
    }

    private Optional<String> getOpenAPISpecFromResources(Package pkg, SemanticModel semanticModel,
                                                         Set<ServiceContractType> serviceContractTypes,
                                                         List<OpenAPIMapperDiagnostic> diagnostics) {
        Optional<String> serviceName = serviceContractType.getName();
        if (serviceName.isEmpty()) {
            return Optional.empty();
        }

        String openApiFileName = serviceName.get() + ".json";

        Optional<ModuleSymbol> serviceContractModule = serviceContractType.getModule();
        if (serviceContractModule.isEmpty()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_133, serviceName.get()));
            return Optional.empty();
        }
        ModuleID serviceContractModuleId = serviceContractModule.get().id();

        Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceDeclarationNode);
        if (serviceSymbol.isEmpty()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_133, serviceName.get()));
            return Optional.empty();
        }

        Optional<ModuleSymbol> currentModule = serviceSymbol.get().getModule();
        if (currentModule.isEmpty()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_133, serviceName.get()));
            return Optional.empty();
        }
        ModuleID currentModuleId = currentModule.get().id();

        if (currentModuleId.orgName().equals(serviceContractModuleId.orgName()) && currentModuleId.moduleName()
                .equals(serviceContractModuleId.moduleName())) {
            Optional<ServiceContractType> serviceContract = serviceContractTypes.stream()
                    .filter(contractType -> contractType.matchesName(serviceName.get()))
                    .findFirst();
            if (serviceContract.isEmpty()) {
                diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_133, serviceName.get()));
                return Optional.empty();
            }
            OASResult oasResult = generateOasFroServiceNode(pkg.project(), serviceName.get(), semanticModel, null,
                    serviceContract.get());
            if (oasResult.getDiagnostics().stream()
                    .anyMatch(diagnostic -> diagnostic.getDiagnosticSeverity().equals(DiagnosticSeverity.ERROR))) {
                diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_132, serviceName.get()));
                return Optional.empty();
            }
            return oasResult.getJson();
        }

        Optional<ResolvedPackageDependency> resolvedPackage = pkg.getResolution().allDependencies().stream()
                .filter(dependency -> {
                    PackageDescriptor descriptor = dependency.packageInstance().descriptor();
                    return descriptor.org().value().equals(serviceContractModuleId.orgName()) &&
                            descriptor.name().value().equals(serviceContractModuleId.packageName());
                })
                .findFirst();
        if (resolvedPackage.isEmpty()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_134, String.format("%s/%s:%s",
                    serviceContractModuleId.orgName(), serviceContractModuleId.packageName(), serviceName.get())));
            return Optional.empty();
        }

        Module defaultModule = resolvedPackage.get().packageInstance().getDefaultModule();
        Optional<DocumentId> openApiDocument = defaultModule.resourceIds().stream()
                .filter(resourceId -> resourceId.toString().contains(openApiFileName))
                .findFirst();
        if (openApiDocument.isEmpty()) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_135, String.format("%s/%s:%s",
                    serviceContractModuleId.orgName(), serviceContractModuleId.packageName(), serviceName.get())));
            return Optional.empty();
        }

        byte[] openApiContent = defaultModule.resource(openApiDocument.get()).content();
        return Optional.of(new String(openApiContent, StandardCharsets.UTF_8));
    }
}
