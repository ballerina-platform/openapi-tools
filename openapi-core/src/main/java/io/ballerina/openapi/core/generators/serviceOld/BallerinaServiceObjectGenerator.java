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

package io.ballerina.openapi.core.generators.serviceOld;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;

/**
 * This class is used to generate the ballerina service object for the given openapi contract.
 */
public class BallerinaServiceObjectGenerator {
    private final List<Node> resourceFunctionList;

    public BallerinaServiceObjectGenerator(List<Node> resourceFunctionList) {
        this.resourceFunctionList = resourceFunctionList;
    }

//    public SyntaxTree generateSyntaxTree() {
//        // TODO: Check the possibility of having imports other than `ballerina/http`
//        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
//        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(generateServiceObject());
//        Token eofToken = createIdentifierToken("");
//        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);
//
//        TextDocument textDocument = TextDocuments.from("");
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//        return syntaxTree.modifyWith(modulePartNode);
//    }

//    public TypeDefinitionNode generateServiceObject() {
//        List<Node> serviceObjectMemberNodes = new ArrayList<>();
//        TypeReferenceNode httpServiceTypeRefNode = createTypeReferenceNode(createToken(ASTERISK_TOKEN),
//                createIdentifierToken("http:Service"), createToken(SEMICOLON_TOKEN));
//        serviceObjectMemberNodes.add(httpServiceTypeRefNode);
//        for (Node functionNode : resourceFunctionList) {
//            NodeList<Token> methodQualifierList = createNodeList(createToken(RESOURCE_KEYWORD));
//            if (functionNode instanceof FunctionDefinitionNode &&
//                    ((FunctionDefinitionNode) functionNode).qualifierList()
//                            .stream().anyMatch(token -> Objects.equals(token.text(), RESOURCE_KEYWORD.stringValue()))) {
//                FunctionDefinitionNode resourceFunctionDefinitionNode = (FunctionDefinitionNode) functionNode;
//                MethodDeclarationNode resourceMethodDeclarationNode = createMethodDeclarationNode(
//                        RESOURCE_ACCESSOR_DECLARATION, null,
//                        methodQualifierList,
//                        createToken(FUNCTION_KEYWORD),
//                        resourceFunctionDefinitionNode.functionName(),
//                        resourceFunctionDefinitionNode.relativeResourcePath(),
//                        resourceFunctionDefinitionNode.functionSignature(),
//                        createToken(SEMICOLON_TOKEN));
//                serviceObjectMemberNodes.add(resourceMethodDeclarationNode);
//            }
//        }
//
//        NodeList<Node> members = createNodeList(serviceObjectMemberNodes);
//        NodeList<Token> objectQualifierList = createNodeList(createToken(SERVICE_KEYWORD));
//        ObjectTypeDescriptorNode objectTypeDescriptorNode = createObjectTypeDescriptorNode(
//                objectQualifierList,
//                createToken(SyntaxKind.OBJECT_KEYWORD),
//                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
//                members,
//                createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
//
//        return createTypeDefinitionNode(null, null, createToken(TYPE_KEYWORD),
//                createIdentifierToken(SERVICE_TYPE_NAME), objectTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
//    }
}
