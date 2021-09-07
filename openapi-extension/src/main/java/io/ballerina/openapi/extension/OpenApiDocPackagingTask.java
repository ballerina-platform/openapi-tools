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

import io.ballerina.openapi.extension.doc.ResourcePackagingService;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleTask;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.openapi.extension.doc.DocGenerationUtils.getDiagnostics;

/**
 * {@code HttpDocGeneratedTask} handles post compile tasks related to OpenAPI doc generation.
 */
public class OpenApiDocPackagingTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {
    private final ResourcePackagingService packagingService;

    public OpenApiDocPackagingTask() {
        this.packagingService = new ResourcePackagingService();
    }

    @Override
    public void perform(CompilerLifecycleEventContext context) {
        Optional<Path> executablePath = context.getGeneratedArtifactPath();
        executablePath.ifPresent(exec ->
                updateResources(exec, context)
        );
    }

    private void updateResources(Path executablePath, CompilerLifecycleEventContext context) {
        try {
            Path executableJarAbsPath = executablePath.toAbsolutePath();
            // get the path for `target/bin`
            Path targetBinPath = executableJarAbsPath.getParent();
            if (null != targetBinPath && Files.exists(targetBinPath)) {
                // do not proceed if `resources` directory is not there
                if (!Files.exists(targetBinPath.resolve(Constants.RESOURCES_DIR_NAME))) {
                    return;
                }

                String executableJarFileName = executableJarAbsPath.toFile().getName();
                try {
                    this.packagingService.updateExecutableJar(targetBinPath, executableJarFileName);
                } catch (IOException e) {
                    OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_104;
                    Diagnostic diagnostic = getDiagnostics(errorCode, new NullLocation(), e.getMessage());
                    context.reportDiagnostic(diagnostic);
                }

                // clean up created intermediate resources
                execCleanup(targetBinPath, context);
            }
        } catch (Exception e) {
            OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_105;
            Diagnostic diagnostic = getDiagnostics(errorCode, new NullLocation(), e.getMessage());
            context.reportDiagnostic(diagnostic);
        }
    }

    private void execCleanup(Path targetPath, CompilerLifecycleEventContext context) {
        Path resourcesDirectory = getResourcesPath(targetPath, context);
        try {
            if (Files.exists(resourcesDirectory)) {
                Files.delete(resourcesDirectory);
            }
        } catch (IOException e) {
            OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_106;
            Diagnostic diagnostic = getDiagnostics(errorCode, new NullLocation(), e.getMessage());
            context.reportDiagnostic(diagnostic);
        }
    }

    private Path getResourcesPath(Path targetPath, CompilerLifecycleEventContext context) {
        ProjectKind projectType = context.currentPackage().project().kind();
        if (ProjectKind.BUILD_PROJECT.equals(projectType)) {
            return targetPath.resolve(Constants.RESOURCES_DIR_NAME);
        }
        return targetPath.resolve(Constants.TARGET_DIR_NAME);
    }

    private static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
