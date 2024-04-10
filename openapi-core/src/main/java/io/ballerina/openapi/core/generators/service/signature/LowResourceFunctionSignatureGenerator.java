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

package io.ballerina.openapi.core.generators.service.signature;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.service.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.generators.service.response.ReturnTypeGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;

public class LowResourceFunctionSignatureGenerator extends FunctionSignatureGenerator {

    public LowResourceFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }
    @Override
    public FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                      String path) throws BallerinaOpenApiException {
        List<Node> parameters = new ArrayList<>();
        // create parameter `http:Caller caller`
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_CALLER));
        IdentifierToken paramName = createIdentifierToken(GeneratorConstants.CALLER);
        RequiredParameterNode httpCaller = createRequiredParameterNode(createEmptyNodeList(), typeName, paramName);
        parameters.add(httpCaller);
        // create parameter `http:Request request`
        parameters.add(createToken(COMMA_TOKEN));
        RequestBodyGenerator requestBodyGenerator = RequestBodyGenerator
                .getRequestBodyGenerator(oasServiceMetadata, path);
        RequiredParameterNode requestBodyNode = requestBodyGenerator.createRequestBodyNode(operation.getValue()
                .getRequestBody());
        parameters.add(requestBodyNode);
        diagnostics.addAll(requestBodyGenerator.getDiagnostics());

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, path);
        ReturnTypeDescriptorNode returnTypeDescriptorNode;
        returnTypeDescriptorNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        diagnostics.addAll(returnTypeGenerator.getDiagnostics());

        return createFunctionSignatureNode(createToken(
                        SyntaxKind.OPEN_PAREN_TOKEN), parameterList, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }
}
