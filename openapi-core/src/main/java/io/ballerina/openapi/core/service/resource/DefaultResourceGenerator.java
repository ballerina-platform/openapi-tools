package io.ballerina.openapi.core.service.resource;

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
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.parameter.ParametersGeneratorImpl;
import io.ballerina.openapi.core.service.parameter.DefaultRequestBodyGenerator;
import io.ballerina.openapi.core.service.response.ReturnTypeGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.getValidName;

public class DefaultResourceGenerator extends ResourceGenerator {
    public DefaultResourceGenerator(OASServiceMetadata oasServiceMetadata) {
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
        ParametersGeneratorImpl parametersGenerator = new ParametersGeneratorImpl(false,
                oasServiceMetadata.getOpenAPI());
        try {
            parametersGenerator.generateResourcesInputs(operation);
        } catch (OASTypeGenException e) {
            throw new RuntimeException();
        }
        List<Node> params = new ArrayList<>(parametersGenerator.getRequiredParams());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            RequiredParameterNode nodeForRequestBody = null;
            if (requestBody.getContent() != null) {
                DefaultRequestBodyGenerator requestBodyGen = new DefaultRequestBodyGenerator(requestBody);
                try {
                    nodeForRequestBody = requestBodyGen.createRequestBodyNode();
                } catch (OASTypeGenException e) {
                    throw new RuntimeException(e);
                }
                params.add(nodeForRequestBody);
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }

        // For creating the order of the parameters in the function
        if (!parametersGenerator.getDefaultableParams().isEmpty()) {
            params.addAll(parametersGenerator.getDefaultableParams());
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }

//        if (!oasServiceMetadata.isNullable()) {
//            isNullableRequired = parametersGenerator.isNullableRequired();
//        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
        String pathForRecord = Objects.equals(path, GeneratorConstants.SLASH) || Objects.equals(path, GeneratorConstants.CATCH_ALL_PATH) ? "" :
                getValidName(path, true);
        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, pathForRecord);
        ReturnTypeDescriptorNode returnNode;
        try {
            returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(
                createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnNode);

        // Function Body Node
        // If path parameter has some special characters, extra body statements are added to handle the complexity.
        List<StatementNode> bodyStatements = GeneratorUtils.generateBodyStatementForComplexUrl(path);
        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null,
                bodyStatements.isEmpty() ?
                        createEmptyNodeList() :
                        createNodeList(bodyStatements),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);
    }

    /**
     * Resolve requestBody reference.
     */
    private RequestBody resolveRequestBodyReference(RequestBody requestBody) {
        if (requestBody.get$ref() != null) {
            try {
                String requestBodyName = GeneratorUtils.extractReferenceType(requestBody.get$ref());
                requestBody = resolveRequestBodyReference(oasServiceMetadata.getOpenAPI().getComponents()
                        .getRequestBodies().get(requestBodyName.trim()));
            } catch (OASTypeGenException e) {
                throw new RuntimeException();
            }
        }
        return requestBody;
    }
}
