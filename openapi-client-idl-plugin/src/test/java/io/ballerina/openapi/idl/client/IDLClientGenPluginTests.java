package io.ballerina.openapi.idl.client;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * Client IDL test.
 */
public class IDLClientGenPluginTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/client-projects").toAbsolutePath();
    @Test(description = "Provide valid swagger path")
    public void validSwaggerContract() {
//        Project project = TestUtils.loadBuildProject(RESOURCE_DIRECTORY.resolve("project_01"));
//        // Check whether there are any diagnostics
//        IDLClientGeneratorResult idlClientGeneratorResult = project.currentPackage().runIDLGeneratorPlugins();
//        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
    }
}
