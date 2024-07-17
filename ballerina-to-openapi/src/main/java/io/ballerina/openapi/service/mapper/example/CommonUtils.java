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
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This {@link CommonUtils} class represents the common utility functions of example mapper.
 *
 * @since 2.1.0
 */
public final class CommonUtils {

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
        if (!(exampleValue instanceof HashMap<?,?> exampleMap)) {
            return Optional.empty();
        }

        Object value = exampleMap.get("value");
        if (Objects.isNull(value) || !(value instanceof ConstantValue)) {
            return Optional.empty();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return Optional.of(objectMapper.readValue(getJsonString(value), Object.class));
    }

    public static boolean isOpenAPIExampleAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                                     SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, "ExampleValue", semanticModel);
    }

    public static boolean isOpenAPIExamplesAnnotation(AnnotationAttachmentSymbol annotAttachment,
                                                      SemanticModel semanticModel) {
        return isOpenAPIAnnotation(annotAttachment, "ExampleValues", semanticModel);
    }

    private static boolean isOpenAPIAnnotation(AnnotationAttachmentSymbol annotAttachment, String annotationName,
                                               SemanticModel semanticModel) {
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

    public static String getJsonString(Object value) {
        if (value instanceof ConstantValue constantValue) {
            return getJsonString(constantValue.value());
        } else if (value instanceof BLangConstantValue constantValue) {
            return getJsonString(constantValue.value);
        } else if (value instanceof String stringValue) {
            return "\"" + stringValue + "\"";
        } else if (value instanceof ArrayList<?> objects) {
            return objects.stream()
                    .map(CommonUtils::getJsonString)
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
