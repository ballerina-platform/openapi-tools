package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OBJECT_METHOD_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;

public class RemoteFunctionGenerator implements FunctionGenerator {
    String path;
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    List<Diagnostic> diagnostics;
    OpenAPI openAPI;
    AuthConfigGeneratorImp authConfigGeneratorImp;
    BallerinaUtilGenerator ballerinaUtilGenerator;
    RemoteFunctionGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation, OpenAPI openAPI,
                            AuthConfigGeneratorImp authConfigGeneratorImp,
                            BallerinaUtilGenerator ballerinaUtilGenerator) {
        this.path = path;
        this.operation = operation;
        this.openAPI = openAPI;
        this.authConfigGeneratorImp = authConfigGeneratorImp;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;

    }
    @Override
    public Optional<FunctionDefinitionNode> generateFunction() {
        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getValue().getOperationId());
        // Create function signature
        RemoteFunctionSignatureGenerator signatureGenerator = new RemoteFunctionSignatureGenerator(operation.getValue(),
                openAPI);
        //Create function body
        FunctionBodyNode functionBodyNode;
        try {
            Optional<FunctionBodyNode> functionBodyNodeResult = getFunctionBodyNode();
            if (functionBodyNodeResult.isEmpty()) {
                return Optional.empty();
            }
            functionBodyNode = functionBodyNodeResult.get();
        } catch (BallerinaOpenApiException e) {
            //todo diagnostic
            diagnostics.add(null);
            return Optional.empty();
        }

        try {
            return getFunctionDefinitionNode(qualifierList, functionKeyWord, functionName, signatureGenerator,
                    functionBodyNode);
        } catch (FunctionSignatureGeneratorException e) {
            //todo diagnostic
            return Optional.empty();
        }
    }

    protected Optional<FunctionDefinitionNode> getFunctionDefinitionNode(NodeList<Token> qualifierList,
                                                                         Token functionKeyWord,
                                                                         IdentifierToken functionName,
                                                                         RemoteFunctionSignatureGenerator
                                                                                 signatureGenerator,
                                                                         FunctionBodyNode functionBodyNode)
            throws FunctionSignatureGeneratorException {
        return Optional.of(NodeFactory.createFunctionDefinitionNode(OBJECT_METHOD_DEFINITION, null,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(),
                signatureGenerator.generateFunctionSignature(), functionBodyNode));
    }

    protected Optional<FunctionBodyNode> getFunctionBodyNode() throws BallerinaOpenApiException {
        FunctionBodyGeneratorImp functionBodyGenerator = new FunctionBodyGeneratorImp(path, operation, openAPI,
                authConfigGeneratorImp, ballerinaUtilGenerator);
        return functionBodyGenerator.getFunctionBodyNode();
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return null;
    }
}
