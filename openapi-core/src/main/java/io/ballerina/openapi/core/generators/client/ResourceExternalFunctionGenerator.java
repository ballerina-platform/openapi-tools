package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExternalFunctionBodyNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EXTERNAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;

public class ResourceExternalFunctionGenerator extends ResourceFunctionGenerator {

    ResourceExternalFunctionGenerator(Map.Entry<PathItem.HttpMethod, Operation> operation, String path, OpenAPI openAPI,
                                      AuthConfigGeneratorImp authConfigGeneratorImp,
                                      BallerinaUtilGenerator ballerinaUtilGenerator) {
        super(operation, path, openAPI, authConfigGeneratorImp, ballerinaUtilGenerator);
    }

    @Override
    protected Optional<FunctionDefinitionNode> getFunctionDefinitionNode(NodeList<Token> qualifierList,
                                                                         Token functionKeyWord,
                                                                         IdentifierToken functionName,
                                                                         List<Node> relativeResourcePath,
                                                                         ResourceFunctionSignatureGenerator
                                                                                 signatureGenerator,
                                                                         FunctionBodyNode functionBodyNode)
            throws FunctionSignatureGeneratorException {
        BasicLiteralNode implFuncName = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"" + operation.getValue().getOperationId() +  "Impl\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode implFuncNameField = createSpecificFieldNode(null, createIdentifierToken("name"),
                createToken(COLON_TOKEN), implFuncName);
        MappingConstructorExpressionNode implFunctionMap = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(implFuncNameField), createToken(CLOSE_BRACE_TOKEN));
        SimpleNameReferenceNode annotationRef = createSimpleNameReferenceNode(createIdentifierToken("MethodImpl"));
        AnnotationNode implAnnotation = createAnnotationNode(createToken(AT_TOKEN), annotationRef, implFunctionMap);
        MetadataNode metadataNode = createMetadataNode(null, createNodeList(implAnnotation));
        return Optional.of(NodeFactory.createFunctionDefinitionNode(RESOURCE_ACCESSOR_DEFINITION, metadataNode,
                qualifierList, functionKeyWord, functionName, createNodeList(relativeResourcePath),
                signatureGenerator.generateFunctionSignature().get(), functionBodyNode));
    }

    @Override
    protected Optional<FunctionBodyNode> getFunctionBodyNode() {
        QualifiedNameReferenceNode javaMethodToken = createQualifiedNameReferenceNode(
                createIdentifierToken("java"), createToken(COLON_TOKEN), createIdentifierToken("Method"));
        BasicLiteralNode classValueExp = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"io.ballerina.openapi.client.GeneratedClient\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        BasicLiteralNode methodValueExp = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"" + getNativeMethodName() + "\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode classFieldNode = createSpecificFieldNode(null, createIdentifierToken("'class"),
                createToken(COLON_TOKEN), classValueExp);
        SpecificFieldNode methodFieldNode = createSpecificFieldNode(null, createIdentifierToken("name"),
                createToken(COLON_TOKEN), methodValueExp);
        MappingConstructorExpressionNode methodMapExp = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(classFieldNode, createToken(COMMA_TOKEN), methodFieldNode), createToken(CLOSE_BRACE_TOKEN));
        AnnotationNode javaMethodAnnot = createAnnotationNode(createToken(AT_TOKEN), javaMethodToken, methodMapExp);
        return Optional.of(createExternalFunctionBodyNode(createToken(EQUAL_TOKEN),
                createNodeList(javaMethodAnnot), createToken(EXTERNAL_KEYWORD), createToken(SEMICOLON_TOKEN)));
    }

    private String getNativeMethodName() {
        return operation.getValue().getParameters().stream().anyMatch(p -> p.getIn().equals("path")) ? "invokeResource"
                : "invokeResourceWithoutPath";
    }

    @Override
    protected ResourceFunctionSignatureGenerator getSignatureGenerator() {
        return new ResourceExternalFunctionSignatureGenerator(operation.getValue(), openAPI);
    }
}
