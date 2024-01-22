/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.type.RecordTypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperImpl;
import io.ballerina.openapi.service.mapper.type.UnionTypeMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.removeStartingSingleQuote;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

/**
 * This {@link HeaderParameterMapper} class represents the header parameter mapper.
 * This mapper provides functionalities to map the Ballerina header parameter to OpenAPI header parameter.
 *
 * @since 1.9.0
 */
public class HeaderParameterMapper extends AbstractParameterMapper {

    private TypeSymbol type = null;
    private String name = null;
    private boolean isRequired = false;
    private String description = null;
    private  boolean treatNilableAsOptional = false;
    private Object defaultValue = null;
    private TypeMapper typeMapper = null;

    public HeaderParameterMapper(ParameterNode parameterNode, Map<String, String> apiDocs,
                                 OperationInventory operationInventory, Components components,
                                 boolean treatNilableAsOptional, AdditionalData additionalData) {
        super(operationInventory);
        Symbol parameterSymbol = additionalData.semanticModel().symbol(parameterNode).orElse(null);
        if (Objects.nonNull(parameterSymbol) && (parameterSymbol instanceof ParameterSymbol headerParameter)) {
            this.type = headerParameter.typeDescriptor();
            String paramName = unescapeIdentifier(parameterSymbol.getName().get());
            this.name = getHeaderName(parameterNode, paramName);
            this.isRequired = headerParameter.paramKind().equals(ParameterKind.REQUIRED);
            this.description = apiDocs.get(removeStartingSingleQuote(headerParameter.getName().get()));
            this.treatNilableAsOptional = treatNilableAsOptional;
            if (parameterNode instanceof DefaultableParameterNode defaultableHeaderParam) {
                this.defaultValue = AbstractParameterMapper.getDefaultValue(defaultableHeaderParam);
            }
            this.typeMapper = new TypeMapperImpl(components, additionalData);
        }
    }

    String getHeaderName(ParameterNode parameterNode, String defaultName) {
        NodeList<AnnotationNode> annotations = null;
        if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            annotations = requiredParameterNode.annotations();
        } else if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            annotations = defaultableParameterNode.annotations();
        }
        if (Objects.isNull(annotations) || annotations.isEmpty()) {
            return defaultName;
        }
        for (AnnotationNode annotation : annotations) {
            if (annotation.annotReference().toString().trim().equals("http:Header")) {
                String valueExpression = getNameFromHeaderAnnotation(annotation);
                if (valueExpression != null) {
                    return valueExpression;
                }
            }
        }
        return defaultName;
    }

    private static String getNameFromHeaderAnnotation(AnnotationNode annotation) {
        Optional<MappingConstructorExpressionNode> annotationRecord = annotation.annotValue();
        if (annotationRecord.isPresent()) {
            SeparatedNodeList<MappingFieldNode> fields = annotationRecord.get().fields();
            for (MappingFieldNode field : fields) {
                if (field instanceof SpecificFieldNode specificFieldNode &&
                        specificFieldNode.fieldName().toString().trim().equals("name")) {
                        Optional<ExpressionNode> expressionNode = specificFieldNode.valueExpr();
                        if (expressionNode.isPresent()) {
                            ExpressionNode valueExpression = expressionNode.get();
                            if (valueExpression.kind().equals(STRING_LITERAL)) {
                                return valueExpression.toString().trim().replaceAll("\"", "");
                            }
                        }
                }
            }
        }
        return null;
    }

    public List<Parameter> getParameterSchemaList() {
        if (Objects.isNull(type)) {
            return List.of();
        }

        List<Parameter> headerParameters = getRecordParameterSchema();
        if (!headerParameters.isEmpty()) {
            return headerParameters;
        }

        return List.of(getParameterSchema());
    }

    @Override
    public HeaderParameter getParameterSchema() {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setName(name);
        if (isRequired && (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(type))) {
            headerParameter.setRequired(true);
        }
        Schema typeSchema = typeMapper.getTypeSchema(type);
        if (Objects.nonNull(defaultValue)) {
            TypeMapper.setDefaultValue(typeSchema, defaultValue);
        }
        headerParameter.setSchema(typeSchema);
        headerParameter.setDescription(description);
        return headerParameter;
    }

    public List<Parameter> getRecordParameterSchema() {
        RecordTypeMapper.RecordTypeInfo recordTypeInfo = RecordTypeMapper.getDirectRecordType(type, null);
        if (Objects.isNull(recordTypeInfo)) {
            return List.of();
        }

        Set<String> requiredHeaders = new HashSet<>();
        HashMap<String, RecordFieldSymbol> headerMap = new HashMap<>(recordTypeInfo.typeSymbol().fieldDescriptors());
        Map<String, Schema> headerSchemaMap = typeMapper.getSchemaForRecordFields(headerMap, requiredHeaders,
                recordTypeInfo.name(), treatNilableAsOptional);

        return headerSchemaMap.entrySet().stream().map(
                entry -> createHeaderParameter(entry.getKey(), entry.getValue(),
                        requiredHeaders.contains(entry.getKey()), getDefaultValueForHeaderField(entry.getKey()))
        ).collect(Collectors.toList());
    }

    private Object getDefaultValueForHeaderField(String name) {
        if (Objects.nonNull(defaultValue) && defaultValue instanceof Map defaultableValueMap) {
            return defaultableValueMap.get(name);
        }
        return null;
    }

    private static HeaderParameter createHeaderParameter(String name, Schema schema, boolean isRequired,
                                                         Object defaultValue) {
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setName(name);
        if (isRequired) {
            headerParameter.setRequired(true);
        }
        if (Objects.nonNull(defaultValue)) {
            TypeMapper.setDefaultValue(schema, defaultValue);
        }
        headerParameter.setSchema(schema);
        return headerParameter;
    }
}
