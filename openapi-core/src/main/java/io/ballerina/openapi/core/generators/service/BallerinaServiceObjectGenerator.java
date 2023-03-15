package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_ACCESSOR_DECLARATION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RESOURCE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SERVICE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.SERVICE_TYPE_NAME;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.createImportDeclarationNodes;

/**
 *
 */
public class BallerinaServiceObjectGenerator {
    private final List<Node> resourceFunctionList;

    public BallerinaServiceObjectGenerator(List<Node> resourceFunctionList) {
        this.resourceFunctionList = resourceFunctionList;
    }

    public SyntaxTree generateSyntaxTree() {
        // TODO: Check the possibility of having imports other than `ballerina/http`
        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(generateServiceObject());
        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    public TypeDefinitionNode generateServiceObject() {
        List<Node> serviceObjectMemberNodes = new ArrayList<>();
        TypeReferenceNode httpServiceTypeRefNode = createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                createIdentifierToken("http:Service"), createToken(SEMICOLON_TOKEN));
        serviceObjectMemberNodes.add(httpServiceTypeRefNode);
        for (Node functionNode : resourceFunctionList) {
            NodeList<Token> methodQualifierList = createNodeList(createToken(RESOURCE_KEYWORD));
            if (functionNode instanceof FunctionDefinitionNode &&
                    ((FunctionDefinitionNode) functionNode).qualifierList()
                            .stream().anyMatch(token -> Objects.equals(token.text(), RESOURCE_KEYWORD.stringValue()))) {
                FunctionDefinitionNode resourceFunctionDefinitionNode = (FunctionDefinitionNode) functionNode;
                MethodDeclarationNode resourceMethodDeclarationNode = createMethodDeclarationNode(
                        RESOURCE_ACCESSOR_DECLARATION, null,
                        methodQualifierList,
                        createToken(FUNCTION_KEYWORD),
                        resourceFunctionDefinitionNode.functionName(),
                        resourceFunctionDefinitionNode.relativeResourcePath(),
                        resourceFunctionDefinitionNode.functionSignature(),
                        createToken(SEMICOLON_TOKEN));
                serviceObjectMemberNodes.add(resourceMethodDeclarationNode);
            }
        }

        NodeList<Node> members = createNodeList(serviceObjectMemberNodes);
        NodeList<Token> objectQualifierList = createNodeList(createToken(SERVICE_KEYWORD));
        ObjectTypeDescriptorNode objectTypeDescriptorNode = createObjectTypeDescriptorNode(
                objectQualifierList,
                createToken(SyntaxKind.OBJECT_KEYWORD),
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                members,
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        return createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD),
                createIdentifierToken(SERVICE_TYPE_NAME), objectTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }
}
