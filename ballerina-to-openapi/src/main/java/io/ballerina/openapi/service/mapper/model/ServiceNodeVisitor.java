/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.service.mapper.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;

/**
 * Visitor to get the ServiceDeclarationNode.
 *
 */
public class ServiceNodeVisitor extends NodeVisitor {

    public Map<String, List<Map<String, String>>> resourceMap = new HashMap<>();
//    private String resourceName;
//    private String method;
//    private String operationId;

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        for (Node child : serviceDeclarationNode.children()) {
            if (SyntaxKind.RESOURCE_ACCESSOR_DEFINITION.equals(child.kind())) {
                child.accept(this);
            }
        }
//        System.out.println(resourceMap);
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        for (Node child : functionDefinitionNode.children()) {
            if (SyntaxKind.METADATA.equals(child.kind())) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(MetadataNode metadataNode) {
        metadataNode.children().get(0).accept(this);
    }

    @Override
    public void visit(AnnotationNode annotationNode) {
        for (Node child : annotationNode.children()) {
            if (SyntaxKind.MAPPING_CONSTRUCTOR.equals(child.kind())) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(MappingConstructorExpressionNode mappingConstructorExpressionNode) {
        for (Node child : mappingConstructorExpressionNode.children()) {
            if (SyntaxKind.SPECIFIC_FIELD.equals(child.kind())) {
                child.accept(this);
            }
        }
    }

    @Override
    public void visit(SpecificFieldNode specificFieldNode) {
        if (SyntaxKind.STRING_LITERAL.equals(specificFieldNode.children().get(2).kind())) {
//             resourceName = specificFieldNode.children().get(2).toString().replace("\"","");
        } else {
            specificFieldNode.children().get(2).accept(this);
        }
    }

    @Override
    public void visit(ListConstructorExpressionNode listConstructorExpressionNode) {
//        resourceLink = listConstructorExpressionNode.children().get(1);
//        resourceMap.put(resourceName, resourceLink);
    }
}
