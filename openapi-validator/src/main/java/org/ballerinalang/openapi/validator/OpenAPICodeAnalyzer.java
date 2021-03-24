package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

/**
 * This class for generate openAPI code analyzer.
 */
public  class OpenAPICodeAnalyzer extends CodeAnalyzer {
    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new ServiceValidator(), SyntaxKind.SERVICE_DECLARATION);
    }
}
