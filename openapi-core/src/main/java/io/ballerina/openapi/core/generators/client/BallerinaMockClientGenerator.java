package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.mock.MockClientFunctionGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.ClientDocCommentGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;

public class BallerinaMockClientGenerator extends BallerinaClientGenerator {
    public BallerinaMockClientGenerator(OASClientConfig oasClientConfig) {
        super(oasClientConfig);
    }

    //override init function generation
    public FunctionDefinitionNode getInitFunction() {
        FunctionSignatureNode functionSignatureNode = super.getInitFunctionSignatureNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        List<StatementNode> assignmentNodes = new ArrayList<>();
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        FunctionBodyBlockNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(FUNCTION_DEFINITION, super.getInitDocComment(), qualifierList,
                createToken(FUNCTION_KEYWORD), functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    //override client function generation
    public ClassDefinitionNode getClientFunction() throws BallerinaOpenApiException {
        List<Node> memberNodeList = new ArrayList<>();
        memberNodeList.addAll(createClassInstanceVariables());
        memberNodeList.add(getInitFunction());
        Map<String, Map<PathItem.HttpMethod, Operation>> filteredOperations = filterOperations();
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();
        //Mock client function generation
        for (Map.Entry<String, Map<PathItem.HttpMethod, Operation>> operation : filteredOperations.entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operation.getValue().entrySet()) {
                MockClientFunctionGenerator mockClientFunctionGenerator = new MockClientFunctionGenerator(
                        operation.getKey(), operationEntry, oasClientConfig);
                Optional<FunctionDefinitionNode> funDefOptionalNode = mockClientFunctionGenerator.generateFunction();
                funDefOptionalNode.ifPresent(functionDefinitionNodeList::add);
                diagnostics.addAll(mockClientFunctionGenerator.getDiagnostics());
            }
        }
        memberNodeList.addAll(functionDefinitionNodeList);
        MetadataNode metadataNode = getClassMetadataNode();
        IdentifierToken className = createIdentifierToken(GeneratorConstants.CLIENT);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(CLOSE_BRACE_TOKEN), null);

    }

    @Override
    protected List<ModuleMemberDeclarationNode> getModuleMemberDeclarationNodes() throws BallerinaOpenApiException {
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        nodes.add(getClientFunction());
        return nodes;
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return return Syntax tree for the ballerina code.
     * @throws BallerinaOpenApiException When function fail in process.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException, ClientException {
        List<ImportDeclarationNode> importForHttp = getImportDeclarationNodes();
        imports.addAll(importForHttp);
        authConfigGeneratorImp.addAuthRelatedRecords(openAPI);
        List<ModuleMemberDeclarationNode> nodes = getModuleMemberDeclarationNodes();
        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(nodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = syntaxTree.modifyWith(modulePartNode);
        ClientDocCommentGenerator clientDocCommentGenerator = new ClientDocCommentGenerator(syntaxTree, openAPI,
                oasClientConfig.isResourceMode());
        return clientDocCommentGenerator.updateSyntaxTreeWithDocComments();
    }
}
