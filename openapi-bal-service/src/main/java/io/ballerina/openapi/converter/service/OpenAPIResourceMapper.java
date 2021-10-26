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


package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.error.ErrorMessages;
import io.ballerina.openapi.converter.error.IncompatibleResourceError;
import io.ballerina.openapi.converter.error.OpenAPIConverterError;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * This class will do resource mapping from ballerina to openApi.
 *
 * @since 2.0.0
 */
public class OpenAPIResourceMapper {
    private final SemanticModel semanticModel;
    private final Paths pathObject = new Paths();
    private final Components components = new Components();
    private final List<OpenAPIConverterError> errors = new ArrayList<>();

    public List<OpenAPIConverterError> getErrors() {

        return errors;
    }

    /**
     * Initializes a resource parser for openApi.
     */
    OpenAPIResourceMapper(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Components getComponents() {
        return components;
    }
    /**
     * This method will convert ballerina resource to openApi Paths objects.
     *
     * @param resources Resource list to be converted.
     * @return map of string and openApi path objects.
     */
    public Paths getPaths(List<FunctionDefinitionNode> resources) {
        for (FunctionDefinitionNode resource : resources) {
            List<String> methods = this.getHttpMethods(resource, false);
            getResourcePath(resource, methods);
        }
        return pathObject;
    }

    /**
     * Resource mapper when a resource has more than 1 http method.
     * @param resource The ballerina resource.
     * @param httpMethods   Sibling methods related to operation.
     */
    private void getResourcePath(FunctionDefinitionNode resource, List<String> httpMethods) {
        String path = generateRelativePath(resource);
        Operation operation;
        for (String httpMethod : httpMethods) {
            //Iterate through http methods and fill path map.
            if (resource.functionName().toString().trim().equals(httpMethod)) {
                if (httpMethod.equals("'default")) {
                    ErrorMessages errorMessage = ErrorMessages.OAS_CONVERTOR_100;
                    IncompatibleResourceError error = new IncompatibleResourceError(errorMessage.getSeverity(),
                            errorMessage.getDescription(), errorMessage.getCode(), resource.location());
                    errors.add(error);
                } else {
                    operation = convertResourceToOperation(resource, httpMethod, path).getOperation();
                    generatePathItem(httpMethod, pathObject, operation, path);
                }
                break;
            }
        }
    }

    private void generatePathItem(String httpMethod, Paths path, Operation operation, String pathName) {
        PathItem pathItem = new PathItem();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case Constants.GET:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setGet(operation);
                } else {
                    pathItem.setGet(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.PUT:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPut(operation);
                } else {
                    pathItem.setPut(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.POST:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPost(operation);
                } else {
                    pathItem.setPost(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.DELETE:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setDelete(operation);
                } else {
                    pathItem.setDelete(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.OPTIONS:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setOptions(operation);
                } else {
                    pathItem.setOptions(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.PATCH:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPatch(operation);
                } else {
                    pathItem.setPatch(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.HEAD:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setHead(operation);
                } else {
                    pathItem.setHead(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            default:
                break;
        }
    }

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
     *
     * @return Operation Adaptor object of given resource
     */
    private OperationAdaptor convertResourceToOperation(FunctionDefinitionNode resource, String httpMethod,
                                                        String generateRelativePath) {
        OperationAdaptor op = new OperationAdaptor();
        op.setHttpOperation(httpMethod);
        op.setPath(generateRelativePath);
        /* Set operation id */
        String resName = (resource.functionName().text() + "_" +
                generateRelativePath).replaceAll("\\{///\\}", "_");

        if (generateRelativePath.equals("/")) {
            resName = resource.functionName().text();
        }
        op.getOperation().setOperationId(getOperationId(resName));
        op.getOperation().setParameters(null);
        // Set operation summary
        // Map API documentation
        Map<String, String> apiDocs = listAPIDocumentations(resource, op);
        //Add path parameters if in path and query parameters
        OpenAPIParameterMapper openAPIParameterMapper = new OpenAPIParameterMapper(resource, op, apiDocs);
        openAPIParameterMapper.getResourceInputs(components, semanticModel);
        OpenAPIResponseMapper openAPIResponseMapper = new OpenAPIResponseMapper(semanticModel, components,
                resource.location());
        openAPIResponseMapper.getResourceOutput(resource, op);
        errors.addAll(openAPIResponseMapper.getErrors());
        return op;
    }

    /**
     * Filter the API documentations from resource function node.
     */
    private Map<String, String> listAPIDocumentations(FunctionDefinitionNode resource, OperationAdaptor op) {

        Map<String, String> apiDocs = new HashMap<>();
        if (resource.metadata().isPresent()) {
            Optional<Symbol> resourceSymbol = semanticModel.symbol(resource);
            if (resourceSymbol.isPresent()) {
                Symbol symbol = resourceSymbol.get();
                Optional<Documentation> documentation = ((Documentable) symbol).documentation();
                if (documentation.isPresent()) {
                    Documentation documentation1 = documentation.get();
                    Optional<String> description = documentation1.description();
                    if (description.isPresent()) {
                        String resourceFunctionAPI = description.get().trim();
                        apiDocs = documentation1.parameterMap();
                        op.getOperation().setSummary(resourceFunctionAPI);
                    }
                }
            }
        }
        return apiDocs;
    }

    /**
     * get UUID generated with the given post fix.
     *
     * @param postFix string post fix to attach to ID
     * @return {@link String} generated UUID
     */
    private String getOperationId(String postFix) {
        return "operation_" + postFix;
    }

    /**
     * Gets the http methods of a resource.
     *
     * @param resource    The ballerina resource.
     * @param useDefaults True to add default http methods, else false.
     * @return A list of http methods.
     */
    private List<String> getHttpMethods(FunctionDefinitionNode resource, boolean useDefaults) {
        Set<String> httpMethods = new LinkedHashSet<>();
        ServiceDeclarationNode parentNode = (ServiceDeclarationNode) resource.parent();
        NodeList<Node> siblings = parentNode.members();
        httpMethods.add(resource.functionName().text());
        String relativePath = generateRelativePath(resource);
        for (Node function: siblings) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                FunctionDefinitionNode sibling = (FunctionDefinitionNode) function;
                //need to build relative path
                String siblingRelativePath = generateRelativePath(sibling);
                if (relativePath.equals(siblingRelativePath)) {
                    httpMethods.add(sibling.functionName().text());
                }
            }
        }
        return new ArrayList<>(httpMethods);
    }

    private String generateRelativePath(FunctionDefinitionNode resource) {

        StringBuilder relativePath = new StringBuilder();
        relativePath.append("/");
        if (!resource.relativeResourcePath().isEmpty()) {
            for (Node node: resource.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode) {
                    ResourcePathParameterNode pathNode = (ResourcePathParameterNode) node;
                    relativePath.append("{");
                    relativePath.append(pathNode.paramName());
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
