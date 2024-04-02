package io.ballerina.openapi.core.service.response;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

public class LowResourceReturnTypeGenerator extends ReturnTypeGenerator {

    public LowResourceReturnTypeGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                String path) {
        return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                createSimpleNameReferenceNode(createIdentifierToken("error?")));
    }
}
