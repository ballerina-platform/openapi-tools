/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

import java.util.Optional;

/**
 * This class can be used to travers syntax tree.
 */
public class OpenAPIVisitor {

    /**
     * This function uses for checking the each ballerina file has openapi annotation.
     * @param syntaxTree syntax tree for ballerina file
     * @return whether annotation is available or not.
     */
    public static boolean isOpenAPIAnnotationAvailable(SyntaxTree syntaxTree) {
        boolean isExist = false;
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a listen_declaration for the server part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                // Check annotation is available
                Optional<MetadataNode> metadata = serviceDeclarationNode.metadata();
                MetadataNode openApi = metadata.orElseThrow();
                if (!openApi.annotations().isEmpty()) {
                    NodeList<AnnotationNode> annotations = openApi.annotations();
                    for (AnnotationNode annotationNode : annotations) {
                        Node annotationRefNode = annotationNode.annotReference();
                        if (annotationRefNode.toString().trim().equals("openapi:ServiceInfo")) {
                            isExist = true;
                        }
                    }
                }
            }
        }
        return isExist;
    }
}
