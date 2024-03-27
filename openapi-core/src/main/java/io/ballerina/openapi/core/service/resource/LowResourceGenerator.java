package io.ballerina.openapi.core.service.resource;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.response.ReturnTypeGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;

public class LowResourceGenerator extends ResourceGenerator {
    public LowResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public FunctionDefinitionNode generateResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                           List<Node> pathNodes, String path) {
        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
        List<Node> parameters = new ArrayList<>();
        // create parameter `http:Caller caller`
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_CALLER));
        IdentifierToken paramName = createIdentifierToken(GeneratorConstants.CALLER);
        RequiredParameterNode httpCaller = createRequiredParameterNode(createEmptyNodeList(), typeName, paramName);
        parameters.add(httpCaller);
        // create parameter `http:Request request`
        parameters.add(createToken(COMMA_TOKEN));
        BuiltinSimpleNameReferenceNode typeNameRequest = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_REQUEST));
        IdentifierToken paramNameRequest = createIdentifierToken(GeneratorConstants.REQUEST);
        RequiredParameterNode httpRequest = createRequiredParameterNode(createEmptyNodeList(), typeNameRequest,
                paramNameRequest);
        parameters.add(httpRequest);

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, path);
        ReturnTypeDescriptorNode returnTypeDescriptorNode = null;
        try {
            returnTypeDescriptorNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

        // create function signature
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(
                        SyntaxKind.OPEN_PAREN_TOKEN), parameterList, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
        // create function body
        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null, createEmptyNodeList(),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);

    }
}
