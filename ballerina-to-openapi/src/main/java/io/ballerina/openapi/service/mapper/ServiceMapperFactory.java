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
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapper;
import io.ballerina.openapi.service.mapper.constraint.ConstraintMapperImpl;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapper;
import io.ballerina.openapi.service.mapper.hateoas.HateoasMapperImpl;
import io.ballerina.openapi.service.mapper.interceptor.model.RequestParameterInfo;
import io.ballerina.openapi.service.mapper.interceptor.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.interceptor.pipeline.InterceptorPipeline;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
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

    public ServiceMapperFactory(OpenAPI openAPI, SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor,
                                List<OpenAPIMapperDiagnostic> diagnostics, ServiceDeclarationNode serviceDefinition) {
        this.additionalData = new AdditionalData(semanticModel, moduleMemberVisitor, diagnostics);
        this.treatNilableAsOptional = isTreatNilableAsOptionalParameter(serviceDefinition);
        this.openAPI = openAPI;
        this.interceptorPipeline = getInterceptorPipeline(serviceDefinition, additionalData);

        this.typeMapper = new TypeMapperImpl(getComponents(openAPI), additionalData);
        this.constraintMapper = new ConstraintMapperImpl(openAPI, moduleMemberVisitor, diagnostics);
        this.hateoasMapper = new HateoasMapperImpl();
    }

    public ServersMapper getServersMapper(Set<ListenerDeclarationNode> endpoints, ServiceDeclarationNode serviceNode) {
        return new ServersMapperImpl(openAPI, endpoints, serviceNode);
    }

    public ResourceMapper getResourceMapper(List<FunctionDefinitionNode> resources) {
        return new ResourceMapperImpl(openAPI, resources, additionalData, this);
    }

    public ParameterMapper getParameterMapper(FunctionDefinitionNode resourceNode, Map<String, String> apiDocs,
                                              OperationInventory opInventory) {
        if (Objects.nonNull(interceptorPipeline)) {
            RequestParameterInfo reqParamInfoFromInterceptors = getRequestParameterInfoFromInterceptors(resourceNode,
                    opInventory, additionalData, interceptorPipeline);
            return new ParameterMapperWithInterceptors(resourceNode, opInventory, apiDocs, additionalData,
                    treatNilableAsOptional, reqParamInfoFromInterceptors, this);
        }
        return new DefaultParameterMapper(resourceNode, opInventory, apiDocs, additionalData, treatNilableAsOptional,
                this);
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public ResponseMapper getResponseMapper(FunctionDefinitionNode resourceNode, OperationInventory opInventory) {
        if (Objects.nonNull(interceptorPipeline)) {
            ResponseInfo responseInfoFromInterceptors = getResponseInfoFromInterceptors(resourceNode, opInventory,
                    additionalData, interceptorPipeline);
            return new ResponseMapperWithInterceptors(resourceNode, opInventory, additionalData,
                    responseInfoFromInterceptors, this);
        }
        return new DefaultResponseMapper(resourceNode, opInventory, additionalData, this);
    }

    private ResponseInfo getResponseInfoFromInterceptors(FunctionDefinitionNode resourceNode,
                                                         OperationInventory operationInventory,
                                                         AdditionalData additionalData,
                                                         InterceptorPipeline interceptorPipeline) {
        final ResponseInfo responseInfoFromInterceptors;
        Optional<Symbol> symbol = additionalData.semanticModel().symbol(resourceNode);
        if (symbol.isPresent() && symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol) {
            responseInfoFromInterceptors = interceptorPipeline.getInfoFromInterceptors(resourceMethodSymbol);
        } else {
            responseInfoFromInterceptors = new ResponseInfo(false);
        }
        return responseInfoFromInterceptors;
    }

    private RequestParameterInfo getRequestParameterInfoFromInterceptors(FunctionDefinitionNode resourceNode,
                                                                         OperationInventory operationInventory,
                                                                         AdditionalData additionalData,
                                                                         InterceptorPipeline interceptorPipeline) {
        final RequestParameterInfo requestParameterInfoFromInterceptors;
        Optional<Symbol> symbol = additionalData.semanticModel().symbol(resourceNode);
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

    private static boolean isTreatNilableAsOptionalParameter(ServiceDeclarationNode serviceNode) {
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

    private static boolean isInterceptableService(ServiceDeclarationNode serviceDefinition,
                                                  SemanticModel semanticModel) {
        boolean[] isInterceptable = {false};
        semanticModel.symbol(serviceDefinition).ifPresent(symbol -> {
            if (symbol instanceof ServiceDeclarationSymbol serviceSymbol) {
                serviceSymbol.typeDescriptor().ifPresent(serviceType ->
                    semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY, INTERCEPTABLE_SERVICE).ifPresent(
                        typeSymbol -> {
                            if (typeSymbol instanceof TypeDefinitionSymbol interceptableServiceType) {
                                isInterceptable[0] = serviceType.subtypeOf(interceptableServiceType.typeDescriptor());
                            }
                        }
                    ));
            }
        });
        return isInterceptable[0];
    }

    private static InterceptorPipeline getInterceptorPipeline(ServiceDeclarationNode serviceDefinition,
                                                              AdditionalData additionalData) {
        if (!isInterceptableService(serviceDefinition, additionalData.semanticModel())) {
            return null;
        }
        return InterceptorPipeline.build(serviceDefinition, additionalData);
    }
}
