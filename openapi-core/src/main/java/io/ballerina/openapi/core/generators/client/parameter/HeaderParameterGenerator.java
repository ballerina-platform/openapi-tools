package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
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

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_TYPE_DESC;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class HeaderParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;

    public HeaderParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        //supported types string, int, boolean, decimal, float , primitive array
        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName().trim(), false));
        Optional<TypeDescriptorNode> typeNodeResult = TypeHandler.getInstance()
                .getTypeNodeFromOASSchema(parameter.getSchema(), true);
        if (typeNodeResult.isEmpty()) {
            ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_108, paramName.text());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }
        TypeDescriptorNode typeNode = typeNodeResult.get();
        Schema schema = parameter.getSchema();
        if (parameter.getRequired()) {
            return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeNode, paramName));
        } else if (schema.getDefault() != null) {
            LiteralValueToken literalValueToken;
            if (typeNode.kind().equals(STRING_TYPE_DESC)) {
                literalValueToken = createLiteralValueToken(null,
                        '"' + schema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                        createEmptyMinutiaeList());
            } else {
                literalValueToken = createLiteralValueToken(null, schema.getDefault().toString(),
                        createEmptyMinutiaeList(), createEmptyMinutiaeList());
            }
            return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNode, paramName,
                    createToken(EQUAL_TOKEN), literalValueToken));
        } else {
            String type = typeNode.toString();
            String nillableType = type.endsWith(NILLABLE) ? type : type + NILLABLE;
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(nillableType));
            NilLiteralNode nilLiteralNode =
                    createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
            return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeName, paramName,
                    createToken(EQUAL_TOKEN), nilLiteralNode));
        }
    }



    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

}
