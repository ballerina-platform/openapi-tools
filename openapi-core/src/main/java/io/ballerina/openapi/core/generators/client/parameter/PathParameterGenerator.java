package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_101;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class PathParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;
    public PathParameterGenerator(Parameter parameter, OpenAPI openAPI) {
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
            diagnostics.add(new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_101, parameter.getName()));
            return Optional.empty();
        }
        TypeDescriptorNode typeDescNode = typeNode.get();
        if (typeDescNode.kind().equals(SyntaxKind.ARRAY_TYPE_DESC) ||
                typeDescNode.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
            ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(OAS_CLIENT_101, parameter.getName());
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
