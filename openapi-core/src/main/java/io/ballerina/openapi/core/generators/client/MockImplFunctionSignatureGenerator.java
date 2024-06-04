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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.generateStatusCodeTypeInclusionRecord;

/**
 * This class is for generating the mock advance client method signature.
 *
 * @since 2.1.0
 */
public class MockImplFunctionSignatureGenerator extends ImplFunctionSignatureGenerator {
    List<Diagnostic> diagnostics = new ArrayList<>();
    public MockImplFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod, String path,
                                              FunctionDefinitionNode clientExternFunction) {
        super(operation, openAPI, httpMethod, path, clientExternFunction);
        returnTypeDescriptorNode = getReturnTypeDescriptorNode();
    }

    private ReturnTypeDescriptorNode getReturnTypeDescriptorNode() {
        ApiResponses responses = operation.getResponses();
        ApiResponse successResponse = null;
        String code = null;
        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            if (response.getKey().startsWith("2")) {
                code = response.getKey().trim();
                successResponse = response.getValue();
                break;
            }
        }
        if (code == null) {
            return null;
        }
        Optional<TypeDescriptorNode> typeDescriptorNode = populateReturnTypeDesc(code, successResponse);
        return typeDescriptorNode.map(descriptorNode -> createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD),
                createEmptyNodeList(), descriptorNode)).orElse(null);
    }

    public Optional<TypeDescriptorNode> populateReturnTypeDesc(String responseCode, ApiResponse response) {
        String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);
        try {
            return Optional.of(generateStatusCodeTypeInclusionRecord(code, response, httpMethod, openAPI, path,
                    diagnostics));
        } catch (InvalidReferenceException e) {
            return Optional.empty();
        }
    }

    public List<Diagnostic> getDiagnostics () {
        return diagnostics;
    }
}
