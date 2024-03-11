package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.ballerinalang.model.tree.FunctionNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;

public class RemoteFunctionGenerator implements FunctionGenerator {
    String path;
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    List<Diagnostic> diagnostics;
    OpenAPI openAPI;
    RemoteFunctionGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation>
                                    operation, OpenAPI openAPI) {
        this.path = path;
        this.operation = operation;
        this.openAPI = openAPI;

    }
    @Override
    public FunctionNode generateFunction() {
        //Create qualifier list
        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(operation.getValue().getOperationId());
        // Create function signature
        RemoteFunctionSignatureGenerator signatureGenerator = new RemoteFunctionSignatureGenerator(operation.getValue(),
                openAPI.getComponents().getParameters());

        return null;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return null;
    }
}
