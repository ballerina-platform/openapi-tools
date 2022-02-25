/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *  command and function to handle common input and outputs.
 */
public class ValidatorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    public static Project getProject(Path servicePath) {
        Project project = null;
        // Load project instance for single ballerina file
        try {
            project = ProjectLoader.loadProject(servicePath);
        } catch (ProjectException e) {
            //ignore
        }
        return project;
    }

//    // Summaries the all the functions
//    public static ResourceMethod getResourceMethod(Project project, String path, String method) {
//        List<FunctionDefinitionNode> functions = new ArrayList<>();
//        Map<String, ResourcePathSummary> resourcePathSummaryMap = new HashMap<>();
//        SyntaxTree syntaxTree;
//        Package packageName = project.currentPackage();
//        DocumentId docId;
//        Document doc;
//        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
//            docId = project.documentId(project.sourceRoot());
//            ModuleId moduleId = docId.moduleId();
//            doc = project.currentPackage().module(moduleId).document(docId);
//        } else {
//            // Take module instance for traversing the syntax tree
//            Module currentModule = packageName.getDefaultModule();
//            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
//
//            docId = documentIterator.next();
//            doc = currentModule.document(docId);
//        }
//        syntaxTree = doc.syntaxTree();
//        ModulePartNode modulePartNode = syntaxTree.rootNode();
//        for (Node node : modulePartNode.members()) {
//            SyntaxKind syntaxKind = node.kind();
//            // Load a listen_declaration for the server part in the yaml spec
//            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
//                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
//                // Check annotation is available
//                Optional<MetadataNode> metadata = serviceDeclarationNode.metadata();
//                MetadataNode openApi = metadata.orElseThrow();
//                if (!openApi.annotations().isEmpty()) {
//                    NodeList<AnnotationNode> annotations = openApi.annotations();
//
//                    //summaries functions
//                    NodeList<Node> members = serviceDeclarationNode.members();
//                    Iterator<Node> iterator = members.iterator();
//                    while (iterator.hasNext()) {
//                        Node next = iterator.next();
//                        if (next instanceof FunctionDefinitionNode) {
//                            functions.add((FunctionDefinitionNode) next);
//                        }
//                    }
//                    // Make resourcePath summery
//                    resourcePathSummaryMap = ResourceWithOperation.summarizeResources(functions);
//                }
//            }
//        }
//        ResourcePathSummary resourcePathSummary = resourcePathSummaryMap.get(path);
//        Map<String, ResourceMethod> methods = resourcePathSummary.getMethods();
//        return methods.get(method);
//    }
//
//    // Take semantic model
//    public static SemanticModel getSemanticModel(Project project) {
//        Package packageName = project.currentPackage();
//        DocumentId docId;
//        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
//            docId = project.documentId(project.sourceRoot());
//            ModuleId moduleId = docId.moduleId();
//        } else {
//            // Take module instance for traversing the syntax tree
//            Module currentModule = packageName.getDefaultModule();
//            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
//            docId = documentIterator.next();
//        }
//        return project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
//    }
//
//    //Take syntax tree
//    public static SyntaxTree getSyntaxTree(Project project) {
//        //Travers and filter service
//        //Take package name for project
//        Package packageName = project.currentPackage();
//        DocumentId docId;
//        Document doc;
//        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
//            docId = project.documentId(project.sourceRoot());
//            ModuleId moduleId = docId.moduleId();
//            doc = project.currentPackage().module(moduleId).document(docId);
//        } else {
//            // Take module instance for traversing the syntax tree
//            Module currentModule = packageName.getDefaultModule();
//            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
//
//            docId = documentIterator.next();
//            doc = currentModule.document(docId);
//        }
//        return doc.syntaxTree();
//    }
//
//    public static ServiceDeclarationNode getServiceDeclarationNode(Project project) {
//        SyntaxTree syntaxTree;
//        Package packageName = project.currentPackage();
//        DocumentId docId;
//        Document doc;
//        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
//            docId = project.documentId(project.sourceRoot());
//            ModuleId moduleId = docId.moduleId();
//            doc = project.currentPackage().module(moduleId).document(docId);
//        } else {
//            // Take module instance for traversing the syntax tree
//            Module currentModule = packageName.getDefaultModule();
//            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
//
//            docId = documentIterator.next();
//            doc = currentModule.document(docId);
//        }
//        syntaxTree = doc.syntaxTree();
//        ModulePartNode modulePartNode = syntaxTree.rootNode();
//        for (Node node : modulePartNode.members()) {
//            SyntaxKind syntaxKind = node.kind();
//            // Load a listen_declaration for the server part in the yaml spec
//            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
//                return (ServiceDeclarationNode) node;
//            }
//        }
//        return null;
//    }
//    public static AnnotationNode getAnnotationNode(ServiceDeclarationNode serviceDeclarationNode) {
//        // Check annotation is available
//        Optional<MetadataNode> metadata = serviceDeclarationNode.metadata();
//        MetadataNode openApi  = metadata.orElseThrow();
//        if (!openApi.annotations().isEmpty()) {
//            NodeList<AnnotationNode> annotations = openApi.annotations();
//            for (AnnotationNode annotationNode : annotations) {
//                return annotationNode;
//            }
//        }
//        return null;
//    }

    public static DiagnosticResult getCompilation(Project project) {
        Package cPackage = project.currentPackage();
        return  cPackage.getCompilation().diagnosticResult();

    }

    public static List<FunctionDefinitionNode> getFunctionDefinitionNodes(ServiceDeclarationNode serviceNode) {

        NodeList<Node> members = serviceNode.members();
        Iterator<Node> iterator = members.iterator();
        List<FunctionDefinitionNode> functions = new ArrayList<>();

        while (iterator.hasNext()) {
            Node next = iterator.next();
            if (next instanceof FunctionDefinitionNode) {
                functions.add((FunctionDefinitionNode) next);
            }
        }
        return functions;
    }
}
