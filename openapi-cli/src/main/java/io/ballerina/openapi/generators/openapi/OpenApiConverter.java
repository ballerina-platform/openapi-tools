/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.generators.openapi;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

/**
 * OpenApi related utility classes.
 */

public class OpenApiConverter {
    private  SyntaxTree syntaxTree;
    private  SemanticModel semanticModel;
    private  Project project;

    public OpenApiConverter() {
    }

    /**
     * This util for generating OAS files.
     *
     * @param servicePath The path to a single ballerina file
     * @param outPath     The output directory to which the OpenAPI specifications should be generated to.
     * @param serviceName Filter the services to generate OpenAPI specification for service with this name.
     * @throws IOException               Error when writing the OpenAPI specification file.
     * @throws ProjectException          Error occurred generating OpenAPI specification.
     */
    public void generateOAS3DefinitionsAllService(Path servicePath, Path outPath, String serviceName
            , Boolean needJson) throws IOException, ProjectException, OpenApiConverterException {
        // Load project instance for single ballerina file
        project = ProjectLoader.loadProject(servicePath);
        //Travers and filter service
        //Take package name for project
        Package packageName = project.currentPackage();
        DocumentId docId;
        Document doc;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(servicePath);
            ModuleId moduleId = docId.moduleId();
            doc = project.currentPackage().module(moduleId).document(docId);
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();

            docId = documentIterator.next();
            doc = currentModule.document(docId);
        }
        syntaxTree = doc.syntaxTree();
        semanticModel =  project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
        Map<String, String> openAPIDefinitions =
                ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree, semanticModel, serviceName,
                        needJson,
                outPath);
        if (!openAPIDefinitions.isEmpty()) {
            for (Map.Entry<String, String> definition: openAPIDefinitions.entrySet()) {
                CodegenUtils.writeFile(outPath.resolve(definition.getKey()), definition.getValue());
            }
        }
    }
}
