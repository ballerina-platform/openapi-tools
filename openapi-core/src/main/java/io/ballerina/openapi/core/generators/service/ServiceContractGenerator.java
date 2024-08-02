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
package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * This class is used to generate the ballerina service contract object for the given openapi contract.
 */
public class ServiceContractGenerator extends ServiceTypeGenerator {

    public ServiceContractGenerator(OASServiceMetadata oasServiceMetadata, List<Node> functionsList) {
        super(oasServiceMetadata, functionsList);
    }

    @Override
    protected String getServiceTypeName() {
        return "http:ServiceContract";
    }

    @Override
    protected MetadataNode getServiceMetadata() {
        //create service config annotation
        ListenerGeneratorImpl listenerGenerator = new ListenerGeneratorImpl();
        listenerGenerator.getListenerDeclarationNodes(oasServiceMetadata.getOpenAPI().getServers());
        AnnotationNode annotationNode = createServiceConfigAnnotationNode(
                "basePath:\"" + listenerGenerator.getBasePath() + "\"");
        return createMetadataNode(null, createNodeList(annotationNode));
    }

    /**
     * This util create any annotation node by providing annotation reference and annotation body content.
     *
     * @param annotFields Annotation body content fields with single string
     * @return {@link AnnotationNode}
     */
    private static AnnotationNode createServiceConfigAnnotationNode(String annotFields) {
        String annotBody = GeneratorConstants.OPEN_BRACE + String.join(GeneratorConstants.COMMA, annotFields) +
                GeneratorConstants.CLOSE_BRACE;

        MappingConstructorExpressionNode annotationBody = null;
        SimpleNameReferenceNode annotReference = createSimpleNameReferenceNode(
                createIdentifierToken("http:ServiceConfig"));
        ExpressionNode expressionNode = NodeParser.parseExpression(annotBody);
        if (expressionNode.kind() == SyntaxKind.MAPPING_CONSTRUCTOR) {
            annotationBody = (MappingConstructorExpressionNode) expressionNode;
        }
        return NodeFactory.createAnnotationNode(
                createToken(SyntaxKind.AT_TOKEN),
                annotReference,
                annotationBody);
    }
}
