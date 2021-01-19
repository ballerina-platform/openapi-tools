/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.List;

//import io.ballerina.compiler.syntax.tree.ServiceBodyNode;

/**
 * java.
 */
public class SyntaxTreeToJsonValidatorUtil {

    public static List<ValidationError> validate(Schema<?> schema, SyntaxTree syntaxTree, SemanticModel semanticModel)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrorList = new ArrayList<>();
        final TypeSymbol[] paramType = {null};
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();

            //------------comment due to service implementing happening
            if (syntaxKind.equals(SyntaxKind.TYPE_DEFINITION)) {
                TypeDefinitionNode typeNode = (TypeDefinitionNode) node;
            }
            // Take the service for validation
//            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
//                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
//                ServiceBodyNode srvBNode = (ServiceBodyNode) serviceDeclarationNode.serviceBody();
            // Extract Service Body for validate
            // Get resource list
//                NodeList<Node> resourceList = serviceDeclarationNode.resources();
//                for (Node resource : resourceList) {
//                    if (resource instanceof FunctionDefinitionNode) {
//                        FunctionDefinitionNode functionNode = (FunctionDefinitionNode) resource;
//                        FunctionSignatureNode functionSignatureNode = functionNode.functionSignature();
//                        SeparatedNodeList<ParameterNode> parameterList = functionSignatureNode.parameters();
//                        for (ParameterNode paramNode : parameterList) {
//                            if (paramNode instanceof RequiredParameterNode) {
//                                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) paramNode;
//                                if (requiredParameterNode.typeName().kind()
//                                        .equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
//                                    Optional<Symbol> symbol = semanticModel.symbol(syntaxTree.filePath(),
//                                            LinePosition.from(requiredParameterNode.lineRange().startLine().line(),
//                                                    requiredParameterNode.lineRange().startLine().offset()));
//                                    symbol.ifPresent(symbol1 -> {
//                                        paramType[0] = ((TypeReferenceTypeSymbol) symbol1).typeDescriptor();
//                                    });
//                                    // TypeSymbol validator
//                                    validationErrorList = TypeSymbolToJsonValidatorUtil.validate(schema, paramType[0]
//                                            , requiredParameterNode.syntaxTree(),
//                                            semanticModel);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }

//                --------------------------------------------------------

//        ModulePartNode modulePartNode = syntaxTree.rootNode();
//        for (Node node : modulePartNode.members()) {
//            SyntaxKind syntaxKind = node.kind();
//            System.out.println(syntaxKind);
//        }
//                ---------------------------------------------------

        return validationErrorList;
    }
}
