package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class RequestBodyHeaderParameter implements ParameterGenerator {
    Map.Entry<String, Header> header;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();

    RequestBodyHeaderParameter(Map.Entry<String, Header> header) {
        this.header = header;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        Schema<?> schema = header.getValue().getSchema();
        Boolean required = header.getValue().getRequired();
        if (required == null) {
            required = false;
            schema.setNullable(true);
        }
        Optional<TypeDescriptorNode> typeNodeResult = TypeHandler.getInstance()
                .getTypeNodeFromOASSchema(schema, true);
        if (typeNodeResult.isEmpty()) {
            return Optional.empty();
        }
        if (required) {
            RequiredParameterNode requiredParameterNode = createRequiredParameterNode(createEmptyNodeList(),
                    typeNodeResult.get(), createIdentifierToken(getValidName(header.getKey(), false)));
            return Optional.of(requiredParameterNode);
        } else {
            NilLiteralNode nilLiteralNode =
                    createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
            return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNodeResult.get(),
                    createIdentifierToken(getValidName(header.getKey(), false)), createToken(EQUAL_TOKEN),
                    nilLiteralNode));
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }

}
