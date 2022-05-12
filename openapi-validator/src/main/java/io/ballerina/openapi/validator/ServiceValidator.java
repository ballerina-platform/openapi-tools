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
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.openapi.validator.model.OpenAPIPathSummary;
import io.ballerina.openapi.validator.model.ResourceMethod;
import io.ballerina.openapi.validator.model.ResourcePathSummary;
import io.ballerina.openapi.validator.model.ReturnSummary;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.ballerina.openapi.validator.Constants.ARRAY;
import static io.ballerina.openapi.validator.Constants.DOUBLE;
import static io.ballerina.openapi.validator.Constants.FLOAT;
import static io.ballerina.openapi.validator.Constants.HEADER_NAME;
import static io.ballerina.openapi.validator.Constants.HTTP_HEADER;
import static io.ballerina.openapi.validator.Constants.NUMBER;
import static io.ballerina.openapi.validator.Constants.SQUARE_BRACKETS;
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
 * @since 1.1.0
 */
public class ServiceValidator {
    private SyntaxNodeAnalysisContext context;
    private OpenAPI openAPI;
    private Filter filter;
    private final Map<String, Map<String, ReturnSummary>> balReturnSummary = new HashMap<>();
    private final List<String> balReturnMediaTypes = new ArrayList<>();

    public void initialize(SyntaxNodeAnalysisContext context, OpenAPI openAPI, Filter filter) {
        this.context = context;
        this.openAPI = openAPI;
        this.filter = filter;
    }

    /**
     * Validate the given service. All the diagnostics may update with the @SyntaxNodeAnalysisContext context
     * dynamically.
     */
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
        Map<String, ResourcePathSummary> resourcePathMap = summarizeResources(resourceFunctions, context);

        // 4. Unimplemented resource in service file (extra resource in openapi spec)
        List<OpenAPIPathSummary> updatedOASPaths = unimplementedResourceFunction(openAPIPathSummaries,
                resourcePathMap);
        // 5. Undocumented resource in service file (extra resource in ballerina service)
        Map<String, ResourcePathSummary> updatedResourcePath = undocumentedResource(openAPIPathSummaries,
                resourcePathMap);

        // 6. Ballerina -> OAS validation
        validateBallerinaAgainstToOAS(updatedResourcePath, updatedOASPaths);
                // parameter , query, path - done
                // header - done
                // request body - done partial
                // return type - done partial

        // 7. OpenAPI -> Ballerina
        validateOASAgainstToBallerina(updatedOASPaths, updatedResourcePath);
    }

    /**
     * Validate all the resource with operations to check whether there is any missing implementation for operations.
     * OAS-> ballerina
     */
    private List<OpenAPIPathSummary> unimplementedResourceFunction(List<OpenAPIPathSummary> operations, Map<String,
            ResourcePathSummary> resources) {

        boolean filterEnable = filter.getOperation() != null || filter.getTag() != null ||
                filter.getExcludeTag() != null || filter.getExcludeOperation() != null;
        Iterator<OpenAPIPathSummary> openAPIPathIterator = operations.iterator();
        while (openAPIPathIterator.hasNext()) {
            OpenAPIPathSummary operationPath = openAPIPathIterator.next();
            // Unimplemented path
            if (!resources.containsKey(operationPath.getPath())) {
                if (!filterEnable) {
                    updateContext(context, CompilationError.UNIMPLEMENTED_RESOURCE_PATH, context.node().location(),
                            filter.getKind(),
                            getNormalizedPath(operationPath.getPath()));
                }
                openAPIPathIterator.remove();
            } else {
                // Unimplemented function
                ResourcePathSummary resourcePath = resources.get(operationPath.getPath());
                Map<String, ResourceMethod> resourceMethods = resourcePath.getMethods();
                Map<String, Operation> methods = operationPath.getOperations();
                for (Map.Entry<String, Operation> operation : methods.entrySet()) {
                    if (!resourceMethods.containsKey(operation.getKey().trim())) {
                        if (!filterEnable) {
                            updateContext(context, CompilationError.UNIMPLEMENTED_RESOURCE_FUNCTION,
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
    private Map<String, ResourcePathSummary> undocumentedResource(List<OpenAPIPathSummary> operations, Map<String,
            ResourcePathSummary> resourcePathMap) {
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
                                updateContext(context, CompilationError.UNDOCUMENTED_RESOURCE_FUNCTIONS,
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
                    updateContext(context, CompilationError.UNDOCUMENTED_RESOURCE_PATH, context.node().location(),
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
            Map<String, ReturnSummary> balMethodReturnSummary = new HashMap<>();

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
                    RequestBodyValidator requestBodyValidator = new RequestBodyValidator(context, openAPI,
                            filter.getKind());
                    requestBodyValidator.validateBallerinaRequestBody(method, oasOperation);
                }
                // Return Type validation
                ReturnValidator returnValidator = new ReturnValidator(context, openAPI, method.getKey(),
                        getNormalizedPath(method.getValue().getPath()), method.getValue().getLocation(),
                        filter.getKind());
                ApiResponses responses = oasOperation.getResponses();
                ReturnTypeDescriptorNode returnNode = method.getValue().getReturnNode();
                // If return type doesn't provide in service , then it maps to status code 202.
                if (returnNode == null) {
                    returnValidator.validateReturnBallerinaToOas(Optional.empty(), responses);
                } else {
                    returnValidator.validateReturnBallerinaToOas(
                            Optional.ofNullable((TypeDescriptorNode) returnNode.type()), responses);
                }
                ReturnSummary returnSummary = new ReturnSummary(returnValidator.getBalStatusCodes(),
                        returnValidator.getBalMediaTypes());
                balMethodReturnSummary.put(method.getKey(), returnSummary);
            }
            balReturnSummary.put(path.getKey(), balMethodReturnSummary);
        }
    }

    /**
     * This function is used to validate the ballerina resource header against to openapi header.
     *
     */
    private void validateBallerinaHeaders(Map.Entry<String, ResourceMethod> method, List<Parameter> oasParameters,
                           Map<String, Node> balHeaders) {
        for (Map.Entry<String, Node> balHeader: balHeaders.entrySet()) {
            //TODO: Nullable and default value assign scenarios

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
                List<String> headers = extractAnnotationFieldDetails(HTTP_HEADER, HEADER_NAME, annotations,
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
                        Schema schema = oasParameter.getSchema();
                        String headerType = schema.getType();
                        headerType = getNumberFormatType(oasParameter, headerType);
                        //Array type
                        if (schema instanceof ArraySchema) {
                            ArraySchema arraySchema = (ArraySchema) schema;
                            String items = arraySchema.getItems().getType();
                            Optional<String> arrayItemType = convertOpenAPITypeToBallerina(items);

                            //Array mapping
                            if (arrayItemType.isEmpty() || !ballerinaType.equals(arrayItemType.get() +
                                    SQUARE_BRACKETS)) {
                                // This special concatenation is used to check the array header parameters
                                updateContext(context, CompilationError.TYPE_MISMATCH_HEADER_PARAMETER,
                                        headerNode.location(),
                                        filter.getKind(),
                                        items + SQUARE_BRACKETS,
                                        ballerinaType,
                                        headerName,
                                        method.getKey(),
                                        getNormalizedPath(method.getValue().getPath()));
                                break;
                            }
                        }

                        Optional<String> type = convertOpenAPITypeToBallerina(headerType);
                        if (type.isEmpty() || !ballerinaType.equals(type.get())) {
                            updateContext(context, CompilationError.TYPE_MISMATCH_HEADER_PARAMETER,
                                    headerNode.location(),
                                    filter.getKind(),
                                    headerType,
                                    ballerinaType,
                                    headerName,
                                    method.getKey(),
                                    getNormalizedPath(method.getValue().getPath()));
                            break;
                        }
                    }
                }
            }

            if (!isHeaderDocumented) {
                // undocumented header
                updateContext(context, CompilationError.UNDOCUMENTED_HEADER, balHeader.getValue().location(),
                        filter.getKind(),
                        headerName, method.getKey(), getNormalizedPath(method.getValue().getPath()));
            }
        }
    }

    private String getNumberFormatType(Parameter oasParameter, String headerType) {
        if (headerType.equals(NUMBER)) {
            headerType = DOUBLE;
            if (oasParameter.getSchema().getFormat() != null &&
                    oasParameter.getSchema().getFormat().equals(FLOAT)) {
                headerType = FLOAT;
            }
        }
        return headerType;
    }

    /**
     * This function is used to validate the ballerina resource parameter against to openapi parameters.
     *
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
                    ballerinaType = requireParam.typeName().toString().trim().replaceAll("\\?", "");
                } else {
                    DefaultableParameterNode defaultParam = (DefaultableParameterNode) paramNode;
                    ballerinaType = defaultParam.typeName().toString().trim().replaceAll("\\?", "");
                }
            } else {
                ResourcePathParameterNode pathParameterNode = (ResourcePathParameterNode) parameter.getValue();
                ballerinaType = convertBallerinaType(pathParameterNode.typeDescriptor().kind()).orElse(null);
            }
            if (oasParameters != null) {
                for (Parameter oasParameter : oasParameters) {
                    String oasParameterName = oasParameter.getName();
                    Schema schema = oasParameter.getSchema();
                    if (oasParameter.get$ref() != null) {
                        Optional<String> name = extractReferenceType(oasParameter.get$ref());
                        if (name.isEmpty()) {
                            return;
                        }
                        oasParameterName = name.get();
                        schema = openAPI.getComponents().getParameters().get(oasParameterName).getSchema();
                        if (openAPI.getComponents().getParameters().get(oasParameterName).getName() != null) {
                            oasParameterName = openAPI.getComponents().getParameters().get(oasParameterName).getName();
                        }
                    }
                    //There are situation path parameter name change with its schema name, therefore we need to avoid
                    // name checking in path parameter
                    //ex:
                    // paths:
                    //  /applications/{obsId}/metrics:
                    //    get:
                    //      operationId: getMetrics
                    //      parameters:
                    //        - $ref: "#/components/parameters/obsIdPathParam"
                    if (!parameterName.equals(oasParameterName)) {
                        continue;
                    }
                    isExist = true;
                    String oasType = schema.getType();
                    oasType = getNumberFormatType(oasParameter, oasType);
                    if (oasType.equals(ARRAY) && schema instanceof ArraySchema) {
                        ArraySchema arraySchema = (ArraySchema) schema;
                        oasType = arraySchema.getItems().getType();
                    }
                    Optional<String> type = convertOpenAPITypeToBallerina(oasType);

                    //TODO: map<json> type matching
                    //TODO: Handle optional
                    //Array mapping
                    if (type.isEmpty() || ballerinaType.contains(SQUARE_BRACKETS) &&
                            !ballerinaType.equals(type.get() + SQUARE_BRACKETS)) {
                        // This special concatenation is used to check the array query parameters
                        updateContext(context, CompilationError.TYPE_MISMATCH_PARAMETER,
                                parameter.getValue().location(), filter.getKind(),  oasType +
                                        SQUARE_BRACKETS, ballerinaType,
                                parameterName, method.getKey(), method.getValue().getPath());
                        break;
                    }
                    if (!Objects.equals(ballerinaType, type.get())) {
                        // This special concatenation is used to check the array query parameters
                        updateContext(context, CompilationError.TYPE_MISMATCH_PARAMETER,
                                parameter.getValue().location(), filter.getKind(), oasType, ballerinaType,
                                parameterName, method.getKey(), method.getValue().getPath());
                        break;
                    }
                }
            }
            if (!isExist) {
                // undocumented parameter
                updateContext(context, CompilationError.UNDOCUMENTED_PARAMETER, parameter.getValue().location(),
                        filter.getKind(), parameterName, method.getKey(),
                        getNormalizedPath(method.getValue().getPath()));
            }
        }
    }

    /**
     * Validate openapi operation against to given ballerina resource.
     */
    private void validateOASAgainstToBallerina(List<OpenAPIPathSummary> oasPaths,
                                               Map<String, ResourcePathSummary> resourcePaths) {
        // Parameter validation
        // Headers validation
        // RequestBody validation
        // Return validation
        for (OpenAPIPathSummary openAPIPath: oasPaths) {

            String oasPath = openAPIPath.getPath();

            ResourcePathSummary resourcePath = resourcePaths.get(oasPath);
            Map<String, ResourceMethod> methods = resourcePath.getMethods();

            Map<String, Operation> operations = openAPIPath.getOperations();
            Map<String, ReturnSummary> returnSummaryMap = balReturnSummary.get(oasPath);
            operations.forEach((key, value) -> {
                ResourceMethod resourceMethod = methods.get(key);
                //Parameter validation
                List<Parameter> oasParameters = value.getParameters();
                Map<String, Node> resourceParameters = resourceMethod.getParameters();
                if (oasParameters != null) {
                    oasParameters.forEach(parameter -> {
                        if (parameter.get$ref() != null) {
                            Optional<String> parameterName = extractReferenceType(parameter.get$ref());
                            if (parameterName.isEmpty()) {
                                return;
                            }
                            parameter = openAPI.getComponents().getParameters().get(parameterName.get());
                        }
                        if (parameter instanceof HeaderParameter ||
                                parameter.getIn() != null && parameter.getIn().equals("header")) {
                            // headerValidation
                            Map<String, Node> resourceHeaders = resourceMethod.getHeaders();
                            AtomicBoolean isHeaderExist = new AtomicBoolean(false);
                            Parameter finalParameter = parameter;
                            resourceHeaders.forEach((header, headerNode) -> {
                                if (finalParameter.getName().trim().equals(header)) {
                                    isHeaderExist.set(true);
                                }
                            });
                            if (!isHeaderExist.get()) {
                                updateContext(context, CompilationError.UNIMPLEMENTED_HEADER,
                                        resourceMethod.getLocation(), filter.getKind(), parameter.getName(), key,
                                        oasPath);
                            }
                        } else {
                            AtomicBoolean isImplemented = new AtomicBoolean(false);
                            Parameter finalParameter1 = parameter;
                            resourceParameters.forEach((paramName, paramNode) -> {
                                // avoid headers
                                if (finalParameter1.getName().equals(paramName)) {
                                    isImplemented.set(true);
                                }
                            });
                            if (!isImplemented.get()) {
                                // error message
                                updateContext(context, CompilationError.UNIMPLEMENTED_PARAMETER,
                                        resourceMethod.getLocation(), filter.getKind(), parameter.getName(), key,
                                        oasPath);
                            }
                        }
                    });
                }
                // Request body
                RequestBody requestBody = value.getRequestBody();
                if (requestBody != null) {
                    RequiredParameterNode body = resourceMethod.getBody();
                    RequestBodyValidator rbValidator = new RequestBodyValidator(context, openAPI, filter.getKind());
                    rbValidator.validateOASRequestBody(body, requestBody, oasPath, key, resourceMethod.getLocation());
                }

                // Response validator
                ReturnValidator returnValidator = new ReturnValidator(context, openAPI, key, oasPath,
                        resourceMethod.getLocation(), filter.getKind());
                returnValidator.addAllBalStatusCodes(returnSummaryMap.get(key).getBalStatusCodes());
                returnValidator.addAllBalMediaTypes(returnSummaryMap.get(key).getBalMediaTypes());
                returnValidator.validateReturnOASToBallerina(value.getResponses());
            });
        }
    }
}
