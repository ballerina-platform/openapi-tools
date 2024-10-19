package io.ballerina.openapi.validator.example;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.BALLERINA;
import static io.ballerina.openapi.validator.Constants.EMPTY;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.SERVICE_CONTRACT_TYPE;

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

        boolean fromServiceContract = fromServiceContract(semanticModel, serviceDeclaration);

        serviceDeclaration.methods().values().forEach(method ->
                validateAnnotationOnResources(context, method, fromServiceContract, semanticModel));
    }

    protected void validateAnnotationOnResources(SyntaxNodeAnalysisContext context, MethodSymbol method,
                                                 boolean fromServiceContract, SemanticModel semanticModel) {
        if (method instanceof ResourceMethodSymbol resourceMethodSymbol) {
            if (fromServiceContract) {
                invalidatePathParamExample(context, resourceMethodSymbol, semanticModel);
                invalidateSignatureParamExample(context, resourceMethodSymbol, semanticModel);
            } else {
                validateSignatureParams(context, resourceMethodSymbol, semanticModel);
                validatePathParams(context, resourceMethodSymbol, semanticModel);
            }
        }
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

    private void invalidatePathParamExample(SyntaxNodeAnalysisContext context, ResourceMethodSymbol methodSymbol,
                                            SemanticModel semanticModel) {
        ResourcePath path = methodSymbol.resourcePath();
        if (path instanceof PathRestParam pathRestParam) {
            invalidateBothExampleAnnotations(context, semanticModel, pathRestParam.parameter().annotAttachments());
        } else if (path instanceof PathSegmentList pathSegmentList) {
            pathSegmentList.pathParameters().forEach(pathParameter -> invalidateBothExampleAnnotations(context,
                    semanticModel, pathParameter.annotAttachments()));
            pathSegmentList.pathRestParameter().ifPresent(pathRestParam ->
                    invalidateBothExampleAnnotations(context, semanticModel, pathRestParam.annotAttachments()));
        }
    }

    private void validateSignatureParams(SyntaxNodeAnalysisContext context, ResourceMethodSymbol resourceMethodSymbol,
                                         SemanticModel semanticModel) {
        Optional<List<ParameterSymbol>> params = resourceMethodSymbol.typeDescriptor().params();
        params.ifPresent(parameterSymbols -> parameterSymbols.forEach(parameterSymbol ->
                validateParameterAnnotations(context, parameterSymbol.typeDescriptor(),
                        parameterSymbol.annotAttachments(), semanticModel)));
    }

    private void invalidateSignatureParamExample(SyntaxNodeAnalysisContext context, ResourceMethodSymbol methodSymbol,
                                                 SemanticModel semanticModel) {
        Optional<List<ParameterSymbol>> params = methodSymbol.typeDescriptor().params();
        params.ifPresent(parameterSymbols -> parameterSymbols.forEach(parameterSymbol ->
                invalidateBothExampleAnnotations(context, semanticModel, parameterSymbol.annotAttachments())));
    }

    private void validateParameterAnnotations(SyntaxNodeAnalysisContext context, TypeSymbol typeSymbol,
                                              List<AnnotationAttachmentSymbol> annotations,
                                              SemanticModel semanticModel) {
        validateExampleAnnotationUsage(context, typeSymbol, annotations, semanticModel);
        validateExamplesAnnotationUsage(context, typeSymbol, annotations, semanticModel);
        validateBothExampleAnnotations(context, annotations, semanticModel);
    }

    public static boolean fromServiceContract(SemanticModel semanticModel, ServiceDeclarationSymbol service) {
        Optional<TypeSymbol> serviceTypeSymbol = service.typeDescriptor();

        if (serviceTypeSymbol.isEmpty() ||
                !(serviceTypeSymbol.get() instanceof TypeReferenceTypeSymbol serviceTypeRef)) {
            return false;
        }

        Optional<Symbol> serviceContractType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                SERVICE_CONTRACT_TYPE);
        if (serviceContractType.isEmpty() ||
                !(serviceContractType.get() instanceof TypeDefinitionSymbol serviceContractTypeDef)) {
            return false;
        }

        return serviceTypeRef.subtypeOf(serviceContractTypeDef.typeDescriptor());
    }
}
