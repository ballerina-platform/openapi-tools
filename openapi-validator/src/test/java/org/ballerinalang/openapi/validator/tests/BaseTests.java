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

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This BaseTests class use for main initializing of the tests.
 */
public class BaseTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");
    private final String dummyContent = "function foo() {\n}";

    @Test (description = "tests for validator on build", enabled = false)
    public void testBuildProject() {
//        Path projectPath = RESOURCE_DIRECTORY.resolve("openapi_project_api_based_tests").resolve("modules").resolve(
//                "openapi-validator-on");

        Path projectPath = RESOURCE_DIRECTORY.resolve("openapi_project_api_based_tests");

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
//        Module defaultModule = currentPackage.getDefaultModule();

//        Assert.assertEquals(defaultModule.documentIds().size(), 2);
        // 4. Compile the module
        PackageCompilation compilation = currentPackage.getCompilation();

    }

}
