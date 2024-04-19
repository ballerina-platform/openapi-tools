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

package io.ballerina.openapi.core.generators.service.resource;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.GeneratorConstants;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.service.signature.FunctionSignatureGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;

public class DefaultResourceGenerator extends ResourceGenerator {
    public DefaultResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public FunctionDefinitionNode generateResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                           String path) throws BallerinaOpenApiException {
        GeneratorUtils.addCommonParamsToOperationParams(operation, oasServiceMetadata.getOpenAPI(), path);
        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
                oasServiceMetadata.getOpenAPI().getComponents(), oasServiceMetadata.generateWithoutDataBinding(),
                diagnostics);
        FunctionSignatureGenerator functionSignatureGenerator = FunctionSignatureGenerator
                .getFunctionSignatureGenerator(oasServiceMetadata);
        FunctionSignatureNode functionSignatureNode = functionSignatureGenerator.getFunctionSignature(operation, path);
        if (functionSignatureGenerator.isNullableRequired()) {
            isNullableRequired = true;
        }
        diagnostics.addAll(functionSignatureGenerator.getDiagnostics());
        // Function Body Node
        // If path parameter has some special characters, extra body statements are added to handle the complexity.
        List<StatementNode> bodyStatements = GeneratorUtils.generateBodyStatementForComplexUrl(path);
        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null,
                bodyStatements.isEmpty() ?
                        createEmptyNodeList() :
                        createNodeList(bodyStatements),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);
    }
}
