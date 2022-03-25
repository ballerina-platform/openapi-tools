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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 *  command and function to handle common input and outputs.
 */
public class ValidatorTest {

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

    @NotNull
    public static Object[] getDiagnostics(DiagnosticResult diagnostic) {
        Object[] errors =
                diagnostic.diagnostics().stream().filter(d -> DiagnosticSeverity.ERROR == d.diagnosticInfo().severity())
                        .toArray();
        return errors;
    }
}
