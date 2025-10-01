/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

/**
 * Exception type definition for OpenAPI CLI related errors.
 *
 * @since 2.4.0
 */
public class OpenAPICmdToolException extends RuntimeException {

    private final String diagnosticMessage;
    private final String diagnosticCode;

    public OpenAPICmdToolException(OpenAPICmdToolDiagnostic diagnostic, String... args) {
        super("ERROR: " + String.format(diagnostic.getMessage(), (Object[]) args));
        this.diagnosticCode = diagnostic.getCode();
        this.diagnosticMessage = String.format(diagnostic.getMessage(), (Object[]) args);
    }

    public OpenAPICmdToolException(Throwable e, OpenAPICmdToolDiagnostic diagnostic, String... args) {
        super(String.format(diagnostic.getMessage(), (Object[]) args), e);
        this.diagnosticCode = diagnostic.getCode();
        this.diagnosticMessage = String.format(diagnostic.getMessage(), (Object[]) args);
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
        }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }
}
