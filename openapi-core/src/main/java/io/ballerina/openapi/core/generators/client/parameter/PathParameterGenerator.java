package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.model.TypeGeneratorResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class PathParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;
    public PathParameterGenerator(Parameter parameter, OpenAPI openAPI){
        this.parameter = parameter;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName(), false));
        // type should be a any type node.
        Schema parameterSchema = parameter.getSchema();
        // Reference type resolve
        Optional<TypeDescriptorNode> typeNode = TypeHandler.getInstance()
                .getTypeNodeFromOASSchema(parameterSchema, true);
        if (typeNode.isEmpty()) {
//            throw new BallerinaOpenApiException("Error while generating type descriptor node for path parameter");
            //todo diagnostic
            return Optional.empty();
        }
        TypeDescriptorNode typeDescNode = typeNode.get();
        if (typeDescNode.kind().equals(SyntaxKind.ARRAY_TYPE_DESC)|| typeDescNode.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
            DiagnosticMessages diagMessages = DiagnosticMessages.OAS_CLIENT_101;
            ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(diagMessages.getCode(),
                    diagMessages.getDescription(), parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }

        return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeDescNode, paramName));
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
