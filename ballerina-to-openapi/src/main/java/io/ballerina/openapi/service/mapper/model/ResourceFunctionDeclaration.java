/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * The {@link ResourceFunctionDeclaration} represents the resource function declaration.
 *
 * @since 2.1.0
 */
public class ResourceFunctionDeclaration implements ResourceFunction {
    MethodDeclarationNode resourceFunction;
    FunctionSignatureNode functionSignature;

    public ResourceFunctionDeclaration(MethodDeclarationNode methodDeclarationNode) {
        resourceFunction = new MethodDeclarationNode(methodDeclarationNode.internalNode(),
                methodDeclarationNode.position(), methodDeclarationNode.parent());
        functionSignature = methodDeclarationNode.methodSignature();
    }

    public NodeList<Node> relativeResourcePath() {
        return resourceFunction.relativeResourcePath();
    }

    public String functionName() {
        return resourceFunction.methodName().text().trim();
    }

    public Location location() {
        return resourceFunction.location();
    }

    public Optional<MetadataNode> metadata() {
        return resourceFunction.metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(resourceFunction);
    }

    public FunctionSignatureNode functionSignature() {
        return new FunctionSignatureNode(functionSignature.internalNode(), functionSignature.position(),
                functionSignature.parent());
    }

    public Optional<MetadataNode> parentMetaData() {
        return ((TypeDefinitionNode) resourceFunction.parent().parent()).metadata();
    }
}
