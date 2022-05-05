package io.ballerina.openapi.validator;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

/**
 * This class for generate openAPI code analyzer.
 *
 * @since 2201.1.0
 */
public  class OpenAPICodeAnalyzer extends CodeAnalyzer {
    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new ServiceAnalysisTask(), SyntaxKind.SERVICE_DECLARATION);
    }
}
