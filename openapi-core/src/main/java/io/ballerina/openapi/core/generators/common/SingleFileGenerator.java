/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.openapi.core.generators.common;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.Collection;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;

/**
 * This class combines the syntax tree handling the duplicate imports and returns a single syntax
 * tree used for single file generation.
 *
 * @since 2.1.0
 */
public class SingleFileGenerator {

    public static SyntaxTree combineSyntaxTrees(SyntaxTree ...syntaxTrees) {
        if (syntaxTrees.length == 1) {
            return syntaxTrees[0];
        }
        NodeList<ImportDeclarationNode> importDeclarationNodes = createNodeList();
        NodeList<ModuleMemberDeclarationNode> moduleMemberDeclarationNodes = createNodeList();
        for (SyntaxTree syntaxTree: syntaxTrees) {
            ModulePartNode rootNode = syntaxTree.rootNode();
            NodeList<ImportDeclarationNode> appendingImportDeclarationNodes = rootNode.imports();
            Collection<ImportDeclarationNode> removingImports = new ArrayList<>();
            importDeclarationNodes.stream().forEach(importDecNode -> {
                appendingImportDeclarationNodes.forEach(newImportNode -> {
                    if (importDecNode.toString().equals(newImportNode.toString())) {
                        removingImports.add(newImportNode);
                    }
                });
            });
            importDeclarationNodes = importDeclarationNodes.addAll(appendingImportDeclarationNodes
                    .removeAll(removingImports).stream().toList());
            moduleMemberDeclarationNodes = moduleMemberDeclarationNodes.addAll(rootNode.members().stream().toList());
        }
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(createModulePartNode(importDeclarationNodes, moduleMemberDeclarationNodes,
                createToken(EOF_TOKEN)));
    }
}
