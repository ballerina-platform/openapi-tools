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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;

public class ResourceFunctionGenerator implements FunctionGenerator {
    OpenAPI openAPI;
    List<ImportDeclarationNode> imports;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    String path;
    AuthConfigGeneratorImp authConfigGeneratorImp;
    BallerinaUtilGenerator ballerinaUtilGenerator;

    ResourceFunctionGenerator(Map.Entry<PathItem.HttpMethod, Operation> operation, String path, OpenAPI openAPI,
                              AuthConfigGeneratorImp authConfigGeneratorImp,
                              BallerinaUtilGenerator ballerinaUtilGenerator, List<ImportDeclarationNode> imports) {
        this.operation = operation;
        this.path = path;
        this.openAPI = openAPI;
        this.authConfigGeneratorImp = authConfigGeneratorImp;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
        this.imports = imports;
    }

    public List<ImportDeclarationNode> getImports() {
        return imports;
    }

    @Override
    public Optional<FunctionDefinitionNode> generateFunction() {

        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createToken(RESOURCE_KEYWORD), createToken(ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().toString().toLowerCase(Locale.ROOT));
        // create relative path
        try {
            NodeList<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
                    null, openAPI.getComponents(), false);
            // Create function signature
            ResourceFunctionSingnatureGenerator signatureGenerator = new ResourceFunctionSingnatureGenerator(
                    operation.getValue(), openAPI);
            //Create function body
            FunctionBodyGeneratorImp functionBodyGenerator = new FunctionBodyGeneratorImp(path, operation, openAPI,
                    authConfigGeneratorImp, ballerinaUtilGenerator, imports);
            Optional<FunctionBodyNode> functionBodyNodeResult = functionBodyGenerator.getFunctionBodyNode();
            if (functionBodyNodeResult.isEmpty()) {
                return Optional.empty();
            }
            FunctionBodyNode functionBodyNode = functionBodyNodeResult.get();
            return Optional.of(NodeFactory.createFunctionDefinitionNode(RESOURCE_ACCESSOR_DEFINITION, null,
                    qualifierList, functionKeyWord, functionName, relativeResourcePath,
                    signatureGenerator.generateFunctionSignature().get(), functionBodyNode));
        } catch (BallerinaOpenApiException e) {
            //todo diagnostic
            return Optional.empty();
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
