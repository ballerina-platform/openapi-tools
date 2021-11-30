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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
        Package currentPackage = context.currentPackage();
        Project project = currentPackage.project();
        // Take output path to target directory location in package.
        Path outPath = project.targetDir();
        Optional<Path> path = currentPackage.project().documentPath(context.documentId());
        Path inputPath = path.orElse(null);
        // Traverse the service declaration nodes
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        List<OASResult> openAPIDefinitions = new ArrayList<>();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a service declarations for the path part in the yaml spec
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                openAPIDefinitions.addAll(ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                        semanticModel, null, false, outPath, inputPath));
            }
        }
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (!openAPIDefinitions.isEmpty()) {
            Map<String,String> yamls  = new HashMap<>();
            for (OASResult oasResult: openAPIDefinitions) {
                if (oasResult.getYaml().isPresent()) {
                    yamls.put(oasResult.getServiceName(), oasResult.getYaml().get());
                    try {
                        //TODO need to check the already given path has
                        writeFile(outPath.resolve(oasResult.getServiceName()), oasResult.getYaml().get());
                    } catch (IOException e) {
                        //
                    }
                }
                if (!oasResult.getDiagnostics().isEmpty()) {
                    for (OpenAPIConverterDiagnostic diagnostic: oasResult.getDiagnostics()) {
                        diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
                    }
                }
            }
        }
        if (!diagnostics.isEmpty()) {
            for (Diagnostic diagnostic : diagnostics) {
                context.reportDiagnostic(diagnostic);
            }
        }
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}