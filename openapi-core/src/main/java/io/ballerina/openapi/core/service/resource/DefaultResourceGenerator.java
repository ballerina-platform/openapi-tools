package io.ballerina.openapi.core.service.resource;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.signature.FunctionSignatureGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;

public class DefaultResourceGenerator extends ResourceGenerator {
    public DefaultResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public FunctionDefinitionNode generateResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                           String path) throws BallerinaOpenApiException {
        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath;
        relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(), null,
                oasServiceMetadata.getOpenAPI().getComponents(), oasServiceMetadata.generateWithoutDataBinding());
        FunctionSignatureGenerator functionSignatureGenerator = FunctionSignatureGenerator
                .getFunctionSignatureGenerator(oasServiceMetadata);
        FunctionSignatureNode functionSignatureNode = functionSignatureGenerator.getFunctionSignature(operation, path);
        if (functionSignatureGenerator.isNullableRequired()) {
            isNullableRequired = true;
        }
        diagnostics.addAll(functionSignatureGenerator.getDiagnostics());
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
}
