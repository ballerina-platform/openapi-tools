/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.ballerinalang.openapi.validator;

import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiler plugin for ballerina OpenAPI/service validator.
 */
@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class OpenAPIValidatorPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dLog = diagnosticLog;
    }
    @Override
    public void setCompilerContext(CompilerContext context) {
//        this.compilerContext = context;
    }

    @Override
    public List<Diagnostic> codeAnalyze(Project project) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        ServiceValidator serviceValidator = new ServiceValidator();
        try {
            diagnostics = serviceValidator.validateResourceFunctions(project);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OpenApiValidatorException e) {
            e.printStackTrace();
        }
        return diagnostics;
    }

}
