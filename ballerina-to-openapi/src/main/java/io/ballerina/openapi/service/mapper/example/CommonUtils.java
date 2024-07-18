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
package io.ballerina.openapi.service.mapper.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.values.ConstantValue;
import io.swagger.v3.oas.models.examples.Example;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI;

/**
 * This {@link CommonUtils} class represents the common utility functions of example mapper.
 *
 * @since 2.1.0
 */
public final class CommonUtils {

    private static final String EXAMPLE_VALUES = "ExampleValues";
    private static final String EXAMPLE_VALUE = "ExampleValue";
    private static final String VALUE = "value";
    private static final String QUOTE = "\"";
    private static final String COMMA = ",";
    private static final String BRACKET_START = "[";
    private static final String BRACKET_END = "]";
    private static final String COLON = ":";
    private static final String BRACE_START = "{";
    private static final String BRACE_END = "}";

    private CommonUtils() {
    }

    public static Optional<Object> extractOpenApiExampleValue(List<AnnotationAttachmentSymbol> annotations,
                                                              SemanticModel semanticModel)
            throws JsonProcessingException {
        Optional<AnnotationAttachmentSymbol> openAPIExample = annotations.stream()
                .filter(annotAttachment -> isOpenAPIExampleAnnotation(annotAttachment, semanticModel))
                .findFirst();
        if (openAPIExample.isEmpty() || !openAPIExample.get().isConstAnnotation() ||
                openAPIExample.get().attachmentValue().isEmpty()) {
            return Optional.empty();
        }

        Object exampleValue = openAPIExample.get().attachmentValue().get().value();
        return getExampleFromValue(exampleValue);
    }

    private static Optional<Object> getExampleFromValue(Object exampleValue) throws JsonProcessingException {
        if (exampleValue instanceof ConstantValue) {
            exampleValue = ((ConstantValue) exampleValue).value();
        } else if (exampleValue instanceof BLangConstantValue) {
            exampleValue = ((BLangConstantValue) exampleValue).value;
        }

        if (!(exampleValue instanceof HashMap<?, ?> exampleMap)) {
            return Optional.empty();
        }

        Object value = exampleMap.get(VALUE);
        if (Objects.isNull(value) || !(value instanceof ConstantValue)) {
            return Optional.empty();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Optional.of(objectMapper.readValue(getJsonString(value), Object.class));
    }

    public static boolean isOpenAPIExampleAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                                     SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, EXAMPLE_VALUE, semanticModel);
    }

    public static boolean hasBothOpenAPIExampleAnnotations(List<AnnotationAttachmentSymbol> annotations,
                                                           SemanticModel semanticModel) {
        return annotations.stream().anyMatch(
                        annotAttachment -> isOpenAPIExampleAnnotation(annotAttachment, semanticModel)) &&
                annotations.stream().anyMatch(
                        annotAttachment -> isOpenAPIExamplesAnnotation(annotAttachment, semanticModel));
    }

    public static Optional<Map<String, Example>> extractOpenApiExampleValues(List<AnnotationAttachmentSymbol>
                                                                                     annotations,
                                                                             SemanticModel semanticModel)
            throws JsonProcessingException {
        Optional<AnnotationAttachmentSymbol> openAPIExamples = annotations.stream()
                .filter(annotAttachment -> isOpenAPIExamplesAnnotation(annotAttachment, semanticModel))
                .findFirst();

        if (openAPIExamples.isEmpty() || !openAPIExamples.get().isConstAnnotation() ||
                openAPIExamples.get().attachmentValue().isEmpty()) {
            return Optional.empty();
        }

        Object exampleValues = openAPIExamples.get().attachmentValue().get().value();
        if (!(exampleValues instanceof HashMap<?, ?> exampleValuesMap)) {
            return Optional.empty();
        }

        Map<String, Example> examplesMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : exampleValuesMap.entrySet()) {
            Optional<Object> exampleValue = getExampleFromValue(entry.getValue());
            if (exampleValue.isEmpty()) {
                continue;
            }
            Example example = new Example();
            example.setValue(exampleValue.get());
            examplesMap.put(entry.getKey().toString(), example);
        }
        return Optional.of(examplesMap);
    }

    public static boolean isOpenAPIExamplesAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                                      SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, EXAMPLE_VALUES, semanticModel);
    }

    private static boolean isOpenAPIAnnotation(AnnotationAttachmentSymbol annotAttachment, String annotationName,
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

    public static String getJsonString(Object value) {
        if (value instanceof ConstantValue constantValue) {
            return getJsonString(constantValue.value());
        } else if (value instanceof BLangConstantValue constantValue) {
            return getJsonString(constantValue.value);
        } else if (value instanceof String stringValue) {
            return QUOTE + stringValue + QUOTE;
        } else if (value instanceof ArrayList<?> objects) {
            return objects.stream()
                    .map(CommonUtils::getJsonString)
                    .collect(Collectors.joining(COMMA, BRACKET_START, BRACKET_END));
        } else if (value instanceof HashMap<?, ?> hashMap) {
            return hashMap.entrySet().stream()
                    .map(entry -> getJsonString(entry.getKey()) + COLON + getJsonString(entry.getValue()))
                    .collect(Collectors.joining(COMMA, BRACE_START, BRACE_END));
        } else {
            return value.toString();
        }
    }
}
