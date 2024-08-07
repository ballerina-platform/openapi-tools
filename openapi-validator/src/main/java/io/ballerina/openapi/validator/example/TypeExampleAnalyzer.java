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
package io.ballerina.openapi.validator.example;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.List;
import java.util.Optional;

/**
 * Ballerina OpenAPI example annotation(s) analyzer for types.
 *
 * @since 2.1.0
 */
public class TypeExampleAnalyzer extends AbstractExampleAnalyzer {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (diagnosticContainsErrors(context)) {
            return;
        }

        Node node = context.node();
        SemanticModel semanticModel = context.semanticModel();
        Optional<Symbol> symbol = semanticModel.symbol(node);

        if (symbol.isEmpty() || !(symbol.get() instanceof TypeDefinitionSymbol typeDefinition)) {
            return;
        }

        List<AnnotationAttachmentSymbol> annotations = typeDefinition.annotAttachments();
        validateExampleAnnotationUsage(context, typeDefinition.typeDescriptor(), annotations, semanticModel);
    }
}
