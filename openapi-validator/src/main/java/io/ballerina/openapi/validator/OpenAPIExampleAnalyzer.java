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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

/**
 * Compiler plugin for ballerina OpenAPI example annotation(s) validator.
 *
 * @since 2.1.0
 */
public class OpenAPIExampleAnalyzer extends CodeAnalyzer {

    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new TypeExampleAnalyzer(), SyntaxKind.TYPE_DEFINITION);
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new RecordFieldExampleAnalyzer(), SyntaxKind.RECORD_FIELD);
        codeAnalysisContext.addSyntaxNodeAnalysisTask(new ParameterExampleAnalyzer(), SyntaxKind.SERVICE_DECLARATION);
    }
}
