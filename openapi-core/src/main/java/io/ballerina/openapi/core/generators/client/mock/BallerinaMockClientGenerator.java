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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;

/**
 * This generator class is for generate the mock clients.
 *
 * @since 2.1.0
 */
public class BallerinaMockClientGenerator extends BallerinaClientGenerator {
    public BallerinaMockClientGenerator(OASClientConfig oasClientConfig) {
        super(oasClientConfig);
    }

    public FunctionDefinitionNode getInitFunction() {
        FunctionSignatureNode functionSignatureNode = super.getInitFunctionSignatureNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        List<StatementNode> assignmentNodes = new ArrayList<>();
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        FunctionBodyBlockNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(FUNCTION_DEFINITION, super.getInitDocComment(), qualifierList,
                createToken(FUNCTION_KEYWORD), functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    @Override
    public ClassDefinitionNode getClassDefinitionNode() {
        List<Node> memberNodeList = new ArrayList<>();
        memberNodeList.addAll(createClassInstanceVariables());
        memberNodeList.add(getInitFunction());
        Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperations = filterOperations();
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        //Mock client function generation
        for (Map.Entry<String, Map<PathItem.HttpMethod, Operation>> operation : filteredOperations.entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operation.getValue().entrySet()) {
                MockClientFunctionGenerator mockClientFunctionGenerator = new MockClientFunctionGenerator(
                        operation.getKey(), operationEntry, oasClientConfig);
                Optional<FunctionDefinitionNode> funDefOptionalNode = mockClientFunctionGenerator.generateFunction();
                funDefOptionalNode.ifPresent(functionDefinitionNodeList::add);
                diagnostics.addAll(mockClientFunctionGenerator.getDiagnostics());
            }
        }
        memberNodeList.addAll(functionDefinitionNodeList);
        MetadataNode metadataNode = getClassMetadataNode();
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN), null);

    }

    @Override
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException, ClientException {
        return getSyntaxTree();
    }

    @Override
    public List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        // add apiKey instance variable when API key security schema is given
        ObjectFieldNode apiKeyFieldNode = authConfigGeneratorImp.getApiKeyMapClassVariable();
        if (apiKeyFieldNode != null) {
            fieldNodeList.add(apiKeyFieldNode);
        }
        return fieldNodeList;
    }
}
