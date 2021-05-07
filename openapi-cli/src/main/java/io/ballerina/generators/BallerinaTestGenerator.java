package io.ballerina.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;

/**
 * This class use for generating boilerplate codes for test cases.
 */
public class BallerinaTestGenerator {
    static List<String> remoteFunctionNameList = new ArrayList<>();

    @Nonnull
    public static SyntaxTree generateSyntaxTree(Path definitionPath, Filter filter)
            throws IOException, BallerinaOpenApiException {
        ImportDeclarationNode importForTest = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA,
                GeneratorConstants.MODULE_TEST);
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForTest);
        List<FunctionDefinitionNode> functions = new ArrayList<>();
        getFunctionDefinitionNode(functions);

        // Get OpenAPI object from the swagger parser
//        OpenAPI openAPI = getBallerinaOpenApiType(definitionPath);
//        if (!openAPI.getPaths().isEmpty()) {
//            Paths paths = GeneratorUtils.setOperationId(openAPI.getPaths());
//            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
//            for (Map.Entry<String, PathItem> path : pathsItems) {
//                if (!path.getValue().readOperationsMap().isEmpty()) {
//                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
//                    for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
//                        List<String> filterTags = filter.getTags();
//                        List<String> operationTags = operation.getValue().getTags();
//                        List<String> filterOperations = filter.getOperations();
//                        if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
//                            // Add only the filtered operations to the
//                            if (operationTags != null || ((!filterOperations.isEmpty())
//                                    && (operation.getValue().getOperationId() != null))) {
//                                if (GeneratorUtils.hasTags(operationTags, filterTags) ||
//                                        ((operation.getValue().getOperationId() != null) &&
//                                                filterOperations.contains(
//                                                operation.getValue().getOperationId().trim()))) {
//                                    getFunctionDefinitionNode(functions, operation);
//                                }
//                            }
//                        } else {
//                            getFunctionDefinitionNode(functions, operation);
//                        }
//                    }
//                }
//            }
//        }

        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>(functions);
        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, createNodeList(nodes), eofToken);
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private static void getFunctionDefinitionNode(List<FunctionDefinitionNode> functions) {
        if (!remoteFunctionNameList.isEmpty()) {
            for (String functionName : remoteFunctionNameList) {
                MetadataNode metadataNode = getTestAnnotation();
                Token functionKeyWord = createToken(FUNCTION_KEYWORD);
                IdentifierToken testFunctionName = createIdentifierToken(GeneratorConstants.PREFIX_TEST
                        + modifyFunctionName(functionName.trim()));
                FunctionSignatureNode functionSignatureNode = getFunctionSignature();
                FunctionBodyNode functionBodyNode = getFunctionBody();
                NodeList<Node> relativeResourcePath = createEmptyNodeList();
                NodeList<Token> qualifierList = createEmptyNodeList();
                FunctionDefinitionNode functionDefinitionNode = createFunctionDefinitionNode(FUNCTION_DEFINITION,
                        metadataNode, qualifierList, functionKeyWord, testFunctionName, relativeResourcePath,
                        functionSignatureNode, functionBodyNode);
                functions.add(functionDefinitionNode);
            }
        }
    }

    /**
     * Generate Function Signature Node.
     */
    private static FunctionSignatureNode getFunctionSignature() {
        List<Node> parameterList = new ArrayList<>();
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),
                parameters, createToken(CLOSE_PAREN_TOKEN), null);
    }

    /**
     * Generate Function Body Node.
     */
    private static FunctionBodyNode getFunctionBody() {
        List<StatementNode> statementsList = new ArrayList<>();
        NodeList<StatementNode> statementsNodeList = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementsNodeList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate Metadata Node.
     */
    private static MetadataNode getTestAnnotation() {
        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANNOT_TEST));
        List<Node> fileds = new ArrayList<>();
        SeparatedNodeList<MappingFieldNode> fieldNodesList = createSeparatedNodeList(fileds);
        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), fieldNodesList, createToken(CLOSE_BRACE_TOKEN));
        AnnotationNode annotationNode = createAnnotationNode(createToken(SyntaxKind.AT_TOKEN),
                annotateReference, annotValue);
        return createMetadataNode(null, createNodeList(annotationNode));
    }

    /**
     * Modify function name by capitalizing the first letter.
     */
    private static String modifyFunctionName(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Generate Function Definition Node.
     */
/*    private static void getFunctionDefinitionNode(List<FunctionDefinitionNode> functions,
Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        MetadataNode metadataNode = getTestAnnotation();
        Token functionKeyWord = createToken(FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(GeneratorConstants.PREFIX_TEST +
        operation.getValue().getOperationId().trim());
        FunctionSignatureNode functionSignatureNode = getFunctionSignature();
        FunctionBodyNode functionBodyNode = getFunctionBody();
        NodeList<Node> relativeResourcePath = createEmptyNodeList();
        NodeList<Token> qualifierList = createEmptyNodeList();
        FunctionDefinitionNode functionDefinitionNode = createFunctionDefinitionNode(FUNCTION_DEFINITION, metadataNode,
                qualifierList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyNode);
        functions.add(functionDefinitionNode);
    }*/
}
