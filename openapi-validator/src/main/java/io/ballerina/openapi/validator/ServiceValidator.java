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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.openapi.validator.model.OpenAPIPathSummary;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.validator.ValidatorUtils.summarizeOpenAPI;

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

    public void initialize(SyntaxNodeAnalysisContext context,OpenAPI openAPI, Filter filter) {
        this.context = context;
        this.openAPI = openAPI;
        this.filter = filter;
    }

    public void validate() {
        SemanticModel semanticModel = context.semanticModel();
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

    }
}

