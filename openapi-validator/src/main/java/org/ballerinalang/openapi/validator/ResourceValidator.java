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

package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import org.ballerinalang.openapi.validator.error.MissingFieldInBallerinaType;
import org.ballerinalang.openapi.validator.error.OneOfTypeValidation;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This util is checking the availability of services and the operations in contract and ballerina file.
 */
public class ResourceValidator {

    public static List<ValidationError> validateResourceAgainstOperation(Operation operation,
                                                                         ResourceMethod resourceMethod,
                                                                         SemanticModel semanticModel,
                                                                         SyntaxTree syntaxTree)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrorList = new ArrayList<>();
        if (!resourceMethod.getParameters().isEmpty()) {
            for (Map.Entry<String, Node> resourceParameter : resourceMethod.getParameters().entrySet()) {
                boolean isParameterExit = false;
                String paramType = "";
                RequiredParameterNode bodyNode = null;
                if (resourceParameter.getValue() instanceof RequiredParameterNode) {
                     bodyNode = (RequiredParameterNode) resourceParameter.getValue();
                }
                // Handle Path parameter
                if (operation.getParameters() != null) {
                    List<Parameter> parameters = operation.getParameters();
                    for (Parameter parameter : parameters) {
                        String resourceParam = resourceParameter.getKey().replaceFirst("'", "").trim();
                        if (resourceParam.equals(parameter.getName()) && (parameter.getSchema() != null)) {
                            isParameterExit = true;
                            // Handle path parameter
                            if (resourceParameter.getValue().kind().equals(SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM)) {
                                ResourcePathParameterNode paramNode =
                                        (ResourcePathParameterNode) resourceParameter.getValue();
                                paramType = paramNode.typeDescriptor().toString().trim();
                                TypeDescriptorNode typeNode = (TypeDescriptorNode) paramNode.typeDescriptor();
                                if (typeNode instanceof BuiltinSimpleNameReferenceNode) {
                                    Optional<Symbol> symbol = semanticModel.symbol(typeNode);
                                    TypeSymbol typeSymbol = null;
                                    if (symbol.isPresent() && symbol.orElseThrow().kind().equals(SymbolKind.TYPE)) {
                                         TypeReferenceTypeSymbol type = (TypeReferenceTypeSymbol) symbol.orElseThrow();
                                         typeSymbol = type.typeDescriptor();
                                    }

                                    List<ValidationError> validationErrors =
                                            TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                    typeSymbol, syntaxTree, semanticModel, resourceParameter.getKey(),
                                                    resourceParameter.getValue().location());

                                    if (!validationErrors.isEmpty()) {
                                        validationErrorList.addAll(validationErrors);
                                    }
                                }
                            }
                            //Handle query parameter
                            if (resourceParameter.getValue() instanceof RequiredParameterNode) {
                                RequiredParameterNode queryParam = (RequiredParameterNode) resourceParameter.getValue();
                                //equals or contains
                                if (!queryParam.toString().equals("http:Payload")) {
                                    paramType = queryParam.typeName().toString().trim();
                                    TypeSymbol typeSymbol = getTypeSymbol(semanticModel, queryParam);

                                    List<ValidationError> validationErrors =
                                            TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                    typeSymbol, syntaxTree, semanticModel,
                                                    resourceParameter.getKey(),
                                                    resourceParameter.getValue().location());

                                    if (!validationErrors.isEmpty()) {
                                        validationErrorList.addAll(validationErrors);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                // Handle Request Body
                if (operation.getRequestBody() != null && bodyNode != null) {
                    Map<String, Schema> requestBodyForOperation = getRequestBodyForOperation(operation);
                    for (Map.Entry<String, Schema> requestBody: requestBodyForOperation.entrySet()) {
                        if (resourceParameter.getKey().equals("payload")) {
                            isParameterExit = true;
                            Schema value = requestBody.getValue();
                            TypeSymbol typeSymbol = getTypeSymbol(semanticModel, bodyNode);

                            List<ValidationError> validationErrors =
                                    TypeSymbolToJsonValidatorUtil.validate(value, typeSymbol, syntaxTree, semanticModel,
                                            bodyNode.typeName().toString().trim(),
                                            resourceParameter.getValue().location());

                            if (!validationErrors.isEmpty()) {
                                validationErrorList.addAll(validationErrors);
                            }
                        }
                    }
                }
                if (!isParameterExit) {

                    ValidationError validationError = new ValidationError(resourceParameter.getKey(),
                            TypeSymbolToJsonValidatorUtil.convertTypeToEnum(paramType),
                            resourceParameter.getValue().location());

                    validationErrorList.add(validationError);
                }
            }
        }
        return validationErrorList;
    }

    static List<ValidationError> validateOperationAgainstResource(Operation operation,
                                                                  ResourceMethod resourceMethod,
                                                                  SemanticModel semanticModel,
                                                                  SyntaxTree syntaxTree,
                                                                  Location location)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        if (operation.getParameters() != null) {
            for (Parameter parameter: operation.getParameters()) {
                boolean isOParameterExist = false;
                if (!resourceMethod.getParameters().isEmpty()) {
                    Map<String, Node> parameters = resourceMethod.getParameters();
                    for (Map.Entry<String, Node> resourceParam: parameters.entrySet()) {
                        Node paramNode = resourceParam.getValue();
                        // Check query parameters
                        if (parameter instanceof QueryParameter && (parameter.getName().equals(resourceParam.getKey()))
                                && paramNode instanceof RequiredParameterNode) {
                            RequiredParameterNode queryParam = (RequiredParameterNode) paramNode;
                            //equals or contains
                            if (!queryParam.toString().equals("http:Payload")) {
                                isOParameterExist = true;
                                TypeSymbol typeSymbol = getTypeSymbol(semanticModel, queryParam);
                                List<ValidationError> validationErrors =
                                        TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                typeSymbol, syntaxTree, semanticModel,
                                                resourceParam.getKey(), location);
                                if (!validationErrors.isEmpty()) {
                                    validationErrorList.addAll(validationErrors);
                                }
                            }

                        }
                        //  Check whether it is path parameter
                        if ((parameter instanceof PathParameter) && (parameter.getName().equals(resourceParam.getKey()))
                                && paramNode instanceof ResourcePathParameterNode) {
                            isOParameterExist = true;
                            TypeDescriptorNode typeNode =
                                    (TypeDescriptorNode) ((ResourcePathParameterNode) paramNode).typeDescriptor();
                            Token paramName = ((ResourcePathParameterNode) paramNode).paramName();
                            if (typeNode instanceof BuiltinSimpleNameReferenceNode) {
                                Optional<Symbol> symbol = semanticModel.symbol(paramName);
                                TypeSymbol typeSymbol = null;
                                if (symbol.isPresent() && symbol.orElseThrow().kind().equals(SymbolKind.VARIABLE)) {
                                    VariableSymbol symbol1 = (VariableSymbol) symbol.orElseThrow();
                                    typeSymbol = symbol1.typeDescriptor();
                                }
                                List<ValidationError> validationErrors =
                                        TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                typeSymbol, syntaxTree, semanticModel,
                                                resourceParam.getKey(), location);
                                if (!validationErrors.isEmpty()) {
                                    validationErrorList.addAll(validationErrors);
                                }
                            }
                            break;
                        }
                    }
                }

                if (!isOParameterExist) {
                    ValidationError validationError = new ValidationError(parameter.getName(),
                            TypeSymbolToJsonValidatorUtil.convertTypeToEnum(parameter.getSchema().getType()), location);
                    validationErrorList.add(validationError);
                }
            }
        }
        //  Handle the requestBody
        if (operation.getRequestBody() != null) {
            Map<String, Node> resourceParams = resourceMethod.getParameters();
            Map<String, Schema> requestBodySchemas = getRequestBodyForOperation(operation);
            for (Map.Entry<String, Schema> operationRB: requestBodySchemas.entrySet()) {
                boolean isOParamExit = false;
                if (resourceMethod.getBody()) {
                    isOParamExit = validateRequestBodyOpenApiToResource(validationErrorList, resourceParams,
                            operationRB, isOParamExit, semanticModel, syntaxTree, location);
                }
                if (!isOParamExit) {
                    String type = "";
                    if (operationRB.getValue().getType() == null && (operationRB.getValue().getProperties() != null)) {
                        type = "object";
                    } else {
                        type = operationRB.getValue().getType();
                    }
                    ValidationError validationError = new ValidationError(operationRB.getKey(),
                            TypeSymbolToJsonValidatorUtil.convertTypeToEnum(type), location);
                    validationErrorList.add(validationError);
                }
            }
        }
        return validationErrorList;
    }

    private static TypeSymbol getTypeSymbol(SemanticModel semanticModel, RequiredParameterNode bodyNode) {
        Optional<Token> paramName = bodyNode.paramName();
        Token token = paramName.orElseThrow();
        Optional<Symbol> symbol = semanticModel.symbol(token);
        TypeSymbol typeSymbol = null;
        if (symbol.isPresent() && symbol.orElseThrow().kind().equals(SymbolKind.VARIABLE)) {
            VariableSymbol symbolVar = (VariableSymbol) symbol.orElseThrow();
            typeSymbol = symbolVar.typeDescriptor();
        } else if (symbol.isPresent() && symbol.orElseThrow().kind().equals(SymbolKind.PARAMETER)) {
            ParameterSymbol symbolParam = (ParameterSymbol) symbol.orElseThrow();
            typeSymbol = symbolParam.typeDescriptor();
        }

        return typeSymbol;
    }

    private static Map<String, Schema> getRequestBodyForOperation(Operation operation) {
        Map<String, Schema> requestBodySchemas = new HashMap<>();
        if (operation.getRequestBody() != null) {
            Content content = operation.getRequestBody().getContent();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                requestBodySchemas.put(mediaTypeEntry.getKey(), mediaTypeEntry.getValue().getSchema());
            }
        }
        return requestBodySchemas;
    }

    private static Boolean validateRequestBodyOpenApiToResource(List<ValidationError> validationErrorList,
                                                                Map<String, Node> resourceParam,
                                                                Map.Entry<String, Schema> operationRB,
                                                                Boolean isOParamExit,
                                                                SemanticModel semanticModel,
                                                                SyntaxTree syntaxTree,
                                                                Location location) throws OpenApiValidatorException {

        if (!resourceParam.isEmpty()) {
            for (Map.Entry<String, Node> resourceParameter : resourceParam.entrySet()) {
                if (resourceParameter.getValue() instanceof RequiredParameterNode) {
                    RequiredParameterNode bodyNode = (RequiredParameterNode) resourceParameter.getValue();
                    Iterator<AnnotationNode> iterator = bodyNode.annotations().iterator();
                    boolean isPayLoad = false;
                    while (iterator.hasNext()) {
                        AnnotationNode anno = iterator.next();
                        Node node = anno.annotReference();
                        if (node.toString().trim().equals("http:Payload")) {
                            isPayLoad = true;
                        }
                    }

                    if (isPayLoad) {
                        Schema value = operationRB.getValue();
                        TypeSymbol typeSymbol = getTypeSymbol(semanticModel, bodyNode);
                        List<ValidationError> validationErrors =
                                TypeSymbolToJsonValidatorUtil.validate(value, typeSymbol, syntaxTree, semanticModel,
                                        bodyNode.typeName().toString().trim(), location);
                        if (validationErrors.isEmpty()) {
                            isOParamExit = true;
                        } else {
                            for (ValidationError validEr: validationErrors) {
                                if ((validEr instanceof MissingFieldInBallerinaType) ||
                                        (validEr instanceof OneOfTypeValidation) ||
                                        (validEr instanceof TypeMismatch)) {
                                    validationErrorList.add(validEr);
                                    isOParamExit = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return isOParamExit;
    }
}
