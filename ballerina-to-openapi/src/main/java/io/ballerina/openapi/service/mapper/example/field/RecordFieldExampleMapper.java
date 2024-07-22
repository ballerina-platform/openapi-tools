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
package io.ballerina.openapi.service.mapper.example.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExampleMapper;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.example.CommonUtils.setExampleForInlineRecordFields;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

/**
 * This {@link RecordFieldExampleMapper} class represents the example mapper for record field.
 *
 * @since 2.1.0
 */
public class RecordFieldExampleMapper extends ExampleMapper {

    String recordName;
    RecordTypeSymbol recordTypeSymbol;
    ObjectSchema schema;
    List<OpenAPIMapperDiagnostic> diagnostics;
    Location location;

    public RecordFieldExampleMapper(String recordName, RecordTypeSymbol recordTypeSymbol, ObjectSchema schema,
                                    SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        super(semanticModel);
        this.recordName = recordName;
        this.recordTypeSymbol = recordTypeSymbol;
        this.schema = schema;
        this.diagnostics = diagnostics;
        this.location = recordTypeSymbol.getLocation().orElse(null);
    }

    @Override
    public void setExample() {
        Map<String, RecordFieldSymbol> recordFields = recordTypeSymbol.fieldDescriptors();
        if (Objects.isNull(recordFields) || recordFields.isEmpty()) {
            return;
        }
        recordFields.forEach((fieldName, fieldSymbol) -> {
            setPropertyExample(fieldName, fieldSymbol);
            setExampleForInlineRecord(fieldName, fieldSymbol);
        });
    }

    private void setPropertyExample(String fieldName, RecordFieldSymbol fieldSymbol) {
        List<AnnotationAttachmentSymbol> annotations = fieldSymbol.annotAttachments();
        try {
            Optional<Object> example = extractExample(annotations);
            if (example.isEmpty()) {
                return;
            }
            schema.getProperties().get(unescapeIdentifier(fieldName.trim())).setExample(example.get());
        } catch (JsonProcessingException exp) {
            diagnostics.add(new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_133, location, recordName,
                    fieldName));
        }
    }

    private void setExampleForInlineRecord(String fieldName, RecordFieldSymbol fieldSymbol) {
        TypeSymbol fieldType = fieldSymbol.typeDescriptor();
        if (Objects.isNull(schema.getProperties())) {
            return;
        }
        Schema fieldSchema = schema.getProperties().get(unescapeIdentifier(fieldName.trim()));
        setExampleForInlineRecordFields(fieldType, fieldSchema, getSemanticModel(), diagnostics);
    }
}
