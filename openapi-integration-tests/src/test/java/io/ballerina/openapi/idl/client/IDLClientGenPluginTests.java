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

package io.ballerina.openapi.idl.client;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Client IDL import integration tests.
 */
public class IDLClientGenPluginTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/client-projects").toAbsolutePath();
    @Test(description = "Provide valid swagger path")
    public void validSwaggerContract() {
        Project project = loadBuildProject(RESOURCE_DIRECTORY.resolve("project_01"));
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
    }

    @Test(description = "When client declaration without annotation")
    public void withOutAnnotation() {

    }

    @Test(description = "Provide client annotation symbol")
    public void withAnnotation() {

    }

    @Test(description = "When client declaration inside the function")
    public void withClientDeclarationNode() {

    }

    @Test(description = "When client declaration in module level the function")
    public void withModuleClientDeclarationNode() {

    }


    public static BuildProject loadBuildProject(Path projectPath) {
        BuildOptions buildOptions = BuildOptions.builder().setOffline(true).build();
        return BuildProject.load(projectPath, buildOptions);
    }
}
