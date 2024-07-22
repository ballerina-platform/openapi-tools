/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.BALLERINA;
import static io.ballerina.openapi.validator.Constants.EMPTY;
import static io.ballerina.openapi.validator.Constants.EXAMPLE_VALUE;
import static io.ballerina.openapi.validator.Constants.EXAMPLE_VALUES;
import static io.ballerina.openapi.validator.Constants.OPENAPI;
import static io.ballerina.openapi.validator.diagnostic.OpenAPIDiagnosticCodes.OPENAPI_100;
import static io.ballerina.openapi.validator.diagnostic.OpenAPIDiagnosticCodes.OPENAPI_101;
import static io.ballerina.openapi.validator.diagnostic.OpenAPIDiagnosticCodes.OPENAPI_102;
import static io.ballerina.openapi.validator.diagnostic.OpenAPIDiagnosticCodes.OPENAPI_103;

/**
 * Abstract class for example analyzers.
 *
 * @since 2.1.0
 */
public abstract class AbstractExampleAnalyzer implements AnalysisTask<SyntaxNodeAnalysisContext> {
    void validateExampleAnnotationUsage(SyntaxNodeAnalysisContext context, TypeSymbol typeSymbol,
                                               List<AnnotationAttachmentSymbol> annotations,
                                               SemanticModel semanticModel) {
        Optional<AnnotationAttachmentSymbol> example = getOpenAPIExampleAnnotation(annotations, semanticModel);
        if (example.isEmpty() || !example.get().isConstAnnotation() ||
                example.get().attachmentValue().isEmpty()) {
            return;
        }

        if (!typeSymbol.subtypeOf(semanticModel.types().ANYDATA)) {
            context.reportDiagnostic(OPENAPI_100.getDiagnosticCode(example.get().getLocation().orElse(null)));
        }
    }

    private Optional<AnnotationAttachmentSymbol> getOpenAPIExampleAnnotation(
            List<AnnotationAttachmentSymbol> annotations, SemanticModel semanticModel) {
        return annotations.stream()
                .filter(annotAttachment -> isOpenAPIExampleAnnotation(annotAttachment, semanticModel))
                .findFirst();
    }

    void validateExamplesAnnotationUsage(SyntaxNodeAnalysisContext context, TypeSymbol typeSymbol,
                                         List<AnnotationAttachmentSymbol> annotations,
                                         SemanticModel semanticModel) {
        Optional<AnnotationAttachmentSymbol> examples = getOpenAPIExamplesAnnotation(annotations, semanticModel);
        if (examples.isEmpty() || !examples.get().isConstAnnotation() ||
                examples.get().attachmentValue().isEmpty()) {
            return;
        }

        if (!typeSymbol.subtypeOf(semanticModel.types().ANYDATA)) {
            context.reportDiagnostic(OPENAPI_101.getDiagnosticCode(examples.get().getLocation().orElse(null)));
        }
    }

    private Optional<AnnotationAttachmentSymbol> getOpenAPIExamplesAnnotation(
            List<AnnotationAttachmentSymbol> annotations, SemanticModel semanticModel) {
        return annotations.stream()
                .filter(annotAttachment -> isOpenAPIExamplesAnnotation(annotAttachment, semanticModel))
                .findFirst();
    }

    void validateBothExampleAnnotations(SyntaxNodeAnalysisContext context,
                                        List<AnnotationAttachmentSymbol> annotations, SemanticModel semanticModel) {
        if (hasBothOpenAPIExampleAnnotations(annotations, semanticModel)) {
            Optional<AnnotationAttachmentSymbol> openAPIExamples = annotations.stream()
                    .filter(annotAttachment -> isOpenAPIExamplesAnnotation(annotAttachment, semanticModel))
                    .findFirst();
            context.reportDiagnostic(OPENAPI_102.getDiagnosticCode(openAPIExamples.get().getLocation().orElse(null)));
        }
    }

    boolean hasBothOpenAPIExampleAnnotations(List<AnnotationAttachmentSymbol> annotations,
                                             SemanticModel semanticModel) {
        return annotations.stream().anyMatch(
                annotAttachment -> isOpenAPIExampleAnnotation(annotAttachment, semanticModel)) &&
                annotations.stream().anyMatch(
                        annotAttachment -> isOpenAPIExamplesAnnotation(annotAttachment, semanticModel));
    }

    boolean diagnosticContainsErrors(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        List<Diagnostic> diagnostics = syntaxNodeAnalysisContext.semanticModel().diagnostics();
        return diagnostics.stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
    }

    void validateRestParameterAnnotations(SyntaxNodeAnalysisContext context,
                                          List<AnnotationAttachmentSymbol> annotations, SemanticModel semanticModel) {
        getOpenAPIExampleAnnotation(annotations, semanticModel).ifPresent(annotAttachment ->
                reportUnsupportedAnnotationForRestParam(context, annotAttachment.getLocation().orElse(null)));
        getOpenAPIExamplesAnnotation(annotations, semanticModel).ifPresent(annotAttachment ->
                reportUnsupportedAnnotationForRestParam(context, annotAttachment.getLocation().orElse(null)));
    }

    private void reportUnsupportedAnnotationForRestParam(SyntaxNodeAnalysisContext context, Location location) {
        context.reportDiagnostic(OPENAPI_103.getDiagnosticCode(location));
    }

    private boolean isOpenAPIExampleAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                               SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, EXAMPLE_VALUE, semanticModel);
    }

    private boolean isOpenAPIExamplesAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                                SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, EXAMPLE_VALUES, semanticModel);
    }

    private boolean isOpenAPIAnnotation(AnnotationAttachmentSymbol annotAttachment, String annotationName,
                                        SemanticModel semanticModel) {
        if (annotAttachment.typeDescriptor().typeDescriptor().isEmpty()) {
            return false;
        }
        Optional<Symbol> exampleValueSymbol = semanticModel.types().getTypeByName(BALLERINA, OPENAPI, EMPTY,
                annotationName);
        if (exampleValueSymbol.isEmpty() ||
                !(exampleValueSymbol.get() instanceof TypeDefinitionSymbol serviceContractInfoType)) {
            return false;
        }
        return annotAttachment.typeDescriptor().typeDescriptor().get()
                .subtypeOf(serviceContractInfoType.typeDescriptor());
    }
}
