/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationBuilder;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapper;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapperInterface;
import io.ballerina.openapi.service.mapper.response.ResponseMapper;
import io.ballerina.openapi.service.mapper.response.ResponseMapperInterface;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static io.ballerina.openapi.service.mapper.Constants.DEFAULT;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOperationId;

/**
 * This class will do resource mapping from ballerina to openApi.
 *
 * @since 2.0.0
 */
public class ResourceMapper implements ResourceMapperInterface {
    private final Paths pathObject = new Paths();
    private final AdditionalData additionalData;
    private final OpenAPI openAPI;
    private final List<FunctionDefinitionNode> resources;
    private final boolean treatNilableAsOptional;

    /**
     * Initializes a resource parser for openApi.
     */
    ResourceMapper(OpenAPI openAPI, List<FunctionDefinitionNode> resources, AdditionalData additionalData,
                   boolean treatNilableAsOptional) {
        this.openAPI = openAPI;
        this.resources = resources;
        this.additionalData = additionalData;
        this.treatNilableAsOptional = treatNilableAsOptional;
    }

    public void addMapping() {
        Components components = openAPI.getComponents();
        if (components == null) {
            components = new Components();
            openAPI.setComponents(components);
        }
        if (components.getSchemas() == null) {
            components.setSchemas(new TreeMap<>());
        }
        for (FunctionDefinitionNode resource : resources) {
            generateResourceMapping(resource, components);
        }
        if (components.getSchemas().isEmpty()) {
            openAPI.setComponents(null);
        }
        openAPI.setPaths(pathObject);
    }

    private void generateResourceMapping(FunctionDefinitionNode resource, Components components) {
        String path = MapperCommonUtils.unescapeIdentifier(generateRelativePath(resource));
        String httpMethod = resource.functionName().toString().trim();
        if (httpMethod.equals("'" + DEFAULT) || httpMethod.equals(DEFAULT)) {
            DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_100;
            IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                    resource.location());
            additionalData.diagnostics().add(error);
        } else {
            Optional<OperationBuilder> operationBuilder = convertResourceToOperation(resource, httpMethod, path,
                    components);
            if (operationBuilder.isPresent()) {
                Operation operation = operationBuilder.get().getOperation();
                generatePathItem(httpMethod, pathObject, operation, path);
            }
        }
    }

    private void generatePathItem(String httpMethod, Paths path, Operation operation, String pathName) {
        PathItem pathItem = new PathItem();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case Constants.GET -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setGet(operation);
                } else {
                    pathItem.setGet(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.PUT -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPut(operation);
                } else {
                    pathItem.setPut(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.POST -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPost(operation);
                } else {
                    pathItem.setPost(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.DELETE -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setDelete(operation);
                } else {
                    pathItem.setDelete(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.OPTIONS -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setOptions(operation);
                } else {
                    pathItem.setOptions(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.PATCH -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPatch(operation);
                } else {
                    pathItem.setPatch(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.HEAD -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setHead(operation);
                } else {
                    pathItem.setHead(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            default -> { }
        }
    }

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
     *
     * @return Operation Adaptor object of given resource
     */
    private Optional<OperationBuilder> convertResourceToOperation(FunctionDefinitionNode resource, String httpMethod,
                                                                  String generateRelativePath, Components components) {
        OperationBuilder operationBuilder = new OperationBuilder();
        operationBuilder.setHttpOperation(httpMethod);
        operationBuilder.setPath(generateRelativePath);
        /* Set operation id */
        String resName = (resource.functionName().text() + "_" +
                generateRelativePath).replaceAll("\\{///}", "_");

        if (generateRelativePath.equals("/")) {
            resName = resource.functionName().text();
        }
        operationBuilder.setOperationId(getOperationId(resName));
        // Set operation summary
        // Map API documentation
        Map<String, String> apiDocs = listAPIDocumentations(resource, operationBuilder);
        //Add path parameters if in path and query parameters
        ParameterMapperInterface parameterMapper = new ParameterMapper(resource, operationBuilder, components, apiDocs,
                additionalData, treatNilableAsOptional);
        parameterMapper.setParameters();
        List<OpenAPIMapperDiagnostic> diagnostics = additionalData.diagnostics();
        if (diagnostics.size() > 1 || (diagnostics.size() == 1 && !diagnostics.get(0).getCode().equals(
                DiagnosticMessages.OAS_CONVERTOR_113.getCode()))) {
            boolean isErrorIncluded = diagnostics.stream().anyMatch(diagnostic ->
                    DiagnosticSeverity.ERROR.equals(diagnostic.getDiagnosticSeverity()));
            boolean hasRestPathParam = diagnostics.stream().anyMatch(diagnostic ->
                    DiagnosticMessages.OAS_CONVERTOR_125.getCode().equals(diagnostic.getCode()));
            if (isErrorIncluded || hasRestPathParam) {
                return Optional.empty();
            }
        }

        ResponseMapperInterface responseMapper = new ResponseMapper(resource, operationBuilder, components,
                additionalData);
        responseMapper.setApiResponses();
        return Optional.of(operationBuilder);
    }

    /**
     * Filter the API documentations from resource function node.
     */
    private Map<String, String> listAPIDocumentations(FunctionDefinitionNode resource,
                                                      OperationBuilder operationBuilder) {

        Map<String, String> apiDocs = new HashMap<>();
        if (resource.metadata().isPresent()) {
            Optional<Symbol> resourceSymbol = additionalData.semanticModel().symbol(resource);
            if (resourceSymbol.isPresent()) {
                Symbol symbol = resourceSymbol.get();
                Optional<Documentation> documentation = ((Documentable) symbol).documentation();
                if (documentation.isPresent()) {
                    Documentation documentation1 = documentation.get();
                    Optional<String> description = documentation1.description();
                    if (description.isPresent()) {
                        String resourceFunctionAPI = description.get().trim();
                        apiDocs = documentation1.parameterMap();
                        operationBuilder.setSummary(resourceFunctionAPI);
                    }
                }
            }
        }
        return apiDocs;
    }

    private String generateRelativePath(FunctionDefinitionNode resource) {

        StringBuilder relativePath = new StringBuilder();
        relativePath.append("/");
        if (!resource.relativeResourcePath().isEmpty()) {
            for (Node node: resource.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode pathNode) {
                    relativePath.append("{");
                    relativePath.append(pathNode.paramName().get());
                    relativePath.append("}");
                } else if ((resource.relativeResourcePath().size() == 1) && (node.toString().trim().equals("."))) {
                    return relativePath.toString();
                } else {
                    relativePath.append(node.toString().trim());
                }
            }
        }
        return relativePath.toString();
    }
}
