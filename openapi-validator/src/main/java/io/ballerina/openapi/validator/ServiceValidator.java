/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.openapi.validator.model.OpenAPIPathSummary;
import io.ballerina.openapi.validator.model.ResourceMethod;
import io.ballerina.openapi.validator.model.ResourcePathSummary;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.openapi.validator.ValidatorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeOpenAPI;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeResources;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This model used to filter and validate all the operations according to the given filter and filter the service
 * resource in the resource file.
 *
 * @since 2201.0.1
 */
public class ServiceValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private Filter filter;

    public void initialize(SyntaxNodeAnalysisContext context, OpenAPI openAPI, Filter filter) {
        this.context = context;
        this.openAPI = openAPI;
        this.filter = filter;
    }

    public void validate() {
        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) this.context.node();

        // 1. Summaries the OAS operations and return the filtered operations
        List<OpenAPIPathSummary> openAPIPathSummaries = summarizeOpenAPI(openAPI, context, filter);
        // 2. Summaries the ballerina resource
        NodeList<Node> members = serviceNode.members();
        List<FunctionDefinitionNode> resourceFunctions = new ArrayList<>();
        for (Node next : members) {
            if (next instanceof FunctionDefinitionNode) {
                resourceFunctions.add((FunctionDefinitionNode) next);
            }
        }
        // 3. Summaries the resource functions
        Map<String, ResourcePathSummary> resourcePathMap = summarizeResources(resourceFunctions);

        // 4. Unimplemented resource in service file
        List<OpenAPIPathSummary> updatedOASPaths = unimplementedResourceFunction(openAPIPathSummaries,
                resourcePathMap);
        // 5. Undocumented resource in service file
        Map<String, ResourcePathSummary> updatedResourcePath = undocumentedResource(openAPIPathSummaries, resourcePathMap);
        SemanticModel semanticModel = context.semanticModel();

        // 6. Ballerina -> OAS validation

                // parameter , query, path
                // header
                // request body
                // return type

        // 7. OpenAPI -> Ballerina

    }

    // OAS-> ballerina
    private List<OpenAPIPathSummary> unimplementedResourceFunction(List<OpenAPIPathSummary> operations, Map<String,
            ResourcePathSummary> resources) {
        Iterator<OpenAPIPathSummary> openAPIPathIterator = operations.iterator();
        while (openAPIPathIterator.hasNext()) {
            OpenAPIPathSummary operationPath = openAPIPathIterator.next();
            // Unimplemented path
            if (!resources.containsKey(operationPath.getPath())) {
                updateContext(context, CompilationError.UNIMPLEMENTED_RESOURCE_PATH, context.node().location(),
                        operationPath.getPath());
                openAPIPathIterator.remove();
            } else {
                // Unimplemented function
                ResourcePathSummary resourcePath = resources.get(operationPath.getPath());
                Map<String, ResourceMethod> resourceMethods = resourcePath.getMethods();
                Map<String, Operation> methods = operationPath.getOperations();
                for (Map.Entry<String, Operation> operation : methods.entrySet()) {
                    if (!resourceMethods.containsKey(operation.getKey().trim())) {
                        updateContext(context, CompilationError.UNIMPLEMENTED_RESOURCE_FUNCTION,
                                context.node().location(), operation.getKey().trim(), operationPath.getPath());
                        openAPIPathIterator.remove();
                    }
                }
            }
        }
        return operations;
    }

    // ballerina -> OAS

    private Map<String, ResourcePathSummary> undocumentedResource(List<OpenAPIPathSummary> operations, Map<String,
            ResourcePathSummary> resourcePathMap) {
        Iterator<Map.Entry<String, ResourcePathSummary>> resourcePathIter = resourcePathMap.entrySet().iterator();
        while (resourcePathIter.hasNext()) {
            Map.Entry<String, ResourcePathSummary> resourcePath = resourcePathIter.next();
            boolean isPathDocumented = false;
            for (OpenAPIPathSummary operationPath: operations) {
                if (operationPath.getPath().equals(resourcePath.getKey())) {
                    isPathDocumented = true;
                    Set<Map.Entry<String, ResourceMethod>> methods = resourcePath.getValue().getMethods().entrySet();
                    Iterator<Map.Entry<String, ResourceMethod>> methodsIter = methods.iterator();
                    while (methodsIter.hasNext()) {
                        Map.Entry<String, ResourceMethod> method = methodsIter.next();
                        if (!operationPath.getAvailableOperations().contains(method.getKey().trim())) {
                            updateContext(context, CompilationError.UNDOCUMENTED_RESOURCE_FUNCTIONS,
                                    method.getValue().getLocation(), method.getKey(), resourcePath.getKey());
                            methodsIter.remove();
                        }
                    }
                    break;
                }
            }
            if (!isPathDocumented) {
                updateContext(context, CompilationError.UNDOCUMENTED_RESOURCE_PATH, context.node().location(),
                        resourcePath.getKey());
                resourcePathIter.remove();
            }
        }
        return resourcePathMap;
    }

    // ballerina -> OAS
    private void validateBallerinaAgainstToOAS (List<OpenAPIPathSummary> oasPaths, Map<String,
            ResourcePathSummary> resourcePaths) {

        Set<Map.Entry<String, ResourcePathSummary>> paths = resourcePaths.entrySet();
        Iterator<Map.Entry<String, ResourcePathSummary>> pathIter = paths.iterator();
        while (pathIter.hasNext()) {
            Map.Entry<String, ResourcePathSummary> path = pathIter.next();
            Map<String, ResourceMethod> methods = path.getValue().getMethods();
            OpenAPIPathSummary oasPath = null;
            for (OpenAPIPathSummary openAPIPath: oasPaths) {
                if (path.getKey().equals(openAPIPath.getPath())) {
                    oasPath = openAPIPath;
                    break;
                }
            }

            for (Map.Entry<String, ResourceMethod> method : methods.entrySet()) {
                assert oasPath != null;
                Map<String, Operation> operations = oasPath.getOperations();
                Operation oasOperation = operations.get(method.getKey());
                // parameter validation ballerina ->
                Map<String, ParameterNode> balParameters = method.getValue().getParameters();
                List<Parameter> oasParameters = oasOperation.getParameters();
                /// 1. parameter type mismatch
                //  2. undocumented parameter
                for (Map.Entry<String, ParameterNode> parameter : balParameters.entrySet()) {
                    boolean isExist = false;
                    ParameterNode paramNode = parameter.getValue();
                    String ballerinaType;
                    if (paramNode instanceof RequiredParameterNode) {
                        RequiredParameterNode requireParam = (RequiredParameterNode) paramNode;
                        ballerinaType = requireParam.typeName().toString().trim();
                    } else {
                        DefaultableParameterNode defaultParam = (DefaultableParameterNode) paramNode;
                        ballerinaType = defaultParam.typeName().toString().trim();
                    }
                    for (Parameter oasParameter : oasParameters) {
                        if (!parameter.getKey().equals(oasParameter.getName())) {
                            continue;
                        }
                        isExist = true;
                        if (convertOpenAPITypeToBallerina(oasParameter.getSchema().getType()).isEmpty()) {
                            // type mismatch
                            return;
                        }
                        String type = convertOpenAPITypeToBallerina(oasParameter.getSchema().getType()).get();
                        if (!ballerinaType.equals(type)) {

                        }
                    }
                }
            }
        }
    }

    private void validateBalParameterAgainstOAS(Map<String, ParameterNode> parameters, List<Parameter> oasParameters) {

        for (Map.Entry<String, ParameterNode> parameter : parameters.entrySet()) {
            boolean isBalParameterExist = false;
            for (Parameter oasParam: oasParameters) {
                //escape special character
                if (parameter.getKey().equals(oasParam.getName())) {
                    isBalParameterExist = true;
                    ParameterNode value = parameter.getValue();

                }
            }
        }

    }
}

