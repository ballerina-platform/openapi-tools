package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
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
        RemoteFunctionSignatureGenerator signatureGenerator = getSignatureGenerator();
        //Create function body
        FunctionBodyNode functionBodyNode;
        Optional<FunctionBodyNode> functionBodyNodeResult = getFunctionBodyNode();
        if (functionBodyNodeResult.isEmpty()) {
            return Optional.empty();
        }
        functionBodyNode = functionBodyNodeResult.get();
        if (signatureGenerator.generateFunctionSignature().isEmpty()) {
            return Optional.empty();
        }

        return getFunctionDefinitionNode(qualifierList, functionKeyWord, functionName, signatureGenerator,
                functionBodyNode);

    }

    protected RemoteFunctionSignatureGenerator getSignatureGenerator() {
        return new RemoteFunctionSignatureGenerator(operation.getValue(), openAPI, operation.getKey().toString().toLowerCase());
    }

    protected Optional<FunctionDefinitionNode> getFunctionDefinitionNode(NodeList<Token> qualifierList,
                                                                         Token functionKeyWord,
                                                                         IdentifierToken functionName,
                                                                         RemoteFunctionSignatureGenerator
                                                                                 signatureGenerator,
                                                                         FunctionBodyNode functionBodyNode) {
        return Optional.of(NodeFactory.createFunctionDefinitionNode(OBJECT_METHOD_DEFINITION, null,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(),
                signatureGenerator.generateFunctionSignature().get(), functionBodyNode));
    }

    protected Optional<FunctionBodyNode> getFunctionBodyNode() {
        FunctionBodyGeneratorImp functionBodyGenerator = new FunctionBodyGeneratorImp(path, operation, openAPI,
                authConfigGeneratorImp, ballerinaUtilGenerator);
        return functionBodyGenerator.getFunctionBodyNode();
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return null;
    }
}
