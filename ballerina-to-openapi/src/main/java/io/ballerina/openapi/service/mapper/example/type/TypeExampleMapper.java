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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.ExampleMapper;
import io.swagger.v3.oas.models.media.Schema;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This {@link TypeExampleMapper} class represents the example mapper for type.
 *
 * @since 2.1.0
 */
public class TypeExampleMapper implements ExampleMapper {

    TypeDefinitionSymbol typeDefinitionSymbol;
    Schema typeSchema;
    SemanticModel semanticModel;
    List<OpenAPIMapperDiagnostic> diagnostics;

    public TypeExampleMapper(TypeDefinitionSymbol typeDefinitionSymbol, Schema typeSchema,
                             SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.typeDefinitionSymbol = typeDefinitionSymbol;
        this.typeSchema = typeSchema;
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    @Override
    public void setExample() {
        Optional<Object> exampleValue = extractExample();
        if (exampleValue.isEmpty() || !(exampleValue.get() instanceof HashMap<?,?> exampleMap)) {
            return;
        }

        Object value = exampleMap.get("value");
        if (Objects.isNull(value) || !(value instanceof ConstantValue)) {
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            typeSchema.setExample(objectMapper.readValue(getJsonString(value), Object.class));
        } catch (JsonProcessingException e) {
            // Add a diagnostic
        }
    }

    public Optional<Object> extractExample() {
        List<AnnotationAttachmentSymbol> annotations = typeDefinitionSymbol.annotAttachments();
        if (Objects.isNull(annotations)) {
            return Optional.empty();
        }

        Optional<AnnotationAttachmentSymbol> openAPIExample = annotations.stream()
                .filter(this::isOpenAPIExampleAnnotation)
                .findFirst();
        if (openAPIExample.isEmpty() || !openAPIExample.get().isConstAnnotation() ||
                openAPIExample.get().attachmentValue().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(openAPIExample.get().attachmentValue().get().value());
    }

    private boolean isOpenAPIExampleAnnotation(AnnotationAttachmentSymbol annotAttachment) {
        return isOpenAPIAnnotation(annotAttachment, "ExampleValue");
    }

    private boolean isOpenAPIAnnotation(AnnotationAttachmentSymbol annotAttachment, String annotationName) {
        if (annotAttachment.typeDescriptor().typeDescriptor().isEmpty()) {
            return false;
        }
        Optional<Symbol> exampleValueSymbol = semanticModel.types().getTypeByName("ballerina", "openapi",
                "", annotationName);
        if (exampleValueSymbol.isEmpty() ||
                !(exampleValueSymbol.get() instanceof TypeDefinitionSymbol serviceContractInfoType)) {
            return false;
        }
        return annotAttachment.typeDescriptor().typeDescriptor().get()
                .subtypeOf(serviceContractInfoType.typeDescriptor());
    }

    private String getJsonString(Object value) {
        if (value instanceof ConstantValue constantValue) {
            return getJsonString(constantValue.value());
        } else if (value instanceof BLangConstantValue constantValue) {
            return getJsonString(constantValue.value);
        } else if (value instanceof String stringValue) {
            return "\"" + stringValue + "\"";
        } else if (value instanceof ArrayList<?> objects) {
            return objects.stream()
                    .map(this::getJsonString)
                    .collect(Collectors.joining(",", "[", "]"));
        } else if (value instanceof HashMap<?, ?> hashMap) {
            return hashMap.entrySet().stream()
                    .map(entry -> getJsonString(entry.getKey()) + ":" + getJsonString(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        } else {
            return value.toString();
        }
    }
}
