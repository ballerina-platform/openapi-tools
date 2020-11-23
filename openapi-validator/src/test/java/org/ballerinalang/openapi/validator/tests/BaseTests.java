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

package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * This BaseTests class use for main initializing of the tests.
 */
public class BaseTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/").toAbsolutePath();
    private final String dummyContent = "function foo() {\n}";

    @Test (description = "tests for validator on build", enabled = true)
    public void testBuildProject() {
        Path projectPath = RESOURCE_DIRECTORY.resolve("openapiValidator");

        // 1. Initializing the project instance
        BuildProject project = null;
        try {
            project = BuildProject.load(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2. Load the package
        Package currentPackage = project.currentPackage();
        // 3. Load default module
        Module defaultModule = currentPackage.getDefaultModule();
        // 4. Load syntax tree
        PackageName pkgName = PackageName.from("openapiValidator");
        ModuleName moduleName = ModuleName.from(pkgName, "validTest");
        Module module = currentPackage.module(moduleName);
        Iterator<DocumentId> documentIterator = module.documentIds().iterator();
        while (documentIterator.hasNext()) {
            DocumentId docId = documentIterator.next();
            Document doc = module.document(docId);
            SyntaxTree syntaxTree = doc.syntaxTree();
//            syntaxTree.
            System.out.println(syntaxTree);
        }


//        Assert.assertEquals(defaultModule.documentIds().size(), 2);
        // 4. Compile the module
        PackageCompilation compilation = currentPackage.getCompilation();

        Assert.assertEquals(compilation.diagnosticResult().diagnosticCount(), 2);

    }

}
