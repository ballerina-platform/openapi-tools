package io.ballerina.openapi.core.generators.client.mock;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.openapi.core.generators.client.FunctionBodyGenerator;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;

/**
 * Mock function body generator.
 * @since 2.1.0
 */
public class MockFunctionBodyGenerator implements FunctionBodyGenerator {
    String path;
    Map.Entry<PathItem.HttpMethod, Operation> operation;
    OpenAPI openAPI;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    boolean isAdvanceClient;


    public MockFunctionBodyGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation,
                                     OpenAPI openAPI, boolean isAdvanceClient) {
        this.path = path;
        this.operation = operation;
        this.openAPI = openAPI;
        this.isAdvanceClient = isAdvanceClient;
    }

    @Override
    public Optional<FunctionBodyNode> getFunctionBodyNode() {

        //Check inline example
        ApiResponses responses = operation.getValue().getResponses();
        //Get the successful response
        ApiResponse successResponse = null;
        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            if (response.getKey().startsWith("2")) {
                successResponse = response.getValue();
                break;
            }
        }
        // Here only consider 2xx response
        if (successResponse == null || successResponse.getContent() == null) {
            return Optional.empty();
        }
        // Get the example
        Map<?, ?> examples = null;
        for (Map.Entry<String, MediaType> mediaType : successResponse.getContent().entrySet()) {
            // handle reference
            if (mediaType.getValue().getExamples() != null) {
                examples = mediaType.getValue().getExamples();
            }
        }

        // check ref example
        if (examples == null) {
            ClientDiagnosticImp diagnosticImp = new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_116,
                    path, operation.getKey().toString());
            diagnostics.add(diagnosticImp);
            return Optional.empty();
        }
        Object response = examples.get("response");
        if (response == null) {
            ClientDiagnosticImp diagnosticImp = new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_116,
                    path, operation.getKey().toString());
            diagnostics.add(diagnosticImp);
            return Optional.empty();
        }
        String exampleValue = ((Example) response).getValue().toString();
        String statement;
        if (isAdvanceClient) {
            statement = "return {\n" +
                    "            body : " + exampleValue + ",\n" +
                    "            headers: {}\n" +
                    "        };";
        } else {
            statement = "return " + exampleValue + ";";
        }
        StatementNode returnNode = NodeParser.parseStatement(statement);
        NodeList<StatementNode> statementList = createNodeList(returnNode);

        FunctionBodyBlockNode fBodyBlock = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
        return Optional.of(fBodyBlock);
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
