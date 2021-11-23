//package io.ballerina.openapi.build;
//
//import io.ballerina.compiler.api.SemanticModel;
//import io.ballerina.compiler.syntax.tree.SyntaxTree;
//import io.ballerina.openapi.converter.service.OASResult;
//import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
//import io.ballerina.projects.Document;
//import io.ballerina.projects.DocumentId;
//import io.ballerina.projects.Module;
//import io.ballerina.projects.ModuleId;
//import io.ballerina.projects.Package;
//import io.ballerina.projects.PackageCompilation;
//import io.ballerina.projects.PackageId;
//import io.ballerina.projects.PackageResolution;
//import io.ballerina.projects.Project;
//import io.ballerina.projects.ProjectKind;
//import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
//import io.ballerina.projects.plugins.CompilerLifecycleTask;
//import io.ballerina.tools.diagnostics.Diagnostic;
//
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Optional;
//
//public class OpenAPIContractGenerationTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {
//
//    @Override
//    public void perform(CompilerLifecycleEventContext context) {
////        PackageCompilation compilation = context.compilation();
////        Package currentPackage = context.currentPackage();
////        Optional<Path> outputPath = context.getGeneratedArtifactPath();
////        PackageId packageId = currentPackage.packageId();
////        Module module = currentPackage.getDefaultModule();
////        Project project = currentPackage.project();
////
////        for (DocumentId documentId: module.documentIds()) {
////            Document document = module.document(documentId);
////            Optional<Path> path = module.project().documentPath(documentId);
////            Path inputPath = path.orElse(null);
////            SyntaxTree syntaxTree = document.syntaxTree();
////        }
////
////        if (project.kind() == ProjectKind.BUILD_PROJECT) {
////            // output path to target folder
////        } else if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
////           // output path to exist folder
////        }
//    }
//}
