package io.ballerina.openapi.core.service.signature;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.service.response.ReturnTypeGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;

public class LowResourceFunctionSignatureGenerator extends FunctionSignatureGenerator {

    public LowResourceFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }
    @Override
    public FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                      String path) {
        List<Node> parameters = new ArrayList<>();
        // create parameter `http:Caller caller`
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_CALLER));
        IdentifierToken paramName = createIdentifierToken(GeneratorConstants.CALLER);
        RequiredParameterNode httpCaller = createRequiredParameterNode(createEmptyNodeList(), typeName, paramName);
        parameters.add(httpCaller);
        // create parameter `http:Request request`
        parameters.add(createToken(COMMA_TOKEN));
        RequestBodyGenerator requestBodyGenerator = RequestBodyGenerator
                .getRequestBodyGenerator(oasServiceMetadata, path);
        try {
            RequiredParameterNode requestBodyNode = requestBodyGenerator.createRequestBodyNode(operation.getValue()
                    .getRequestBody());
            parameters.add(requestBodyNode);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, path);
        ReturnTypeDescriptorNode returnTypeDescriptorNode;
        try {
            returnTypeDescriptorNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

        return createFunctionSignatureNode(createToken(
                        SyntaxKind.OPEN_PAREN_TOKEN), parameterList, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }
}
