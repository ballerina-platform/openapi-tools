/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapperImpl;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.OpenAPIExampleMapper;
import io.ballerina.openapi.service.mapper.example.OpenAPIExampleMapperImpl;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapper;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapperImpl;
import io.ballerina.openapi.service.mapper.interceptor.model.RequestParameterInfo;
import io.ballerina.openapi.service.mapper.interceptor.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.interceptor.pipeline.InterceptorPipeline;
import io.ballerina.openapi.service.mapper.metainfo.MetaInfoMapper;
import io.ballerina.openapi.service.mapper.metainfo.MetaInfoMapperImpl;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.model.ResourceFunction;
import io.ballerina.openapi.service.mapper.model.ServiceNode;
import io.ballerina.openapi.service.mapper.parameter.DefaultParameterMapper;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapper;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapperWithInterceptors;
import io.ballerina.openapi.service.mapper.response.DefaultResponseMapper;
import io.ballerina.openapi.service.mapper.response.ResponseMapper;
import io.ballerina.openapi.service.mapper.response.ResponseMapperWithInterceptors;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperImpl;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_SERVICE_CONFIG;
import static io.ballerina.openapi.service.mapper.Constants.INTERCEPTABLE_SERVICE;
import static io.ballerina.openapi.service.mapper.Constants.TREAT_NILABLE_AS_OPTIONAL;
import static io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper.oasAvailableViaServiceContract;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractServiceAnnotationDetails;

/**
 * This {@link ServiceMapperFactory} class represents the factory for creating mappers for the ballerina service to
 * OpenAPI mapping.
 *
 * @since 1.9.0
 */
public class ServiceMapperFactory {

    private final OpenAPI openAPI;
    private final AdditionalData additionalData;
    private final Boolean treatNilableAsOptional;

    private final TypeMapper typeMapper;
    private final ConstraintMapper constraintMapper;
    private final HateoasMapper hateoasMapper;
    private final InterceptorPipeline interceptorPipeline;
    private final MetaInfoMapper metaInfoMapper;
    private final OpenAPIExampleMapper exampleMapper;

    public ServiceMapperFactory(OpenAPI openAPI, SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor,
                                List<OpenAPIMapperDiagnostic> diagnostics, ServiceNode serviceDefinition,
                                boolean enableBallerinaExt) {
        this.additionalData = new AdditionalData(semanticModel, moduleMemberVisitor, diagnostics, enableBallerinaExt);
        this.treatNilableAsOptional = isTreatNilableAsOptionalParameter(serviceDefinition);
        this.openAPI = openAPI;
        // Skip interceptor pipeline build for services implemented via service contract
        // since the OpenAPI generation with interceptors is already handled by the service contract
        this.interceptorPipeline = oasAvailableViaServiceContract(serviceDefinition) ? null :
                getInterceptorPipeline(serviceDefinition, additionalData);

        this.typeMapper = new TypeMapperImpl(getComponents(openAPI), additionalData);
        this.constraintMapper = new ConstraintMapperImpl(openAPI, moduleMemberVisitor, diagnostics);
        this.hateoasMapper = new HateoasMapperImpl();
        this.metaInfoMapper = new MetaInfoMapperImpl();
        this.exampleMapper = new OpenAPIExampleMapperImpl(openAPI, serviceDefinition, additionalData);
    }

    public ServiceMapperFactory(OpenAPI openAPI, SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor,
                                List<OpenAPIMapperDiagnostic> diagnostics, ServiceNode serviceDefinition) {
        this(openAPI, semanticModel, moduleMemberVisitor, diagnostics, serviceDefinition, false);
    }

    public ServersMapper getServersMapper(Set<ListenerDeclarationNode> endpoints, ServiceNode serviceNode) {
        return new ServersMapperImpl(openAPI, additionalData, endpoints, serviceNode);
    }

    public ResourceMapper getResourceMapper(List<ResourceFunction> resources) {
        return new ResourceMapperImpl(openAPI, resources, additionalData, this);
    }

    public ParameterMapper getParameterMapper(ResourceFunction resourceNode, Map<String, String> apiDocs,
                                              OperationInventory opInventory) {
        if (Objects.nonNull(interceptorPipeline)) {
            RequestParameterInfo reqParamInfoFromInterceptors = getRequestParameterInfoFromInterceptors(resourceNode,
                    additionalData, interceptorPipeline);
            return new ParameterMapperWithInterceptors(resourceNode, opInventory, apiDocs, additionalData,
                    treatNilableAsOptional, reqParamInfoFromInterceptors, this);
        }
        return new DefaultParameterMapper(resourceNode, opInventory, apiDocs, additionalData, treatNilableAsOptional,
                this);
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public ResponseMapper getResponseMapper(ResourceFunction resourceNode, OperationInventory opInventory) {
        if (Objects.nonNull(interceptorPipeline)) {
            ResponseInfo responseInfoFromInterceptors = getResponseInfoFromInterceptors(resourceNode,
                    additionalData, interceptorPipeline);
            return new ResponseMapperWithInterceptors(resourceNode, opInventory, additionalData,
                    responseInfoFromInterceptors, this);
        }
        return new DefaultResponseMapper(resourceNode, opInventory, additionalData, this);
    }

    private ResponseInfo getResponseInfoFromInterceptors(ResourceFunction resourceNode,
                                                         AdditionalData additionalData,
                                                         InterceptorPipeline interceptorPipeline) {
        final ResponseInfo responseInfoFromInterceptors;
        Optional<Symbol> symbol = resourceNode.getSymbol(additionalData.semanticModel());
        if (symbol.isPresent() && symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol) {
            responseInfoFromInterceptors = interceptorPipeline.getInfoFromInterceptors(resourceMethodSymbol);
        } else {
            responseInfoFromInterceptors = new ResponseInfo(false);
        }
        return responseInfoFromInterceptors;
    }

    private RequestParameterInfo getRequestParameterInfoFromInterceptors(ResourceFunction resourceNode,
                                                                         AdditionalData additionalData,
                                                                         InterceptorPipeline interceptorPipeline) {
        final RequestParameterInfo requestParameterInfoFromInterceptors;
        Optional<Symbol> symbol = resourceNode.getSymbol(additionalData.semanticModel());
        if (symbol.isPresent() && symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol) {
            requestParameterInfoFromInterceptors = interceptorPipeline.getRequestParameterInfo(resourceMethodSymbol);
        } else {
            requestParameterInfoFromInterceptors = new RequestParameterInfo(additionalData.semanticModel());
        }
        return requestParameterInfoFromInterceptors;
    }

    public ConstraintMapper getConstraintMapper() {
        return constraintMapper;
    }

    public HateoasMapper getHateoasMapper() {
        return hateoasMapper;
    }

    public MetaInfoMapper getMetaInfoMapper() {
            return metaInfoMapper;
    }

    public OpenAPIExampleMapper getExampleMapper() {
        return exampleMapper;
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

    private static boolean isTreatNilableAsOptionalParameter(ServiceNode serviceNode) {
        if (serviceNode.metadata().isEmpty()) {
            return true;
        }

        MetadataNode metadataNode = serviceNode.metadata().get();
        NodeList<AnnotationNode> annotations = metadataNode.annotations();

        if (!annotations.isEmpty()) {
            Optional<String> values = extractServiceAnnotationDetails(annotations, HTTP_SERVICE_CONFIG,
                    TREAT_NILABLE_AS_OPTIONAL);
            if (values.isPresent()) {
                return values.get().equals(Constants.TRUE);
            }
        }
        return true;
    }

    private static boolean isInterceptableService(ServiceNode serviceDefinition, SemanticModel semanticModel) {
        Optional<TypeSymbol> serviceType = serviceDefinition.typeDescriptor(semanticModel);
        if (serviceType.isEmpty()) {
            return false;
        }

        Optional<Symbol> interceptableServiceType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                INTERCEPTABLE_SERVICE);
        if (interceptableServiceType.isEmpty() || !(interceptableServiceType.get() instanceof TypeDefinitionSymbol
                interceptableServiceTypeSymbol)) {
            return false;
        }

        return serviceType.get().subtypeOf(interceptableServiceTypeSymbol.typeDescriptor());
    }

    private static InterceptorPipeline getInterceptorPipeline(ServiceNode serviceDefinition,
                                                              AdditionalData additionalData) {
        if (!isInterceptableService(serviceDefinition, additionalData.semanticModel())) {
            return null;
        }
        return InterceptorPipeline.build(serviceDefinition, additionalData);
    }
}
