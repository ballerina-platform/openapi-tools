package io.ballerina.openapi.generators.openapi;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class OpenAPIInfoTests {
    private static final Path RESOURCE_DIRECTORY = Paths
            .get("src", "test", "resources", "ballerina_sources").toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths
            .get("build", "target", "ballerina-distribution").toAbsolutePath();

    @Test
    public void testDocGenerationForBallerinaProject() throws IOException {
        Package currentPackage = loadPackage("project_1", false);
        PackageCompilation compilation = currentPackage.getCompilation();
        Assert.assertTrue(noOpenApiWarningAvailable(compilation));
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
