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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * The {@link ServiceNode} represents the interface for service node.
 *
 * @since 2.1.0
 */
public interface ServiceNode {

    enum Kind {
        SERVICE_OBJECT_TYPE,
        SERVICE_DECLARATION
    }

    Optional<MetadataNode> metadata();

    Optional<Symbol> getSymbol(SemanticModel semanticModel);

    Optional<TypeSymbol> typeDescriptor(SemanticModel semanticModel);

    SeparatedNodeList<ExpressionNode> expressions();

    String absoluteResourcePath();

    NodeList<Node> members();

    Kind kind();

    Location location();

    int getServiceId();

    void updateAnnotations(NodeList<AnnotationNode> newAnnotations);

    ModuleMemberDeclarationNode getInternalNode();

    Optional<TypeSymbol> getInterceptorReturnType(SemanticModel semanticModel);
}
