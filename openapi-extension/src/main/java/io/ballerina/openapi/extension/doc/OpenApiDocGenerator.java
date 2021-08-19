/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.extension.doc;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.openapi.extension.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@code Generator} generates open-api related docs for HTTP service.
 */
public class OpenApiDocGenerator {
    private static final String OPEN_API_DOC_NAME_FORMAT = "%d.json";
    private static final PrintStream ERR = System.err;

    public void generate(Path projectRoot, SemanticModel semanticModel,
                         SyntaxTree syntaxTree, ServiceDeclarationSymbol serviceSymbol,
                         ServiceDeclarationNode serviceNode) {
        try {
            String openApiDocName = String.format(OPEN_API_DOC_NAME_FORMAT, serviceSymbol.hashCode());

            // construct resource directory structure if not already exists
            Path resourcePath = retrieveResourcePath(projectRoot);
            File resourceDirectory = resourcePath.toFile();
            if (!resourceDirectory.exists()) {
                boolean resourceCreatingSuccessful = resourceDirectory.mkdirs();
                if (!resourceCreatingSuccessful) {
                    ERR.println("error [open-api extension]: could not create resources directory");
                    return;
                }
            }

            // generate open-api doc and add it to resource directory
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            List<ListenerDeclarationNode> listenerNodes = extractListenerNodes(modulePartNode);
            String serviceBasePath = getServiceBasePath(serviceNode);
            String openApiDefinition = ServiceToOpenAPIConverterUtils
                    .generateOASDefinition(serviceNode, serviceBasePath, true, listenerNodes, semanticModel);
            if (null != openApiDefinition && !openApiDefinition.isBlank()) {
                CodegenUtils.writeFile(resourcePath.resolve(openApiDocName), openApiDefinition, false);
            }
        } catch (IOException | OpenApiConverterException e) {
            ERR.println("error [open-api extension]: " + e.getLocalizedMessage());
        }
    }

    private List<ListenerDeclarationNode> extractListenerNodes(ModulePartNode modulePartNode) {
        return modulePartNode.members().stream()
                .filter(n -> SyntaxKind.LISTENER_DECLARATION.equals(n.kind()))
                .map(n -> (ListenerDeclarationNode) n)
                .collect(Collectors.toList());
    }

    private String getServiceBasePath(ServiceDeclarationNode serviceDefinition) {
        StringBuilder currentServiceName = new StringBuilder();
        NodeList<Node> serviceNameNodes = serviceDefinition.absoluteResourcePath();
        for (Node serviceBasedPathNode : serviceNameNodes) {
            currentServiceName.append(serviceBasedPathNode.toString());
        }
        return currentServiceName.toString().trim();
    }

    // current design for resources directory structure is as follows :
    //  <executable.jar>
    //      - [resources]
    //          - [ballerina]
    //              - [http]
    private Path retrieveResourcePath(Path projectRoot) {
        return projectRoot
                .resolve(Constants.TARGET_DIR_NAME)
                .resolve(Paths.get(Constants.BIN_DIR_NAME, Constants.RESOURCES_DIR_NAME))
                .resolve(Constants.PACKAGE_ORG).resolve(Constants.PACKAGE_NAME);
    }
}
