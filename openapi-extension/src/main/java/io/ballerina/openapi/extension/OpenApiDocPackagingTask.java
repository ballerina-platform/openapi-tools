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

import io.ballerina.openapi.extension.context.OpenApiDocContext;
import io.ballerina.openapi.extension.doc.ResourcePackagingService;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.JarResolver;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
import io.ballerina.projects.plugins.CompilerLifecycleTask;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.openapi.extension.context.OpenApiDocContextHandler.getContextHandler;
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
    public void perform(CompilerLifecycleEventContext compilationContext) {
        // if the compilation contains errors or compilation contains warnings related to open-api doc generation
        // do not proceed
        if (isErroneousCompilation(compilationContext)) {
            return;
        }

        // if the shared open-api doc context is not found, do not proceed
//        Package currentPackage = compilationContext.currentPackage();
        Optional<OpenApiDocContext> openApiDocContextOpt = getContextHandler()
                .retrieveContext(null, null);
        if (openApiDocContextOpt.isEmpty()) {
            return;
        }

        compilationContext.getGeneratedArtifactPath()
                .ifPresent(execPath -> {
                    OpenApiDocContext context = openApiDocContextOpt.get();
                    updateResources(context, execPath, compilationContext);
                });
    }

    private boolean isErroneousCompilation(CompilerLifecycleEventContext compilationContext) {
        return compilationContext.compilation().diagnosticResult()
                .diagnostics().stream()
                .anyMatch(d ->
                        DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity())
                                || d.diagnosticInfo().code().startsWith("OPENAPI")
                );
    }

    private void updateResources(OpenApiDocContext context, Path executablePath,
                                 CompilerLifecycleEventContext compilationContext) {
        try {
            Path executableJarAbsPath = executablePath.toAbsolutePath();
            // get the path for `target/bin`
            Path targetBinPath = executableJarAbsPath.getParent();
            if (null != targetBinPath && Files.exists(targetBinPath)) {
                // if generated open-api definitions are empty, do not proceed
                if (context.getOpenApiDetails().isEmpty()) {
                    return;
                }

                String executableJarFileName = executableJarAbsPath.toFile().getName();
                try {
                    // update the executable jar
                    this.packagingService.updateJarFile(targetBinPath, executableJarFileName, context);

                    // update the thin-jar | this is required for dockerized applications
                    Optional<JarLibrary> thinJarOpt = getThinJar(compilationContext, executablePath);
                    if (thinJarOpt.isPresent()) {
                        JarLibrary thinJar = thinJarOpt.get();
                        Path thinJarLocation = thinJar.path().toAbsolutePath();
                        Path parenDirectory = thinJarLocation.getParent();
                        String thinJarName = thinJarLocation.toFile().getName();
                        this.packagingService.updateJarFile(parenDirectory, thinJarName, context);
                    }
                } catch (IOException e) {
                    OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_104;
                    Diagnostic diagnostic = getDiagnostics(errorCode, new NullLocation(), e.getMessage());
                    compilationContext.reportDiagnostic(diagnostic);
                }
            }
        } catch (Exception e) {
            OpenApiDiagnosticCode errorCode = OpenApiDiagnosticCode.OPENAPI_105;
            Diagnostic diagnostic = getDiagnostics(errorCode, new NullLocation(), e.getMessage());
            compilationContext.reportDiagnostic(diagnostic);
        }
    }

    private Optional<JarLibrary> getThinJar(CompilerLifecycleEventContext compilationContext, Path executablePath) {
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend
                .from(compilationContext.compilation(), JvmTarget.JAVA_11);
        JarResolver jarResolver = jBallerinaBackend.jarResolver();

        Package currentPackage = compilationContext.currentPackage();
        String thinJarName = "$anon".equals(currentPackage.packageOrg().value())
                ? executablePath.getFileName().toString()
                : currentPackage.packageOrg().value() + "-" + currentPackage.packageName().value() +
                        "-" + currentPackage.packageVersion().value() + ".jar";

        return jarResolver
                .getJarFilePathsRequiredForExecution().stream()
                .filter(jarLibrary -> jarLibrary.path().getFileName().toString().endsWith(thinJarName))
                .findFirst();
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
