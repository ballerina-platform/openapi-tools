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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.openapi.validator.model.MetaData;
import io.ballerina.openapi.validator.model.OpenAPIPathSummary;
import io.ballerina.openapi.validator.model.ResourceMethod;
import io.ballerina.openapi.validator.model.ResourcePathSummary;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeOpenAPI;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeResources;

/**
 * This model used to filter and validate all the operations according to the given filter and filter the service
 * resource in the resource file.
 *
 * @since 1.1.0
 */
public class ServiceValidator implements Validator {
    private Filter filter;
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;

    public void initialize(SyntaxNodeAnalysisContext context, OpenAPI openAPI, Filter filter) {
        this.context = context;
        this.openAPI = openAPI;
        this.filter = filter;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    /**
     * Validate the given service. All the diagnostics may update with the @SyntaxNodeAnalysisContext context
     * dynamically.
     */
    @Override
    public void validate() {
        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) context.node();

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
        Map<String, ResourcePathSummary> resourcePathMap = summarizeResources(resourceFunctions, context);

        // 4. Unimplemented resource in service file (extra resource in openapi spec)
        List<OpenAPIPathSummary> updatedOASPaths = validateMissingBalResources(openAPIPathSummaries,
                resourcePathMap);
        // 5. Undocumented resource in service file (extra resource in ballerina service)
        Map<String, ResourcePathSummary> updatedResourcePath = validateUndefinedBalResources(openAPIPathSummaries,
                resourcePathMap);

        // 6. Resource validation
        validateBalServiceWithOAS(updatedResourcePath, updatedOASPaths);

    }

    /**
     * Validate all the resource with operations to check whether there is any missing implementation for operations.
     * OAS-> ballerina validate add
     */
    private List<OpenAPIPathSummary> validateMissingBalResources(List<OpenAPIPathSummary> operations, Map<String,
            ResourcePathSummary> resources) {
        boolean filterEnable = filter.getOperation() != null || filter.getTag() != null ||
                filter.getExcludeTag() != null || filter.getExcludeOperation() != null;
        Iterator<OpenAPIPathSummary> openAPIPathIterator = operations.iterator();
        while (openAPIPathIterator.hasNext()) {
            OpenAPIPathSummary operationPath = openAPIPathIterator.next();
            // Extra path openapi
            if (!resources.containsKey(operationPath.getPath())) {
                if (!filterEnable) {
                    reportDiagnostic(context, CompilationError.MISSING_RESOURCE_PATH, context.node().location(),
                            filter.getKind(),
                            getNormalizedPath(operationPath.getPath()));
                }
                openAPIPathIterator.remove();
            } else {
                // Extra operation in openAPI
                ResourcePathSummary resourcePath = resources.get(operationPath.getPath());
                Map<String, ResourceMethod> resourceMethods = resourcePath.getMethods();
                Map<String, Operation> methods = operationPath.getOperations();
                for (Map.Entry<String, Operation> operation : methods.entrySet()) {
                    if (!resourceMethods.containsKey(operation.getKey().trim())) {
                        if (!filterEnable) {
                            reportDiagnostic(context, CompilationError.MISSING_RESOURCE_FUNCTION,
                                    context.node().location(), filter.getKind(), operation.getKey().trim(),
                                    getNormalizedPath(operationPath.getPath()));
                        }
                        methods.remove(operation.getKey());
                    }
                }
            }
        }
        return operations;
    }

    /**
     * Checking whether there is undocumented resource function with align to openapi spec.
     * Ballerina -> OAS
     */
    private Map<String, ResourcePathSummary> validateUndefinedBalResources(List<OpenAPIPathSummary> operations,
                                                                           Map<String, ResourcePathSummary>
                                                                                   resourcePathMap) {
        boolean filterEnable = filter.getOperation() != null || filter.getTag() != null ||
                filter.getExcludeTag() != null || filter.getExcludeOperation() != null;

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
                        if (!operationPath.getOperations().containsKey(method.getKey().trim())) {
                            if (!filterEnable) {
                                reportDiagnostic(context, CompilationError.UNDEFINED_RESOURCE_FUNCTIONS,
                                        method.getValue().getLocation(), filter.getKind(), method.getKey(),
                                        getNormalizedPath(resourcePath.getKey()));
                            }
                            methodsIter.remove();
                        }
                    }
                    break;
                }
            }
            if (!isPathDocumented) {
                if (!filterEnable) {
                    reportDiagnostic(context, CompilationError.UNDEFINED_RESOURCE_PATH, context.node().location(),
                            filter.getKind(),
                            getNormalizedPath(resourcePath.getKey()));
                }
                resourcePathIter.remove();
            }
        }
        return resourcePathMap;
    }

    /**
     * This validation happens ballerina service against to openapi specification.
     *
     */
    private void validateBalServiceWithOAS(Map<String, ResourcePathSummary> resourcePaths,
                                           List<OpenAPIPathSummary> oasPaths) {

        Set<Map.Entry<String, ResourcePathSummary>> paths = resourcePaths.entrySet();
        for (Map.Entry<String, ResourcePathSummary> path : paths) {
            Map<String, ResourceMethod> methods = path.getValue().getMethods();
            OpenAPIPathSummary oasPath = null;
            for (OpenAPIPathSummary openAPIPath : oasPaths) {
                if (path.getKey().equals(openAPIPath.getPath())) {
                    oasPath = openAPIPath;
                    break;
                }
            }

            for (Map.Entry<String, ResourceMethod> method : methods.entrySet()) {
                assert oasPath != null;
                MetaData metaData = new MetaData(context, openAPI, path.getKey(), method.getKey(), filter.getKind(),
                        method.getValue().getLocation());
                Map<String, Operation> operations = oasPath.getOperations();
                Operation oasOperation = operations.get(method.getKey());
                // Parameters validation
                List<Parameter> oasParameters = oasOperation.getParameters();
                ParameterValidator parameterValidator = new ParameterValidator(metaData,
                        method.getValue().getParameters(), oasParameters);
                parameterValidator.validate();

                // Headers validation
                Map<String, Node> balHeaders = method.getValue().getHeaders();
                HeaderValidator headerValidator = new HeaderValidator(metaData, balHeaders, oasParameters);
                headerValidator.validate();

                // Request body validation
                RequestBodyValidator requestBodyValidator = new RequestBodyValidator(metaData,
                        oasOperation.getRequestBody(), method.getValue().getBody());
                requestBodyValidator.validate();

                // Return Type validation
                ReturnTypeDescriptorNode returnNode = method.getValue().getReturnNode();
                ApiResponses responses = oasOperation.getResponses();

                ReturnTypeValidator returnTypeValidator = new ReturnTypeValidator(metaData, returnNode, responses);
                returnTypeValidator.validate();
            }
        }
    }
}
