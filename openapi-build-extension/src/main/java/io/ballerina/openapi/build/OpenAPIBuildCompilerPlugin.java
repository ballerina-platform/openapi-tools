package io.ballerina.openapi.build;

import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

/**
 * TODO: add doc.
 */
public class OpenAPIBuildCompilerPlugin extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext compilerPluginContext) {
        compilerPluginContext.addCodeAnalyzer(new OpenAPIBuildCodeAnalyzer());
    }
}
