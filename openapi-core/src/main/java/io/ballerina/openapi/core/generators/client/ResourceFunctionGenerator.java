package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
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
    List<Diagnostic> diagnostics = new ArrayList<>();
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    String path;
    AuthConfigGeneratorImp authConfigGeneratorImp;
    BallerinaUtilGenerator ballerinaUtilGenerator;

    ResourceFunctionGenerator(Map.Entry<PathItem.HttpMethod, Operation> operation, String path, OpenAPI openAPI,
                              AuthConfigGeneratorImp authConfigGeneratorImp,
                              BallerinaUtilGenerator ballerinaUtilGenerator) {
        this.operation = operation;
        this.path = path;
        this.openAPI = openAPI;
        this.authConfigGeneratorImp = authConfigGeneratorImp;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
    }
    @Override
    public Optional<FunctionDefinitionNode> generateFunction() {

        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createToken(RESOURCE_KEYWORD), createToken(ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().toString().toLowerCase());
        // create relative path
        try {
            NodeList<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
                    null, openAPI.getComponents(), false);
            // Create function signature
            ResourceFunctionSignatureGenerator signatureGenerator = getSignatureGenerator();
            //Create function body
            Optional<FunctionBodyNode> functionBodyNodeResult = getFunctionBodyNode();
            if (functionBodyNodeResult.isEmpty()) {
                return Optional.empty();
            }
            FunctionBodyNode functionBodyNode = functionBodyNodeResult.get();
            return getFunctionDefinitionNode(qualifierList, functionKeyWord, functionName, relativeResourcePath, signatureGenerator, functionBodyNode);
        } catch (FunctionSignatureGeneratorException | BallerinaOpenApiException e) {
            //todo diagnostic
            return Optional.empty();
        }
    }

    protected ResourceFunctionSignatureGenerator getSignatureGenerator() {
        return new ResourceFunctionSignatureGenerator(operation.getValue(), openAPI, operation.getKey().toString().toLowerCase());
    }

    protected Optional<FunctionBodyNode> getFunctionBodyNode() throws BallerinaOpenApiException {
        FunctionBodyGeneratorImp functionBodyGenerator = new FunctionBodyGeneratorImp(path, operation, openAPI,
                authConfigGeneratorImp, ballerinaUtilGenerator);
        return functionBodyGenerator.getFunctionBodyNode();
    }

    protected Optional<FunctionDefinitionNode> getFunctionDefinitionNode(NodeList<Token> qualifierList, Token functionKeyWord, IdentifierToken functionName, NodeList<Node> relativeResourcePath, ResourceFunctionSignatureGenerator signatureGenerator, FunctionBodyNode functionBodyNode) throws FunctionSignatureGeneratorException {
        return Optional.of(NodeFactory.createFunctionDefinitionNode(RESOURCE_ACCESSOR_DEFINITION, null,
                qualifierList, functionKeyWord, functionName, relativeResourcePath,
                signatureGenerator.generateFunctionSignature().get(), functionBodyNode));
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
