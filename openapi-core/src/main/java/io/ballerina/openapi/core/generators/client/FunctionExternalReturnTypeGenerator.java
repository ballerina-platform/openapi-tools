package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;

public class FunctionExternalReturnTypeGenerator extends FunctionStatusCodeReturnTypeGenerator {
    public FunctionExternalReturnTypeGenerator(Operation operation, OpenAPI openAPI) {
        super(operation, openAPI);
    }

    @Override
    public Optional<ReturnTypeDescriptorNode> getReturnType() {
        TypeDescriptorNode targetTypeOrError = createUnionTypeDescriptorNode(
                createSimpleNameReferenceNode(createIdentifierToken("targetType")),
                createToken(PIPE_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("error")));
        return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                targetTypeOrError));
    }
}
