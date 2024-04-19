/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.service.resource.ResourceGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ServiceGenerator {

    final OASServiceMetadata oasServiceMetadata;
    boolean isNullableRequired;
    List<Node> functionsList;

    final List<Diagnostic> diagnostics = new ArrayList<>();

    public ServiceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public ServiceGenerator(OASServiceMetadata oasServiceMetadata, List<Node> functionsList) {
        this.oasServiceMetadata = oasServiceMetadata;
        this.functionsList = functionsList;
    }

    public abstract SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException;

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public List<Node> getFunctionsList() {
        return functionsList;
    }

    List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) {
        List<Node> functions = new ArrayList<>();
        if (!openApi.getPaths().isEmpty()) {
            Paths paths = openApi.getPaths();
            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
            for (Map.Entry<String, PathItem> path : pathsItems) {
                if (!path.getValue().readOperationsMap().isEmpty()) {
                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                    functions.addAll(applyFiltersForOperations(filter, path.getKey(), operationMap));
                }
            }
        }
        return functions;
    }

    private List<Node> applyFiltersForOperations(Filter filter, String path,
                                                 Map<PathItem.HttpMethod, Operation> operationMap) {
        List<Node> functions = new ArrayList<>();
        for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
            //Add filter availability
            //1.Tag filter
            //2.Operation filter
            //3. Both tag and operation filter
            List<String> filterTags = filter.getTags();
            List<String> operationTags = operation.getValue().getTags();
            List<String> filterOperations = filter.getOperations();
            ResourceGenerator resourceGenerator = ResourceGenerator.createResourceGenerator(oasServiceMetadata);
            if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                if (operationTags != null || ((!filterOperations.isEmpty())
                        && (operation.getValue().getOperationId() != null))) {
                    if ((operationTags != null && GeneratorUtils.hasTags(operationTags, filterTags)) ||
                            ((operation.getValue().getOperationId() != null) &&
                                    filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                        FunctionDefinitionNode resourceFunction;
                        try {
                            resourceFunction = resourceGenerator.generateResourceFunction(operation, path);
                        } catch (BallerinaOpenApiException e) {
                            // this will catch the error level diagnostics that affects the function generation.
                            diagnostics.add(e.getDiagnostic());
                            continue;
                        }
                        // this will catch the warning level diagnostics that does not affect the function generation.
                        diagnostics.addAll(resourceGenerator.getDiagnostics());
                        functions.add(resourceFunction);
                    }
                }
            } else {
                FunctionDefinitionNode resourceFunction;
                try {
                    resourceFunction = resourceGenerator.generateResourceFunction(operation, path);
                } catch (BallerinaOpenApiException e) {
                    // this will catch the error level diagnostics that affects the function generation.
                    diagnostics.add(e.getDiagnostic());
                    continue;
                }
                diagnostics.addAll(resourceGenerator.getDiagnostics());
                functions.add(resourceFunction);
            }
            if (resourceGenerator.isNullableRequired()) {
                isNullableRequired = true;
            }
        }
        return functions;
    }
}
