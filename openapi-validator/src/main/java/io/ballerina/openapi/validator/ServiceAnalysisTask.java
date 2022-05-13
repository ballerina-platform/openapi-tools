/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;

/**
 * This model used to filter and validate all the operations according to the given filter and filter the service
 * resource in the resource file.
 *
 * @since 1.1.0
 */
public class ServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private final ServiceValidator serviceValidator;
    private final PreValidator preValidator;

    public ServiceAnalysisTask() {
        this.preValidator = new PreValidator();
        this.serviceValidator = new ServiceValidator();
    }

    //create common interface for validators as validate.  openAPI, contex are common.
    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxContext) {
        this.preValidator.initialize(syntaxContext, null, null);
        this.preValidator.validate();
        if (this.preValidator.getOpenAPI() == null) {
            return;
        }
        Filter filter = this.preValidator.getFilter();
        boolean tagEnabled = filter.getTag() != null;
        boolean operationEnabled = filter.getOperation() != null;
        boolean excludeTagsEnabled = filter.getExcludeTag() != null;
        boolean excludeOperationEnable = filter.getExcludeOperation() != null;

        // This is the annotation has not any filters to filter the operations.
        if (tagEnabled && operationEnabled && excludeOperationEnable && excludeTagsEnabled) {
            reportDiagnostic(syntaxContext, CompilationError.FOUR_ANNOTATION_FIELDS,
                    syntaxContext.node().location(), DiagnosticSeverity.ERROR);
            return;
        }

        // 1. When the both tag and e.tags enable compiler gives compilation error.
        if (tagEnabled && excludeTagsEnabled) {
            reportDiagnostic(syntaxContext, CompilationError.BOTH_TAGS_AND_EXCLUDE_TAGS_ENABLES,
                    syntaxContext.node().location(), DiagnosticSeverity.ERROR);
            return;
        }

        // 2. When the both operation and e. operations enable compiler gives compilation error.
        if (operationEnabled && excludeOperationEnable) {
            reportDiagnostic(syntaxContext, CompilationError.BOTH_OPERATIONS_AND_EXCLUDE_OPERATIONS_ENABLES,
                    syntaxContext.node().location(), DiagnosticSeverity.ERROR);
            return;
        }

        this.serviceValidator.initialize(syntaxContext, this.preValidator.getOpenAPI(), filter);
        this.serviceValidator.validate();
    }
}
