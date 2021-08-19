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

package io.ballerina.openapi.extension;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class includes tests for Ballerina WebSub compiler plugin.
 */
public class OpenApiExtensionTest {
    private static final Path RESOURCE_DIRECTORY = Paths
            .get("src", "test", "resources", "ballerina_sources").toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths
            .get("build", "target", "ballerina-distribution").toAbsolutePath();

    @Test
    public void testDocGenerationForBallerinaProject() throws IOException {
        deleteTarget("sample_1");
        Package currentPackage = loadPackage("sample_1");
        PackageCompilation compilation = currentPackage.getCompilation();
        testGeneratedResources("sample_1", 1);
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithMultipleServices() throws IOException {
        deleteTarget("sample_2");
        Package currentPackage = loadPackage("sample_2");
        PackageCompilation compilation = currentPackage.getCompilation();
        testGeneratedResources("sample_2", 2);
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithMultipleModules() throws IOException {
        deleteTarget("sample_3");
        Package currentPackage = loadPackage("sample_3");
        PackageCompilation compilation = currentPackage.getCompilation();
//        testGeneratedResources("sample_3", 3);
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithAnnotation() throws IOException {
        deleteTarget("sample_4");
        Package currentPackage = loadPackage("sample_4");
        PackageCompilation compilation = currentPackage.getCompilation();
        testGeneratedResources("sample_4", 1);
    }

    private void testGeneratedResources(String projectName, int serviceCount) throws IOException {
        Path resourceRelativePath = Paths.get(projectName, "target", "bin", "resources", "ballerina", "http");
        Path resources = RESOURCE_DIRECTORY.resolve(resourceRelativePath);
        if (Files.isDirectory(resources)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(resources)) {
                List<String> files = new ArrayList<>();
                stream.forEach(p -> files.add(p.toString()));
                Assert.assertEquals(files.size(), serviceCount);
            }
        } else {
            Assert.fail("Resource directory has not been created");
        }
    }

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private void deleteTarget(String projectName) throws IOException {
        Path targetRelativePath = Paths.get(projectName, "target");
        Path target = RESOURCE_DIRECTORY.resolve(targetRelativePath);
        if (Files.isDirectory(target)) {
            deleteDirectory(target);
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
