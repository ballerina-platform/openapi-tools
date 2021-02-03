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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
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
                //  Handle Path parameter
                if (operation.getParameters() != null) {
                    for (Parameter parameter : operation.getParameters()) {
                        if (resourceParameter.getKey().equals(parameter.getName()) && (parameter.getSchema() != null)) {
                            isParameterExit = true;
                            //Handle path parameter
                            if (resourceParameter.getValue() instanceof ResourcePathParameterNode) {
                                ResourcePathParameterNode paramNode =
                                        (ResourcePathParameterNode) resourceParameter.getValue();
                                TypeDescriptorNode typeNode = (TypeDescriptorNode) paramNode.typeDescriptor();
                                if (typeNode instanceof BuiltinSimpleNameReferenceNode) {
                                    Optional<Symbol> symbol = semanticModel.symbol(paramNode);
                                    TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
                                    List<ValidationError> validationErrors =
                                            TypeSymbolToJsonValidatorUtil.validate(parameter.getSchema(),
                                                    typeSymbol, syntaxTree, semanticModel,
                                                    resourceParameter.getKey());

                                }
                            }
                        }
//                        if (resourceParameter.getName().equals(parameter.getName()) &&
//                                (parameter.getSchema() != null)) {
//                            isParameterExit = true;
//                            List<ValidationError> validationErrorsResource =
//                                    BTypeToJsonValidatorUtil.validate(parameter.getSchema(),
//                                            resourceParameter.getParameter().symbol);
//                            if (!validationErrorsResource.isEmpty()) {
//                                validationErrors.addAll(validationErrorsResource);
//                            }
//                            break;
//                        }
                    }
                }
                if (!isParameterExit) {
//                    ValidationError validationError = new ValidationError(resourceParameter.getName(),
//                            BTypeToJsonValidatorUtil.convertTypeToEnum(resourceParameter.getType()));
//                    validationErrors.add(validationError);

                }
            }
        }
        return validationErrorList;
    }
}
