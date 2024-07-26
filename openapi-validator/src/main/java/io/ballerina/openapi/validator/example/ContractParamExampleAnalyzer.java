/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator.example;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.BALLERINA;
import static io.ballerina.openapi.validator.Constants.EMPTY;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.SERVICE_CONTRACT_TYPE;

/**
 * Analyzes the resource function parameters in the service contract and validates the example
 * annotation usage.
 *
 * @since 2.1.0
 */
public class ContractParamExampleAnalyzer extends ParameterExampleAnalyzer {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (diagnosticContainsErrors(context)) {
            return;
        }

        Node node = context.node();
        SemanticModel semanticModel = context.semanticModel();
        Optional<ObjectTypeSymbol> serviceContractObjType = getServiceContractObjType(semanticModel, node);

        if (serviceContractObjType.isEmpty()) {
            return;
        }
        serviceContractObjType.get().methods().values().forEach(method ->
                validateAnnotationOnResources(context, method, false, semanticModel));
    }

    private static Optional<ObjectTypeSymbol> getServiceContractObjType(SemanticModel semanticModel, Node node) {
        if (!(node instanceof ObjectTypeDescriptorNode objectTypeNode)) {
            return Optional.empty();
        }

        Optional<Symbol> serviceObjSymbol = semanticModel.symbol(objectTypeNode.parent());
        if (serviceObjSymbol.isEmpty() ||
                (!(serviceObjSymbol.get() instanceof TypeDefinitionSymbol serviceObjTypeDef))) {
            return Optional.empty();
        }

        Optional<Symbol> serviceContractType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                SERVICE_CONTRACT_TYPE);
        if (serviceContractType.isEmpty() ||
                !(serviceContractType.get() instanceof TypeDefinitionSymbol serviceContractTypeDef)) {
            return Optional.empty();
        }

        TypeSymbol serviceType = serviceObjTypeDef.typeDescriptor();
        if (serviceType.subtypeOf(serviceContractTypeDef.typeDescriptor()) &&
                serviceType instanceof ObjectTypeSymbol serviceObjType) {
            return Optional.of(serviceObjType);
        }

        return Optional.empty();
    }
}
