package io.ballerina.openapi.core.generators.client.mock;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.FunctionGenerator;
import io.ballerina.openapi.core.generators.client.ResourceFunctionSignatureGenerator;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;
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

public class MockClientFunctionGenerator implements FunctionGenerator {
    String path;
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    OpenAPI openAPI;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    boolean isResourceFunction;
    OASClientConfig oasClientConfig;
    public MockClientFunctionGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation,
                                       OASClientConfig oasClientConfig) {
        this.path = path;
        this.operation = operation;
        this.openAPI = oasClientConfig.getOpenAPI();
        this.isResourceFunction = oasClientConfig.isResourceMode();
        this.oasClientConfig = oasClientConfig;
    }

    @Override
    public Optional<FunctionDefinitionNode> generateFunction() throws BallerinaOpenApiException {
        //function signature node, remote, resource
        if (isResourceFunction) {
            //function signature node, remote, resource
            //Create qualifier list
            NodeList<Token> qualifierList = createNodeList(createToken(RESOURCE_KEYWORD),
                    createToken(ISOLATED_KEYWORD));
            Token functionKeyWord = createToken(FUNCTION_KEYWORD);
            String method = operation.getKey().toString().toLowerCase(Locale.ROOT);
            IdentifierToken functionName = createIdentifierToken(method);

            List<Diagnostic> pathDiagnostics = new ArrayList<>();
            NodeList<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
                    openAPI.getComponents(), false, pathDiagnostics);
            if (!pathDiagnostics.isEmpty()) {
                pathDiagnostics.forEach(diagnostic -> {
                    if (diagnostic.diagnosticInfo().code().equals("OAS_COMMON_204")) {
                        DiagnosticMessages message = DiagnosticMessages.OAS_CLIENT_110;
                        ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(message, path, method);
                        diagnostics.add(clientDiagnostic);
                    }
                });
                return Optional.empty();
            }

            //create function signature
            ResourceFunctionSignatureGenerator signatureGenerator = new ResourceFunctionSignatureGenerator(
                    operation.getValue(), openAPI, operation.getKey().toString(), path);
            Optional<FunctionSignatureNode> signatureNodeOptional = signatureGenerator.generateFunctionSignature();
            diagnostics.addAll(signatureGenerator.getDiagnostics());
            if (signatureNodeOptional.isEmpty()) {
                return Optional.empty();
            }
            FunctionSignatureNode signatureNode = signatureNodeOptional.get();
            MockFunctionBodyGenerator bodyGenerator = new MockFunctionBodyGenerator(path, operation, openAPI,
                    oasClientConfig.isStatusCodeBinding());
            Optional<FunctionBodyNode> functionBodyOptionalNode = bodyGenerator.getFunctionBodyNode();
            if (functionBodyOptionalNode.isEmpty()) {
                return Optional.empty();
            }
            diagnostics.addAll(bodyGenerator.getDiagnostics());
            FunctionBodyNode functionBodyNode = functionBodyOptionalNode.get();
            return Optional.of(NodeFactory.createFunctionDefinitionNode(RESOURCE_ACCESSOR_DEFINITION, null,
                    qualifierList,
                    functionKeyWord, functionName, relativeResourcePath, signatureNode, functionBodyNode));
        } else {
            //function signature node, remote, resource
        }
        return Optional.empty();
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }
}
