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
package org.ballerinalang.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

//import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;

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

    // Take semantic model
    public static SemanticModel getSemanticModel(Project project) {
        Package packageName = project.currentPackage();
        DocumentId docId;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(project.sourceRoot());
            ModuleId moduleId = docId.moduleId();
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
            docId = documentIterator.next();
        }
        return project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
    }

    //Take syntax tree
    public static SyntaxTree getSyntaxTree(Project project) {
        //Travers and filter service
        //Take package name for project
        Package packageName = project.currentPackage();
        DocumentId docId;
        Document doc;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(project.sourceRoot());
            ModuleId moduleId = docId.moduleId();
            doc = project.currentPackage().module(moduleId).document(docId);
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();

            docId = documentIterator.next();
            doc = currentModule.document(docId);
        }
        return doc.syntaxTree();
    }
}
