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
package io.ballerina.openapi.service.mapper.example.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExampleAnnotationMapper;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.example.CommonUtils.setExampleForInlineRecordFields;

/**
 * This {@link TypeExampleMapper} class represents the example mapper for type.
 *
 * @since 2.1.0
 */
public class TypeExampleMapper extends ExampleAnnotationMapper {

    List<AnnotationAttachmentSymbol> annotations;
    Schema typeSchema;
    List<OpenAPIMapperDiagnostic> diagnostics;
    String typeName;
    Location location;
    TypeSymbol typeSymbol;

    public TypeExampleMapper(TypeDefinitionSymbol typeDefinitionSymbol, Schema typeSchema,
                             SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        super(semanticModel);
        this.annotations = typeDefinitionSymbol.annotAttachments();
        this.typeSymbol = typeDefinitionSymbol.typeDescriptor();
        this.typeSchema = typeSchema;
        this.diagnostics = diagnostics;
        this.typeName = typeDefinitionSymbol.getName().get();
        this.location = typeDefinitionSymbol.getLocation().orElse(null);
    }

    @Override
    public void setExample() {
        try {
            setExampleForInlineRecordFields(typeSymbol, typeSchema, getSemanticModel(), diagnostics);
            Optional<Object> exampleValue = extractExample(annotations);
            if (exampleValue.isEmpty()) {
                return;
            }
            typeSchema.setExample(exampleValue.get());
        } catch (JsonProcessingException exception) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_132, location, typeName));
        }
    }
}
