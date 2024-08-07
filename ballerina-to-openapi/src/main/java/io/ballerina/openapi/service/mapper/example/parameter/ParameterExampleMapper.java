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
package io.ballerina.openapi.service.mapper.example.parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExamplesAnnotationMapper;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.EXAMPLE;
import static io.ballerina.openapi.service.mapper.Constants.EXAMPLES;
import static io.ballerina.openapi.service.mapper.example.CommonUtils.setExampleForInlineRecordFields;

/**
 * This {@link ParameterExampleMapper} class represents the examples mapper for resource function parameter.
 *
 * @since 2.1.0
 */
public abstract class ParameterExampleMapper extends ExamplesAnnotationMapper {

    Parameter parameterSchema;
    List<AnnotationAttachmentSymbol> annotations;
    List<OpenAPIMapperDiagnostic> diagnostics;
    String paramName;
    Location location;
    String paramTypeName;
    TypeSymbol paramType;

    ParameterExampleMapper(List<AnnotationAttachmentSymbol> annotations, Parameter parameterSchema,
                           SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics,
                           String paramName, Location location, String paramTypeName, TypeSymbol paramType) {
        super(semanticModel);
        this.parameterSchema = parameterSchema;
        this.annotations = annotations;
        this.diagnostics = diagnostics;
        this.paramName = paramName;
        this.location = location;
        this.paramTypeName = paramTypeName;
        this.paramType = paramType;
    }

    @Override
    public void setExample() {
        try {
            setExampleForInlineRecord();
            Optional<Object> exampleValue = extractExample(annotations);
            if (exampleValue.isEmpty()) {
                return;
            }
            parameterSchema.setExample(exampleValue.get());
        } catch (JsonProcessingException exception) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_134, location, EXAMPLE,
                    paramTypeName, paramName));
        }
    }

    private void setExampleForInlineRecord() {
        Schema schema = parameterSchema.getSchema();
        setExampleForInlineRecordFields(paramType, schema, getSemanticModel(), diagnostics);
    }

    @Override
    public void setExamples() {
        try {
            Optional<Map<String, Example>> exampleValues = extractExamples(annotations);
            if (exampleValues.isEmpty()) {
                return;
            }
            parameterSchema.setExamples(exampleValues.get());
        } catch (JsonProcessingException exception) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_134, location, EXAMPLES,
                    paramTypeName, paramName));
        }
    }
}
