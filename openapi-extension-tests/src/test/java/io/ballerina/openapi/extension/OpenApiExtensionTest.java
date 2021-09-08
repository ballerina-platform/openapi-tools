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
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        Package currentPackage = loadPackage("sample_1", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithMultipleServices() throws IOException {
        Package currentPackage = loadPackage("sample_2", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithMultipleModules() throws IOException {
        Package currentPackage = loadPackage("sample_3", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForBallerinaProjectWithAnnotation() throws IOException {
        Package currentPackage = loadPackage("sample_4", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForSingleBalFile() {
        Package currentPackage = loadPackage("sample_5/service.bal", true);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForSingleBalFileWithAnnotation() {
        Package currentPackage = loadPackage("sample_6/service.bal", true);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

    @Test
    public void testDocGenerationForHttpLoadBalance() {
        Package currentPackage = loadPackage("sample_7/service.bal", true);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
    }

//    @Test
    public void testInvalidOpenApiContractForBalProject() {
        Package currentPackage = loadPackage("sample_8", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        List<Diagnostic> openApiDiagnostics = compilation.diagnosticResult().diagnostics().stream()
                .filter(d ->
                        Objects.nonNull(d.diagnosticInfo().code())
                                && d.diagnosticInfo().code().startsWith("OPENAPI_10")).collect(Collectors.toList());
        Assert.assertEquals(openApiDiagnostics.size(), 1);
        Diagnostic diagnostic = openApiDiagnostics.get(0);
        DiagnosticInfo info = diagnostic.diagnosticInfo();
        Assert.assertEquals(info.code(), "OPENAPI_101");
        Assert.assertEquals(info.messageFormat(), "could not find the provided contract file");
    }

    private Package loadPackage(String path, boolean isSingleFile) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        if (isSingleFile) {
            return SingleFileProject.load(getEnvironmentBuilder(), projectDirPath).currentPackage();
        }
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private boolean noOpenApiWarningAvailable(PackageCompilation compilation) {
        return compilation.diagnosticResult().diagnostics().stream()
                .filter(d -> DiagnosticSeverity.WARNING.equals(d.diagnosticInfo().severity()))
                .noneMatch(d ->
                        Objects.nonNull(d.diagnosticInfo().code())
                                && d.diagnosticInfo().code().startsWith("OPENAPI_10"));
    }
}
