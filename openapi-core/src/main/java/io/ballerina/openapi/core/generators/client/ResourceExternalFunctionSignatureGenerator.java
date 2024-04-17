/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.InferredTypedescDefaultNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Objects;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createInferredTypedescDefaultNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParameterizedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.GT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPEDESC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPEDESC_TYPE_DESC;

/**
 * This class is used to generate the function signature of the external resource function for the client.
 *
 * @since 1.9.0
 */
public class ResourceExternalFunctionSignatureGenerator extends ResourceFunctionSignatureGenerator {

    public ResourceExternalFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod,
                                                      String path) {
        super(operation, openAPI, httpMethod);
        this.functionReturnTypeGenerator = new FunctionExternalReturnTypeGenerator(operation, openAPI, httpMethod,
                path);
    }

    @Override
    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        ParametersInfo parametersInfo = super.getParametersInfo(parameters);

        if (parametersInfo == null) {
            return null;
        }

        FunctionReturnTypeGeneratorImp functionReturnTypeGenerator = getFunctionReturnTypeGenerator();
        FunctionReturnTypeGeneratorImp.ReturnTypesInfo returnTypeInfo = functionReturnTypeGenerator.
                getAlreadyDefinedReturnTypeInfo();

        if (Objects.isNull(returnTypeInfo)) {
            returnTypeInfo = functionReturnTypeGenerator.getReturnTypeInfo();
        }

        List<TypeDescriptorNode> returnTypes = returnTypeInfo.types();
        TypeDescriptorNode returnType;
        if (returnTypes.isEmpty()) {
            diagnostics.add(new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_114, operation.getOperationId()));
            return null;
        } else {
            returnType = FunctionReturnTypeGeneratorImp.createUnionReturnType(returnTypes);
        }

        TypeParameterNode returnTypeParam = createTypeParameterNode(createToken(LT_TOKEN), returnType,
                createToken(GT_TOKEN));
        TypeDescriptorNode targetType = createParameterizedTypeDescriptorNode(TYPEDESC_TYPE_DESC,
                createToken(TYPEDESC_KEYWORD), returnTypeParam);
        return populateTargetTypeParam(targetType, parametersInfo);
    }

    protected ParametersInfo populateTargetTypeParam(TypeDescriptorNode targetType, ParametersInfo parametersInfo) {
        InferredTypedescDefaultNode inferredToken = createInferredTypedescDefaultNode(createToken(LT_TOKEN),
                createToken(GT_TOKEN));
        ParameterNode targetTypeParam = createDefaultableParameterNode(createEmptyNodeList(), targetType,
                createIdentifierToken("targetType"), createToken(EQUAL_TOKEN), inferredToken);
        parametersInfo.defaultable().add(targetTypeParam);
        parametersInfo.defaultable().add(createToken(COMMA_TOKEN));
        return parametersInfo;
    }
}
