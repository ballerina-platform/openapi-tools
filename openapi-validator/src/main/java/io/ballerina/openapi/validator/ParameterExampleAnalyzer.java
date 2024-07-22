package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.List;
import java.util.Optional;

/**
 * Ballerina OpenAPI example annotation(s) analyzer for parameters.
 *
 * @since 2.1.0
 */
public class ParameterExampleAnalyzer extends AbstractExampleAnalyzer {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (diagnosticContainsErrors(context)) {
            return;
        }

        Node node = context.node();
        SemanticModel semanticModel = context.semanticModel();
        Optional<Symbol> symbol = semanticModel.symbol(node);

        if (symbol.isEmpty() || !(symbol.get() instanceof ServiceDeclarationSymbol serviceDeclaration)) {
            return;
        }

        serviceDeclaration.methods().values().forEach(method -> {
            if (method instanceof ResourceMethodSymbol resourceMethodSymbol) {
                validateSignatureParams(context, resourceMethodSymbol, semanticModel);
                validatePathParams(context, resourceMethodSymbol, semanticModel);
            }
        });
    }

    private void validatePathParams(SyntaxNodeAnalysisContext context, ResourceMethodSymbol resourceMethodSymbol,
                                    SemanticModel semanticModel) {
        ResourcePath path = resourceMethodSymbol.resourcePath();
        if (path instanceof PathRestParam pathRestParam) {
            validateRestParameterAnnotations(context, pathRestParam.parameter().annotAttachments(), semanticModel);
        } else if (path instanceof PathSegmentList pathSegmentList) {
            pathSegmentList.pathParameters().forEach(pathParameter -> validateParameterAnnotations(context,
                    pathParameter.typeDescriptor(), pathParameter.annotAttachments(), semanticModel));
            pathSegmentList.pathRestParameter().ifPresent(pathRestParam ->
                    validateRestParameterAnnotations(context, pathRestParam.annotAttachments(), semanticModel));
        }
    }

    private void validateSignatureParams(SyntaxNodeAnalysisContext context, ResourceMethodSymbol resourceMethodSymbol,
                                         SemanticModel semanticModel) {
        Optional<List<ParameterSymbol>> params = resourceMethodSymbol.typeDescriptor().params();
        params.ifPresent(parameterSymbols -> parameterSymbols.forEach(parameterSymbol ->
                validateParameterAnnotations(context, parameterSymbol.typeDescriptor(),
                        parameterSymbol.annotAttachments(), semanticModel)));
    }

    private void validateParameterAnnotations(SyntaxNodeAnalysisContext context, TypeSymbol typeSymbol,
                                              List<AnnotationAttachmentSymbol> annotations,
                                              SemanticModel semanticModel) {
        validateExampleAnnotationUsage(context, typeSymbol, annotations, semanticModel);
        validateExamplesAnnotationUsage(context, typeSymbol, annotations, semanticModel);
        validateBothExampleAnnotations(context, annotations, semanticModel);
    }
}
