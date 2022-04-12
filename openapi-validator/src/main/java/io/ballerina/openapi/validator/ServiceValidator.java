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

import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
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
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.validator.Constants.HTTP_HEADER;
import static io.ballerina.openapi.validator.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.validator.TypeValidatorUtils.convertBallerinaType;
import static io.ballerina.openapi.validator.TypeValidatorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.validator.ValidatorUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeOpenAPI;
import static io.ballerina.openapi.validator.ValidatorUtils.summarizeResources;
import static io.ballerina.openapi.validator.ValidatorUtils.unescapeIdentifier;
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

        // 6. Ballerina -> OAS validation
        validateBallerinaAgainstToOAS(updatedResourcePath, updatedOASPaths);
                // parameter , query, path - done
                // header - done
                // request body - done partial
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
                        getNormalizedPath(operationPath.getPath()));
                openAPIPathIterator.remove();
            } else {
                // Unimplemented function
                ResourcePathSummary resourcePath = resources.get(operationPath.getPath());
                Map<String, ResourceMethod> resourceMethods = resourcePath.getMethods();
                Map<String, Operation> methods = operationPath.getOperations();
                for (Map.Entry<String, Operation> operation : methods.entrySet()) {
                    if (!resourceMethods.containsKey(operation.getKey().trim())) {
                        updateContext(context, CompilationError.UNIMPLEMENTED_RESOURCE_FUNCTION,
                                context.node().location(), operation.getKey().trim(),
                                getNormalizedPath(operationPath.getPath()));
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
                                    method.getValue().getLocation(), method.getKey(),
                                    getNormalizedPath(resourcePath.getKey()));
                            methodsIter.remove();
                        }
                    }
                    break;
                }
            }
            if (!isPathDocumented) {
                updateContext(context, CompilationError.UNDOCUMENTED_RESOURCE_PATH, context.node().location(),
                        getNormalizedPath(resourcePath.getKey()));
                resourcePathIter.remove();
            }
        }
        return resourcePathMap;
    }

    // ballerina -> OAS
    /**
     * This validation happens ballerina service against to openapi specification.
     *
     * @param resourcePaths
     * @param oasPaths
     */
    private void validateBallerinaAgainstToOAS(Map<String, ResourcePathSummary> resourcePaths,
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
                Map<String, Operation> operations = oasPath.getOperations();
                Operation oasOperation = operations.get(method.getKey());
                // Ballerina parameters validation
                List<Parameter> oasParameters = oasOperation.getParameters();
                validateBallerinaParameters(method, oasParameters);
                // Ballerina headers validation
                Map<String, Node> balHeaders = method.getValue().getHeaders();
                validateBallerinaHeaders(method, oasParameters, balHeaders);
                // Request body validation
                if (method.getValue().getBody() != null) {
                    RequestBodyValidator requestBodyValidator = new RequestBodyValidator(context, openAPI);
                    requestBodyValidator.validateBallerinaRequestBody(method, oasOperation);
                }
                // Return Type validation
                ReturnValidator returnValidator = new ReturnValidator(context, openAPI, method.getKey(),
                        getNormalizedPath(method.getValue().getPath()));
                ApiResponses responses = oasOperation.getResponses();
                ReturnTypeDescriptorNode returnNode = method.getValue().getReturnNode();
                returnValidator.validateReturnBallerinaToOas(returnNode, responses);

            }
        }
    }



    /**
     * This function is used to validate the ballerina resource header against to openapi header.
     *
     * @param method
     * @param oasParameters
     * @param balHeaders
     */
    private void validateBallerinaHeaders(Map.Entry<String, ResourceMethod> method, List<Parameter> oasParameters,
                           Map<String, Node> balHeaders) {

        for (Map.Entry<String, Node> balHeader: balHeaders.entrySet()) {
            //Todo: Nullable and default value assign scenarios

            // Available header types string|int|boolean|string[]|int[]|boolean[]|? headers
            // Need to remove `\`  while checking header name x\-client\-header
            Node headerNode = balHeader.getValue();
            String headerName = unescapeIdentifier(balHeader.getKey());
            String ballerinaType;
            NodeList<AnnotationNode> annotations;
            if (headerNode instanceof RequiredParameterNode) {
                RequiredParameterNode requireParam = (RequiredParameterNode) headerNode;
                ballerinaType = requireParam.typeName().toString().trim();
                annotations = requireParam.annotations();
            } else {
                DefaultableParameterNode defaultParam = (DefaultableParameterNode) headerNode;
                ballerinaType = defaultParam.typeName().toString().trim();
                annotations = defaultParam.annotations();
            }
            if (!annotations.isEmpty()) {
                List<String> headers = extractAnnotationFieldDetails(HTTP_HEADER, "name", annotations,
                        context.semanticModel());
                if (!headers.isEmpty()) {
                    headerName = headers.get(0);
                }
            }
            boolean isHeaderDocumented = false;
            if (oasParameters != null) {
                for (Parameter oasParameter: oasParameters) {
                    if (!(oasParameter instanceof HeaderParameter)) {
                        continue;
                    }
                    if (headerName.equals(oasParameter.getName())) {
                        isHeaderDocumented = true;
                        Optional<String> type =
                                convertOpenAPITypeToBallerina(oasParameter.getSchema().getType());
                        if (type.isEmpty() || !ballerinaType.equals(type.get())) {
                            updateContext(context, CompilationError.TYPE_MISMATCH_HEADER_PARAMETER,
                                    headerNode.location(), ballerinaType, oasParameter.getSchema().getType(),
                                    headerName, method.getKey(),
                                    getNormalizedPath(method.getValue().getPath()));
                            break;
                        }
                    }
                }
            }

            if (!isHeaderDocumented) {
                // undocumented header
                updateContext(context, CompilationError.UNDOCUMENTED_HEADER, balHeader.getValue().location(),
                        headerName, method.getKey(), getNormalizedPath(method.getValue().getPath()));
            }
        }
    }

    /**
     * This function is used to validate the ballerina resource parameter against to openapi parameters.
     *
     * @param method
     * @param oasParameters
     */
    private void validateBallerinaParameters(Map.Entry<String, ResourceMethod> method,
                                             List<Parameter> oasParameters) {

        for (Map.Entry<String, Node> parameter : method.getValue().getParameters().entrySet()) {
            boolean isExist = false;
            String parameterName = unescapeIdentifier(parameter.getKey());
            String ballerinaType;
            //Todo: Nullable and default value assign scenarios
            if (parameter.getValue() instanceof ParameterNode) {
                ParameterNode paramNode = (ParameterNode) parameter.getValue();
                if (paramNode instanceof RequiredParameterNode) {
                    RequiredParameterNode requireParam = (RequiredParameterNode) paramNode;
                    ballerinaType = requireParam.typeName().toString().trim();
                } else {
                    DefaultableParameterNode defaultParam = (DefaultableParameterNode) paramNode;
                    ballerinaType = defaultParam.typeName().toString().trim();
                }
            } else {
                ResourcePathParameterNode pathParameterNode = (ResourcePathParameterNode) parameter.getValue();
                ballerinaType = convertBallerinaType(pathParameterNode.typeDescriptor().kind()).orElse(null);
            }
            if (oasParameters != null) {
                for (Parameter oasParameter : oasParameters) {
                    if (!parameterName.equals(oasParameter.getName())) {
                        continue;
                    }
                    isExist = true;
                    Optional<String> type = convertOpenAPITypeToBallerina(oasParameter.getSchema().getType());
                    if (type.isEmpty() || !ballerinaType.equals(type.get())) {
                        updateContext(context, CompilationError.TYPE_MISMATCH_PARAMETER,
                                parameter.getValue().location(), ballerinaType, oasParameter.getSchema().getType(),
                                parameterName, method.getKey(), method.getValue().getPath());
                        break;
                    }
                }
            }
            if (!isExist) {
                // undocumented parameter
                updateContext(context, CompilationError.UNDOCUMENTED_PARAMETER, parameter.getValue().location(),
                        parameterName, method.getKey(), getNormalizedPath(method.getValue().getPath()));
            }
        }
    }

    // oas -> ballerina
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

