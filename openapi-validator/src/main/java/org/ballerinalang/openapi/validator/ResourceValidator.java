/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.HashMap;
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
                if (operation.getParameters() != null && bodyNode.typeName().toString().equals("http:Payload")) {
                    List<Parameter> parameters = operation.getParameters();
                    for (Parameter parameter : parameters) {
                        String resourceParam = resourceParameter.getKey().replaceFirst("'","").trim();
                        if (resourceParam.equals(parameter.getName()) && (parameter.getSchema() != null)) {
                            isParameterExit = true;
                            // Handle path parameter
                            if (resourceParameter.getValue() instanceof ResourcePathParameterNode) {
                                ResourcePathParameterNode paramNode =
                                        (ResourcePathParameterNode) resourceParameter.getValue();
                                paramType = paramNode.typeDescriptor().toString().trim();
                                TypeDescriptorNode typeNode = (TypeDescriptorNode) paramNode.typeDescriptor();
                                Token paramName = paramNode.paramName();
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
                                                    resourceParameter.getKey());
                                    if (!validationErrors.isEmpty()) {
                                        validationErrorList.addAll(validationErrors);
                                    }
                                }
                            }
                            //Handle query parameter
                            if (resourceParameter.getValue() instanceof RequiredParameterNode) {
                                RequiredParameterNode queryParam = (RequiredParameterNode) resourceParameter.getValue();
                                paramType = queryParam.typeName().toString().trim();
                                TypeSymbol typeSymbol = getTypeSymbol(semanticModel, queryParam);
                                List<ValidationError> validationErrors =
                                        TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                typeSymbol, syntaxTree, semanticModel,
                                                resourceParameter.getKey());
                                if (!validationErrors.isEmpty()) {
                                    validationErrorList.addAll(validationErrors);
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
                                            bodyNode.typeName().toString().trim());
                            if (!validationErrors.isEmpty()) {
                                validationErrorList.addAll(validationErrors);
                            }
                        }
                    }
                }
                if (!isParameterExit) {
                    ValidationError validationError = new ValidationError(resourceParameter.getKey(),
                            TypeSymbolToJsonValidatorUtil.convertTypeToEnum(paramType));
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
            VariableSymbol symbol1 = (VariableSymbol) symbol.orElseThrow();
            typeSymbol = symbol1.typeDescriptor();
        }
        return typeSymbol;
    }

    public static Map<String, Schema> getRequestBodyForOperation(Operation operation) {
        Map<String, Schema> requestBodySchemas = new HashMap<>();
        if (operation.getRequestBody() != null) {
            Content content = operation.getRequestBody().getContent();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                requestBodySchemas.put(mediaTypeEntry.getKey(), mediaTypeEntry.getValue().getSchema());
            }
        }
        return requestBodySchemas;
    }
}
