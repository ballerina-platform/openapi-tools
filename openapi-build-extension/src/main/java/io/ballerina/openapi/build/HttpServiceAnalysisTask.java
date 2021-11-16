package io.ballerina.openapi.build;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.service.OASResult;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HttpServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        SemanticModel semanticModel = syntaxNodeAnalysisContext.semanticModel();
        SyntaxTree syntaxTree = syntaxNodeAnalysisContext.syntaxTree();
        // Take the output path
        Package currentPackage = syntaxNodeAnalysisContext.currentPackage();
        Project project = currentPackage.project();
        Path outPath = project.targetDir();
        // Check service node is http:service
        List<OASResult> openAPIDefinitions = ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                semanticModel, null, false, outPath);
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (!openAPIDefinitions.isEmpty()) {
            for (OASResult oasResult: openAPIDefinitions) {
                Optional<OpenAPI> openAPI = oasResult.getOpenAPI();
                if (!oasResult.getDiagnostics().isEmpty()) {
                    for (OpenAPIConverterDiagnostic diagnostic: oasResult.getDiagnostics()) {
                        diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
                    }
                }
            }
        }
        // Create the
        if (!diagnostics.isEmpty()) {
            for (Diagnostic diagnostic : diagnostics) {
                syntaxNodeAnalysisContext.reportDiagnostic(diagnostic);
            }
        }
    }
}
