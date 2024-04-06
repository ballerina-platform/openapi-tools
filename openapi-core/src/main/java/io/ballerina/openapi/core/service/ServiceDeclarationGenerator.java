/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 *
 * @since 1.3.0
 */
public class ServiceDeclarationGenerator extends ServiceGenerator {

    public ServiceDeclarationGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
        GeneratorMetaData.createInstance(oasServiceMetadata.getOpenAPI(), oasServiceMetadata.isNullable());
    }

    @Override
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {

            // Create imports http and openapi
            NodeList<ImportDeclarationNode> imports = ServiceGenerationUtils.createImportDeclarationNodes();
            // Need to Generate Base path
            ListenerGenerator listener = new ListenerGeneratorImpl();
        ListenerDeclarationNode listenerDeclarationNode = null;
        listenerDeclarationNode = listener.getListenerDeclarationNodes(oasServiceMetadata.getOpenAPI().getServers());
        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);

        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
        SeparatedNodeList<ExpressionNode> expressions = createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions = createResourceFunctions(oasServiceMetadata.getOpenAPI(), oasServiceMetadata.getFilters());
        NodeList<Node> members = createNodeList(functions);
        // Create annotation if nullable property is enabled
        // @http:ServiceConfig {
        //     treatNilableAsOptional : false
        //}
        MetadataNode metadataNode = null;
        if (isNullableRequired) {
            metadataNode = ServiceGenerationUtils.generateServiceConfigAnnotation();
        }
        TypeDescriptorNode serviceType = null;
        if (oasServiceMetadata.isServiceTypeRequired()) {
            serviceType = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.SERVICE_TYPE_NAME));
        }
        ServiceDeclarationNode serviceDeclarationNode = createServiceDeclarationNode(
                metadataNode, createEmptyNodeList(), createToken(SyntaxKind.SERVICE_KEYWORD,
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE),
                serviceType, absoluteResourcePath, createToken(SyntaxKind.ON_KEYWORD,
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE), expressions,
                createToken(SyntaxKind.OPEN_BRACE_TOKEN), members, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(
                listenerDeclarationNode, serviceDeclarationNode);

        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private NodeList<Node> createBasePathNodeList(ListenerGenerator listener) {

        if (GeneratorConstants.OAS_PATH_SEPARATOR.equals(listener.getBasePath())) {
            return createNodeList(createIdentifierToken(listener.getBasePath()));
        } else {
            String[] basePathNode = listener.getBasePath().split(GeneratorConstants.OAS_PATH_SEPARATOR);
            List<Node> basePath = Arrays.stream(basePathNode).filter(node -> !node.isBlank())
                    .map(node -> createIdentifierToken(GeneratorConstants.OAS_PATH_SEPARATOR +
                            escapeIdentifier(node))).collect(Collectors.toList());
            return createNodeList(basePath);
        }
    }
}
