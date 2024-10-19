/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core.generators.client.mock;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.BallerinaClientGeneratorWithStatusCodeBinding;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.client.FunctionBodyGenerator;
import io.ballerina.openapi.core.generators.client.ImplFunctionSignatureGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;

/**
 * This class contains the advance client generation when the client generation enables with status code bindings.
 *
 * @since 2.1.0
 */
public class AdvanceMockClientGenerator extends BallerinaClientGeneratorWithStatusCodeBinding {
    public AdvanceMockClientGenerator(OASClientConfig oasClientConfig) {
        super(oasClientConfig);
    }

    @Override
    public FunctionBodyNode getInitFunctionBodyNode() {
        List<StatementNode> assignmentNodes = new ArrayList<>();
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    @Override
    public FunctionBodyGenerator getFunctionBodyGeneratorImp(String path,
                                                             Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                             OpenAPI openAPI,
                                                             AuthConfigGeneratorImp authConfigGeneratorImp,
                                                             BallerinaUtilGenerator ballerinaUtilGenerator,
                                                             boolean hasDefaultResponse,
                                                             List<String> nonDefaultStatusCodes,
                                                             ImplFunctionSignatureGenerator signatureGenerator) {
        return new MockFunctionBodyGenerator(path, operation, openAPI, true);
    }

    @Override
    public List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        // add apiKey instance variable when API key security schema is given
        ObjectFieldNode apiKeyFieldNode = authConfigGeneratorImp.getApiKeyMapClassVariable();
        if (apiKeyFieldNode != null) {
            apiKeyFieldNode = apiKeyFieldNode.modify(apiKeyFieldNode.metadata().orElse(null),
                    apiKeyFieldNode.visibilityQualifier().orElse(null),
                    apiKeyFieldNode.qualifierList(),
                    apiKeyFieldNode.typeName(),
                    apiKeyFieldNode.fieldName(),
                    createToken(EQUAL_TOKEN),
                    NodeParser.parseExpression("{ apikey: \"\"}"), apiKeyFieldNode.semicolonToken());

            fieldNodeList.add(apiKeyFieldNode);
        }
        return fieldNodeList;
    }

}
