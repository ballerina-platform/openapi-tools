/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.validator;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.openapi.validator.error.OpenapiServiceValidationError;
import io.ballerina.openapi.validator.error.ResourceValidationError;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.validator.Constants.FULL_STOP;
import static io.ballerina.openapi.validator.Constants.SLASH;

/**
 * This for finding out the all the filtered operations are documented as services in the ballerina file and all the
 * ballerina services are documented in the contract yaml file.
 */
public class ResourceWithOperation {
    /**
     * Filter all the operations according to the given filters.
     * @param openApi       OpenApi Object
     * @param filters       Filter Object
     * @return              List of OpenApiPathSummary
     */
    public static List<OpenAPIPathSummary> filterOpenapi(OpenAPI openApi,
                                                         Filters filters) {

        boolean tagFilteringEnabled = filters.getTag().size() > 0;
        boolean operationFilteringEnabled = filters.getOperation().size() > 0;
        boolean excludeTagsFilteringEnabled = filters.getExcludeTag().size() > 0;
        boolean excludeOperationFilteringEnable = filters.getExcludeOperation().size() > 0;
        List<OpenAPIPathSummary> openAPIPathSummaries = ResourceWithOperation.summarizeOpenAPI(openApi);
        // Check based on the method and path filters
        Iterator<OpenAPIPathSummary> openAPIIter = openAPIPathSummaries.iterator();
        while (openAPIIter.hasNext()) {
            OpenAPIPathSummary openAPIPathSummary = openAPIIter.next();
            // If operation filtering available proceed.
            // Else if proceed to check exclude operation filter is enable
            // Else check tag filtering or excludeTag filtering enable.
            if (operationFilteringEnabled) {
                // If tag filtering available validate only the filtered operations grouped by given tags.
                // Else if exclude tag filtering available validate only the operations that are not include
                // exclude Tags.
                // Else proceed only to validate filtered operations.
                if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        // Check operationId is null scenario.
                        // Check tag is available if it is null then remove other wise else-if not include
                        // tag then remove operations.
                        if (!(filters.getOperation().contains(operationMap.getValue().getOperationId())) ||
                                (operationMap.getValue().getOperationId() == null)) {
                                operations.remove();
                        } else {
                            if ((operationMap.getValue().getTags() == null) ||
                                    (Collections.disjoint(filters.getTag(), operationMap.getValue().getTags()))) {
                                operations.remove();
                            }
                        }
                    }
                } else if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getOperation().contains(operationMap.getValue().getOperationId())) {
                            //  Check tag is available
                            if ((operationMap.getValue().getTags() != null) && (!Collections.
                                    disjoint(filters.getExcludeTag(), operationMap.getValue().getTags()))) {
                                    operationIter.remove();
                            }
                        } else {
                            operationIter.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (!filters.getOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }
            } else if (excludeOperationFilteringEnable) {
                // If exclude tags filtering available validate only the filtered exclude operations grouped by
                // given exclude tags.
                // Else If tags filtering available validate only the operations that filtered by exclude
                // operations.
                // Else proceed only to validate filtered exclude operations.
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if ((!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) &&
                                ((operationMap.getValue().getTags() != null) && (!Collections
                                .disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())))) {
                                operationIter.remove();
                        }
                    }
                } else if (tagFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (!filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
                            //  Check tag is available if it is null and not included in list
                            //  then remove operations.
                            if ((operationMap.getValue().getTags() == null) || (Collections.disjoint(filters.getTag(),
                                    operationMap.getValue().getTags()))) {
                                operations.remove();
                            }
                        } else {
                            operations.remove();
                        }
                    }
                } else {
                    Iterator<Map.Entry<String, Operation>> operationIter =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operationIter.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operationIter.next();
                        if (filters.getExcludeOperation().contains(operationMap.getValue().getOperationId())) {
                            operationIter.remove();
                        }
                    }
                }
                // If exclude tag filtering available proceed to validate all the operations grouped by tags which
                // are not included in list.
                // Else if validate the operations group by tag filtering
                // Else proceed without any filtering.
            } else {
                if (excludeTagsFilteringEnabled) {
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if (operationMap.getValue().getTags() == null) {
                            break;
                        } else if (!Collections.disjoint(filters.getExcludeTag(), operationMap.getValue().getTags())) {
                            operations.remove();
                        }
                    }
                } else if (tagFilteringEnabled) {
                    // If tag filtering available proceed to validate all the operations grouped by given tags.
                    // Else proceed only to validate filtered operations.
                    Iterator<Map.Entry<String, Operation>> operations =
                            openAPIPathSummary.getOperations().entrySet().iterator();
                    while (operations.hasNext()) {
                        Map.Entry<String, Operation> operationMap = operations.next();
                        if ((operationMap.getValue().getTags() == null) || (Collections.disjoint(filters.getTag(),
                                operationMap.getValue().getTags()))) {
                            operations.remove();
                        }
                    }
                }
            }
            if (openAPIPathSummary.getOperations().isEmpty()) {
                openAPIIter.remove();
            }
        }
        return openAPIPathSummaries;
    }

    /**
     * This function use for checking the all the openapi operations have resource functions.
     * @param openAPIPathSummaries  Summary of the openAPIPath
     * @param resourcePathSummaries Summary of the resource paths
     * @return error list with missing operations details in service file
     */
    public static List<OpenapiServiceValidationError> checkOperationsHasFunctions(
            List<OpenAPIPathSummary> openAPIPathSummaries, Map<String, ResourcePathSummary> resourcePathSummaries) {

        List<OpenapiServiceValidationError> operationsValidationErrors = new ArrayList<>();
        for (OpenAPIPathSummary openAPIPathSummary: openAPIPathSummaries) {
            boolean isPathExit = false;

            for (Map.Entry<String, ResourcePathSummary> resourcePathSummary: resourcePathSummaries.entrySet()) {
                if (openAPIPathSummary.getPath().equals(resourcePathSummary.getKey())) {
                    isPathExit = true;
                    for (String method : openAPIPathSummary.getAvailableOperations()) {
                        boolean isMethodExit = false;
                        Map<String, ResourceMethod> methods = resourcePathSummary.getValue().getMethods();
                        for (Map.Entry<String, ResourceMethod> resourceMethod: methods.entrySet()) {
                            if (method.equals(resourceMethod.getKey())) {
                                isMethodExit = true;
                                break;
                            }
                        }
                        if (!isMethodExit) {
                            OpenapiServiceValidationError openapiServiceValidationError =
                                    new OpenapiServiceValidationError(method,
                                            openAPIPathSummary.getPath(),
                                            openAPIPathSummary.getOperations().get(method).getTags(),
                                            openAPIPathSummary);
                            operationsValidationErrors.add(openapiServiceValidationError);
                        }

                    }
                    break;
                }
            }
            if (!isPathExit) {
                OpenapiServiceValidationError openapiServiceValidationError = new OpenapiServiceValidationError(
                        null, openAPIPathSummary.getPath(), null, openAPIPathSummary);
                operationsValidationErrors.add(openapiServiceValidationError);
            }
        }
        return operationsValidationErrors;
    }

    public static List<ResourceValidationError> checkResourceHasOperation(
            List<OpenAPIPathSummary> openAPIPathSummaries, Map<String, ResourcePathSummary> resourcePathSummaries) {
        List<ResourceValidationError> resourceValidationErrors = new ArrayList<>();
        for (Map.Entry<String, ResourcePathSummary> resourcePathSummary: resourcePathSummaries.entrySet()) {
            boolean isResourceExit = false;
            for (OpenAPIPathSummary openAPIPathSummary: openAPIPathSummaries) {
                if (resourcePathSummary.getKey().equals(openAPIPathSummary.getPath())) {
                    isResourceExit = true;
                    Map<String, ResourceMethod> methods = resourcePathSummary.getValue().getMethods();
                    for (Map.Entry<String, ResourceMethod> method : methods.entrySet()) {
                        boolean isMethodExit = false;
                        for (String operation: openAPIPathSummary.getAvailableOperations()) {
                            if (method.getKey().equals(operation)) {
                                isMethodExit = true;
                                break;
                            }
                        }
                        if (!isMethodExit) {
                            ResourceValidationError resourceValidationError =
                                    new ResourceValidationError(method.getValue().getMethodPosition(),
                                            method.getKey(), resourcePathSummary.getKey());
                            resourceValidationErrors.add(resourceValidationError);
                        }
                    }
                    break;
                }
            }
            if (!isResourceExit) {
                ResourceValidationError resourceValidationError =
                        new ResourceValidationError(resourcePathSummary.getValue().getPathPosition(), null,
                                resourcePathSummary.getKey());
                resourceValidationErrors.add(resourceValidationError);
            }
        }
        return resourceValidationErrors;
    }

    /**
     * Extract the details to be validated from the resource.
     * @param functions         documented functions
     * @return List of ResourcePathSummary
     */
    public static Map<String, ResourcePathSummary>  summarizeResources(List<FunctionDefinitionNode> functions) {
        // Iterate resources available in a service and extract details to be validated.
        Map<String, ResourcePathSummary> resourceSummaryList = new HashMap<>();
        for (FunctionDefinitionNode functionDefinitionNode: functions) {
            NodeList<Node> nodes = functionDefinitionNode.relativeResourcePath();
            String functionMethod = functionDefinitionNode.functionName().text().trim();
            Map<String, Node> parameterNodeMap = new HashMap<>();
            String path = generatePath(nodes, parameterNodeMap);
            if (resourceSummaryList.containsKey(path)) {
                setParametersToMethod(functionDefinitionNode, functionMethod, parameterNodeMap,
                        resourceSummaryList.get(path));
            } else {
                ResourcePathSummary resourcePathSummary = new ResourcePathSummary();
                //Set location as first function location
                resourcePathSummary.setPath(path);
                resourcePathSummary.setPathPosition(functionDefinitionNode.location());
                setParametersToMethod(functionDefinitionNode, functionMethod, parameterNodeMap, resourcePathSummary);
                resourceSummaryList.put(path, resourcePathSummary);
            }
        }
        return resourceSummaryList;
    }

    /**
     * This function generates resource function path that align to OAS format.
     */
    private static String generatePath(NodeList<Node> nodes, Map<String, Node> parameterNodeMap) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(SLASH);
        for (Node next : nodes) {
            String node = next.toString().trim();
            if (next instanceof ResourcePathParameterNode) {
                ResourcePathParameterNode pathParameterNode = (ResourcePathParameterNode) next;
                String paramName = pathParameterNode.paramName().text().trim();
                node = "{" + paramName + "}";
                parameterNodeMap.put(paramName, next);
            }
            pathBuilder.append(node);
        }
        String path = pathBuilder.toString();
        if (!path.endsWith(FULL_STOP)) {
            return path;
        }
        return path.substring(0, path.length() - 1);
    }

    private static void setParametersToMethod(FunctionDefinitionNode functionDefinitionNode, String functionMethod,
                                              Map<String, Node> parameterNodeMap,
                                              ResourcePathSummary resourcePathSummary2) {

        FunctionSignatureNode functionSignatureNode = functionDefinitionNode.functionSignature();
        SeparatedNodeList<ParameterNode> parameters = functionSignatureNode.parameters();
        Iterator<ParameterNode> parameterIterator = parameters.iterator();
        ResourceMethod resourceMethod = new ResourceMethod();
        while (parameterIterator.hasNext()) {
            ParameterNode param = parameterIterator.next();
            if (!param.toString().contains("http:Caller") && !param.toString().contains("http:Request")) {
                String[] qParam = param.toString().trim().split(" ");
                String paramName;
                if (qParam.length > 1) {
                    paramName = qParam[qParam.length - 1];
                } else {
                    paramName = param.toString().trim();
                }
                if (param.toString().contains("http:Payload")) {
                    resourceMethod.setBody(true);
                }
                parameterNodeMap.put(paramName, param);
            }
        }
        resourceMethod.setMethod(functionMethod);
        resourceMethod.setParameters(parameterNodeMap);
        resourceMethod.setResourcePosition(functionDefinitionNode.location());
        resourcePathSummary2.addMethod(functionMethod, resourceMethod);
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     * @param contract                openAPI contract
     * @return List of summarized OpenAPIPathSummary
     */
    private static List<OpenAPIPathSummary> summarizeOpenAPI(OpenAPI contract) {
        List<OpenAPIPathSummary> openAPISummaries = new ArrayList<>();
        io.swagger.v3.oas.models.Paths paths = contract.getPaths();
        for (Map.Entry pathItem : paths.entrySet()) {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (pathItem.getKey() instanceof String
                    && pathItem.getValue() instanceof PathItem) {
                String key = (String) pathItem.getKey();
                openAPISummary.setPath(key);

                PathItem operations = (PathItem) pathItem.getValue();
                if (operations.getGet() != null) {
                    addOpenapiSummary(openAPISummary, Constants.GET, operations.getGet());
                }
                if (operations.getPost() != null) {
                    addOpenapiSummary(openAPISummary, Constants.POST, operations.getPost());
                }
                if (operations.getPut() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PUT, operations.getPut());
                }
                if (operations.getDelete() != null) {
                    addOpenapiSummary(openAPISummary, Constants.DELETE, operations.getDelete());
                }
                if (operations.getHead() != null) {
                    addOpenapiSummary(openAPISummary, Constants.HEAD, operations.getHead());
                }
                if (operations.getPatch() != null) {
                    addOpenapiSummary(openAPISummary, Constants.PATCH, operations.getPatch());
                }

                if (operations.getOptions() != null) {
                    addOpenapiSummary(openAPISummary, Constants.OPTIONS, operations.getOptions());
                }
                if (operations.getTrace() != null) {
                    addOpenapiSummary(openAPISummary, Constants.TRACE, operations.getTrace());
                }
            }
            openAPISummaries.add(openAPISummary);
        }
    return openAPISummaries;
    }

    private static void addOpenapiSummary(OpenAPIPathSummary openAPISummary, String get, Operation get2) {
        openAPISummary.addAvailableOperation(get);
        openAPISummary.addOperation(get, get2);
    }

    /**
     * Remove operations based on the missing errors.
     * @param openAPISummaries      List of OpenApiPathSummary
     * @param missingPathInResource Error List of missing operation
     * @return Filtered List with OpenAPiPathSummary
     */
    public static List<OpenAPIPathSummary> removeUndocumentedPath(List<OpenAPIPathSummary> openAPISummaries,
                                                                  List<OpenapiServiceValidationError>
                                                                          missingPathInResource) {
        if (!openAPISummaries.isEmpty()) {
            Iterator<OpenAPIPathSummary> openAPIPathIterator = openAPISummaries.iterator();
            while (openAPIPathIterator.hasNext()) {
                OpenAPIPathSummary openAPIPathSummary = openAPIPathIterator.next();
                if (!missingPathInResource.isEmpty()) {
                    for (OpenapiServiceValidationError error: missingPathInResource) {
                        if (error.getServicePath().equals(openAPIPathSummary.getPath())) {
                            if ((error.getServiceOperation() != null) &&
                                    (!openAPIPathSummary.getOperations().isEmpty())) {
                                Map<String, Operation> operationsMap = openAPIPathSummary.getOperations();
                                operationsMap.entrySet().removeIf(operationMap -> operationMap.getKey()
                                        .equals(error.getServiceOperation()));
                            } else if (error.getServiceOperation() == null) {
                                openAPIPathIterator.remove();
                            }
                        }
                    }
                }
            }
        }
        return openAPISummaries;
    }
}
