package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapperImpl;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapper;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapperImpl;
import io.ballerina.openapi.service.mapper.interceptor.InterceptorPipeline;
import io.ballerina.openapi.service.mapper.interceptor.ReturnTypes;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapperImpl;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapper;
import io.ballerina.openapi.service.mapper.response.ResponseMapper;
import io.ballerina.openapi.service.mapper.response.DefaultResponseMapper;
import io.ballerina.openapi.service.mapper.response.ResponseMapperWithInterceptors;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperImpl;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class ServiceMapperFactory {

    private final OpenAPI openAPI;
    private final AdditionalData additionalData;
    private final Boolean treatNilableAsOptional;

    private final TypeMapper typeMapper;
    private final ConstraintMapper constraintMapper;
    private final HateoasMapper hateoasMapper;
    private final InterceptorPipeline interceptorPipeline;

    public ServiceMapperFactory(OpenAPI openAPI, SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor,
                                List<OpenAPIMapperDiagnostic> diagnostics, Boolean treatNilableAsOptional,
                                InterceptorPipeline interceptorPipeline) {
        this.additionalData = new AdditionalData(semanticModel, moduleMemberVisitor, diagnostics);
        this.treatNilableAsOptional = treatNilableAsOptional;
        this.openAPI = openAPI;
        this.interceptorPipeline = interceptorPipeline;

        this.typeMapper = new TypeMapperImpl(getComponents(openAPI), additionalData);
        this.constraintMapper = new ConstraintMapperImpl(openAPI, moduleMemberVisitor, diagnostics);
        this.hateoasMapper = new HateoasMapperImpl();
    }

    public ServersMapper getServersMapper(Set<ListenerDeclarationNode> endpoints, ServiceDeclarationNode serviceNode) {
        return new ServersMapperImpl(openAPI, endpoints, serviceNode);
    }

    public ResourceMapper getResourceMapper(List<FunctionDefinitionNode> resources) {
        for (FunctionDefinitionNode resource : resources) {
            ResourceMethodSymbol resourceMethodSymbol = (ResourceMethodSymbol) additionalData.semanticModel().
                    symbol(resource).get();
            ReturnTypes returnType = interceptorPipeline.getEffectiveReturnType(resourceMethodSymbol);
            System.out.println("Effective interceptor return type for resource: " + resourceMethodSymbol.getName().get() + " " +
                    resourceMethodSymbol.resourcePath().signature() + " is: " +
                    (Objects.isNull(returnType.fromInterceptors()) ? "null" : returnType.fromInterceptors().signature()));
            System.out.println("Effective target resource return type for resource: " + resourceMethodSymbol.getName().get() + " " +
                    resourceMethodSymbol.resourcePath().signature() + " is: " +
                    (Objects.isNull(returnType.fromTargetResource()) ? "null" : returnType.fromTargetResource().signature()));
            System.out.println("\n");
        }
        return new ResourceMapperImpl(openAPI, resources, additionalData, this);
    }

    public ParameterMapper getParameterMapper(FunctionDefinitionNode resourceNode, Map<String, String> apiDocs,
                                              OperationInventory opInventory) {
        return new ParameterMapperImpl(resourceNode, opInventory, apiDocs, additionalData, treatNilableAsOptional,
                this);
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public ResponseMapper getResponseMapper(FunctionDefinitionNode resourceNode, OperationInventory opInventory) {
        if (Objects.nonNull(interceptorPipeline)) {
            return new ResponseMapperWithInterceptors(resourceNode, opInventory, additionalData,
                    interceptorPipeline, this);
        }
        return new DefaultResponseMapper(resourceNode, opInventory, additionalData, this);
    }

    public ConstraintMapper getConstraintMapper() {
        return constraintMapper;
    }

    public HateoasMapper getHateoasMapper() {
        return hateoasMapper;
    }

    private Components getComponents(OpenAPI openAPI) {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            components = new Components();
            openAPI.setComponents(components);
        }
        if (components.getSchemas() == null) {
            components.setSchemas(new TreeMap<>());
        }
        return components;
    }
}
