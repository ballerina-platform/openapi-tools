package io.ballerina.openapi.build;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.semanticModel();
        SyntaxTree syntaxTree = context.syntaxTree();
        // Take the output path
        Package currentPackage = context.currentPackage();
        Project project = currentPackage.project();
        Path outPath = project.targetDir();
        Optional<Path> path = currentPackage.project().documentPath(context.documentId());
        Path inputPath = path.orElse(null);
        // Check service node is http:service
        // If listener is http then we can directly map if not we cann't
        // Traverse the service declaration nodes
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a service declarations for the path part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                Optional<MetadataNode> metadata = serviceDeclarationNode.metadata();
                if (metadata.isPresent()) {

                }
            }
        }

        List<OASResult> openAPIDefinitions = ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                semanticModel, null, false, outPath, inputPath);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (!openAPIDefinitions.isEmpty()) {
            Map<String,String> yamls  = new HashMap<>();
            for (OASResult oasResult: openAPIDefinitions) {
                if (oasResult.getYaml().isPresent()) {
                    yamls.put(oasResult.getServiceName(), oasResult.getYaml().get());
                }
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
                context.reportDiagnostic(diagnostic);
            }
        }
    }
}