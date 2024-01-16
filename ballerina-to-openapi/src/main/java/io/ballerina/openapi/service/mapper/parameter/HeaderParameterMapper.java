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
import io.ballerina.openapi.service.mapper.model.OperationBuilder;
import io.ballerina.openapi.service.mapper.type.RecordTypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperInterface;
import io.ballerina.openapi.service.mapper.type.UnionTypeMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.removeStartingSingleQuote;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

public class HeaderParameterMapper extends AbstractParameterMapper {

    private TypeSymbol type = null;
    private String name = null;
    private boolean isRequired = false;
    private String description = null;
    private  boolean treatNilableAsOptional = false;
    private Object defaultValue = null;
    private TypeMapperInterface typeMapper = null;

    public HeaderParameterMapper(ParameterNode parameterNode, Map<String, String> apiDocs,
                                 OperationBuilder operationBuilder, Components components,
                                 boolean treatNilableAsOptional, AdditionalData additionalData) {
        super(operationBuilder);
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
            this.typeMapper = new TypeMapper(components, additionalData);
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
            return new ArrayList<>();
        }

        List<Parameter> headerParameters = getRecordParameterSchema();
        if (Objects.nonNull(headerParameters)) {
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
            TypeMapperInterface.setDefaultValue(typeSchema, defaultValue);
        }
        headerParameter.setSchema(typeSchema);
        headerParameter.setDescription(description);
        return headerParameter;
    }

    public List<Parameter> getRecordParameterSchema() {
        RecordTypeMapper.RecordTypeInfo recordTypeInfo = RecordTypeMapper.getDirectRecordType(type, null);
        if (Objects.isNull(recordTypeInfo)) {
            return null;
        }

        Set<String> requiredHeaders = new HashSet<>();
        HashMap<String, RecordFieldSymbol> headerMap = new HashMap<>(recordTypeInfo.typeSymbol().fieldDescriptors());
        Map<String, Schema> headerSchemaMap = typeMapper.mapRecordFields(headerMap, requiredHeaders,
                recordTypeInfo.name(), treatNilableAsOptional);

        List<Parameter> headerParameters = new ArrayList<>();
        for (Map.Entry<String, Schema> entry : headerSchemaMap.entrySet()) {
            HeaderParameter headerParameter = createHeaderParameter(entry.getKey(), entry.getValue(),
                    requiredHeaders.contains(entry.getKey()), getDefaultValueForHeaderField(entry.getKey()));
            headerParameters.add(headerParameter);
        }
        return headerParameters;
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
            TypeMapperInterface.setDefaultValue(schema, defaultValue);
        }
        headerParameter.setSchema(schema);
        return headerParameter;
    }
}
