//package io.ballerina.openapi.build;
//
//import io.ballerina.compiler.api.SemanticModel;
//import io.ballerina.compiler.syntax.tree.SyntaxTree;
//import io.ballerina.openapi.converter.service.OASResult;
//import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
//import io.ballerina.projects.Package;
//import io.ballerina.projects.Project;
//import io.ballerina.projects.plugins.CompilerLifecycleEventContext;
//import io.ballerina.projects.plugins.CompilerLifecycleTask;
//import io.ballerina.tools.diagnostics.Diagnostic;
//
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//
//public class OpenAPIContractGenerationTask implements CompilerLifecycleTask<CompilerLifecycleEventContext> {
//
//    @Override
//    public void perform(CompilerLifecycleEventContext compilerLifecycleEventContext) {
//        //How to handle multiple service
//        SemanticModel semanticModel = compilerLifecycleEventContext.semanticModel();
//        SyntaxTree syntaxTree = syntaxNodeAnalysisContext.syntaxTree();
//        // Take the output path
//        Package currentPackage = syntaxNodeAnalysisContext.currentPackage();
//        Project project = currentPackage.project();
//        Path outPath = project.targetDir();
//        // Check service node is http:service
//        List<OASResult> openAPIDefinitions = ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
//                semanticModel, null, false, outPath);
//        List<Diagnostic> diagnostics = new ArrayList<>();
//
//    }
//}
