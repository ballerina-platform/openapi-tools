/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core.service;

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
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
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

/**
 * This class is used to generate the ballerina service object for the given openapi contract.
 */
public class ServiceTypeGenerator extends ServiceGenerator {
    public ServiceTypeGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public SyntaxTree generateSyntaxTree() {
        // TODO: Check the possibility of having imports other than `ballerina/http`
        NodeList<ImportDeclarationNode> imports = ServiceGenerationUtils.createImportDeclarationNodes();
        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(generateServiceObject());
        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private TypeDefinitionNode generateServiceObject() {
        List<Node> serviceObjectMemberNodes = new ArrayList<>();
        TypeReferenceNode httpServiceTypeRefNode = createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                createIdentifierToken("http:Service"), createToken(SEMICOLON_TOKEN));
        serviceObjectMemberNodes.add(httpServiceTypeRefNode);
        List<Node> functions = createResourceFunctions(oasServiceMetadata.getOpenAPI(), oasServiceMetadata.getFilters());
        for (Node functionNode : functions) {
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
                createIdentifierToken(GeneratorConstants.SERVICE_TYPE_NAME), objectTypeDescriptorNode,
                createToken(SEMICOLON_TOKEN));
    }
}
