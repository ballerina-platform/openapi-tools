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

public class OpenAPIVisitor {

    public static boolean isOpenAPIAnnotationAvailable(SyntaxTree syntaxTree) {
        boolean isExist= false;
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
