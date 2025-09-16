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
package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.service.mapper.Constants.JSON_DATA;
import static io.ballerina.openapi.service.mapper.Constants.NAME_CONFIG;
import static io.ballerina.openapi.service.mapper.Constants.VALUE;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getConstantValues;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getNameFromAnnotation;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getRecordFieldTypeDescription;

/**
 * This {@link RecordTypeMapper} class represents the record type mapper.
 * This class provides functionalities for mapping the Ballerina record type to OpenAPI type schema.
 *
 * @since 1.9.0
 */
public class RecordTypeMapper extends AbstractTypeMapper {

    public RecordTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
    }

    @Override
    public Schema getReferenceSchema(Components components) {
        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeSymbol.typeDescriptor();
        return getSchema(recordTypeSymbol, components, name, additionalData).description(description);
    }

    public static Schema getSchema(RecordTypeSymbol typeSymbol, Components components, String recordName,
                                   AdditionalData additionalData) {
        Set<String> fieldsOnlyForRequiredList = new HashSet<>();
        ObjectSchema schema = new ObjectSchema();
        Set<String> requiredFields = new HashSet<>();

        Map<String, RecordFieldSymbol> recordFieldMap = new LinkedHashMap<>(typeSymbol.fieldDescriptors());
        List<Schema> allOfSchemaList = mapIncludedRecords(typeSymbol, components, recordFieldMap, additionalData,
                recordName, fieldsOnlyForRequiredList);
        RecordFieldMappingContext mappingContext = new RecordFieldMappingContext(
                recordFieldMap, components, requiredFields, recordName, false,
                true, additionalData, fieldsOnlyForRequiredList);
        Map<String, Schema> properties = mapRecordFields(mappingContext);

        Optional<TypeSymbol> restFieldType = typeSymbol.restTypeDescriptor();
        if (restFieldType.isPresent()) {
            if (!additionalData.semanticModel().types().JSON.subtypeOf(restFieldType.get())) {
                Schema restFieldSchema = TypeMapperImpl.getTypeSchema(restFieldType.get(), components, additionalData);
                schema.additionalProperties(restFieldSchema);
            }
        } else {
            schema.additionalProperties(false);
        }

        schema.setRequired(requiredFields.stream().toList());
        schema.setProperties(properties);
        if (!allOfSchemaList.isEmpty()) {
            ObjectSchema schemaWithAllOf = new ObjectSchema();
            allOfSchemaList.add(schema);
            schemaWithAllOf.setAllOf(allOfSchemaList);
            return schemaWithAllOf;
        }
        return schema;
    }

    static List<Schema> mapIncludedRecords(RecordTypeSymbol typeSymbol, Components components,
                                           Map<String, RecordFieldSymbol> recordFieldMap,
                                           AdditionalData additionalData, String recordName,
                                           Set<String> fieldsOnlyForRequiredList) {
        List<Schema> allOfSchemaList = new ArrayList<>();
        List<TypeSymbol> typeInclusions = typeSymbol.typeInclusions();
        for (TypeSymbol typeInclusion : typeInclusions) {
            // Type inclusion in a record is a TypeReferenceType and the referred type is a RecordType
            if (typeInclusion.typeKind() == TypeDescKind.TYPE_REFERENCE &&
                    ((TypeReferenceTypeSymbol) typeInclusion).typeDescriptor().typeKind() == TypeDescKind.RECORD) {
                Schema includedRecordSchema = TypeMapperImpl.getTypeSchema(typeInclusion, components, additionalData);
                allOfSchemaList.add(includedRecordSchema);

                RecordTypeSymbol includedRecordTypeSymbol = (RecordTypeSymbol) ((TypeReferenceTypeSymbol) typeInclusion)
                        .typeDescriptor();
                Map<String, RecordFieldSymbol> includedRecordFieldMap = includedRecordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> includedRecordField : includedRecordFieldMap.entrySet()) {
                    if (!recordFieldMap.containsKey(includedRecordField.getKey())) {
                        continue;
                    }
                    RecordFieldSymbol recordFieldSymbol = recordFieldMap.get(includedRecordField.getKey());
                    RecordFieldSymbol includedRecordFieldValue = includedRecordField.getValue();

                    if (!includedRecordFieldValue.typeDescriptor().equals(recordFieldSymbol.typeDescriptor())) {
                        continue;
                    }
                    IncludedFieldContext context = new IncludedFieldContext(recordFieldMap, recordName, typeInclusion,
                            includedRecordField, recordFieldSymbol, includedRecordFieldValue);
                    eliminateRedundantFields(context, additionalData, fieldsOnlyForRequiredList);
                }
            }
        }
        return allOfSchemaList;
    }

    private static void eliminateRedundantFields(IncludedFieldContext context, AdditionalData additionalData,
                                                 Set<String> fieldsOnlyForRequiredList) {
        Map<String, RecordFieldSymbol> recordFieldMap = context.recordFieldMap();
        String recordName = context.recordName();
        TypeSymbol typeInclusion = context.typeInclusion();
        Map.Entry<String, RecordFieldSymbol> includedRecordField = context.includedRecordField();
        RecordFieldSymbol recordFieldSymbol = context.recordFieldSymbol();
        RecordFieldSymbol includedRecordFieldValue = context.includedRecordFieldValue();

        boolean recordHasDefault = recordFieldSymbol.hasDefaultValue();
        boolean includedHasDefault = includedRecordFieldValue.hasDefaultValue();
        boolean hasTypeInclusionName = typeInclusion.getName().isPresent();
        boolean isIncludedOptional = includedRecordFieldValue.isOptional();
        boolean isRecordFieldOptional = recordFieldSymbol.isOptional();
        boolean recordFieldName = recordFieldSymbol.getName().isPresent();

        if (recordHasDefault && includedHasDefault && hasTypeInclusionName) {
            Optional<Object> recordFieldDefaultValueOpt = getRecordFieldDefaultValue(recordName,
                    includedRecordField.getKey(), additionalData.moduleMemberVisitor(),
                    additionalData.semanticModel());

            Optional<Object> includedFieldDefaultValueOpt = getRecordFieldDefaultValue(
                    typeInclusion.getName().get(), includedRecordField.getKey(),
                    additionalData.moduleMemberVisitor(), additionalData.semanticModel());

            /*
              This check the scenarios
              ex:
              type RecA record {|
                  string a = "a";
                  string aa;
              |};
              type RecD record {|
                  *RecA;
                  string a = "aad";
                  int d;
              |};
             */
            boolean defaultsAreEqual = recordFieldDefaultValueOpt.isPresent()
                    && includedFieldDefaultValueOpt.isPresent()
                    && recordFieldDefaultValueOpt.get().toString()
                    .equals(includedFieldDefaultValueOpt.get().toString());

            /*
              This checks the scenario where RecA has `a` defaultable field. In this case, the
              .hasDefaultValue() API returns true for both records, but RecA provides the value of the default.
              ex:
              type RecA record {|
                  string a = "a";
                  string aa;
              |};
              type RecB record {|
                  *RecA;
                  int b;
              |};
             */
            boolean onlyIncludedHasDefault = recordFieldDefaultValueOpt.isEmpty() &&
                    includedFieldDefaultValueOpt.isPresent();

            if (defaultsAreEqual || onlyIncludedHasDefault) {
                recordFieldMap.remove(includedRecordField.getKey());
            }
        } else if (!recordHasDefault && !includedHasDefault) {
            recordFieldMap.remove(includedRecordField.getKey());
        }
        if (!isRecordFieldOptional && isIncludedOptional && !recordHasDefault && recordFieldName) {
            fieldsOnlyForRequiredList.add(MapperCommonUtils.unescapeIdentifier(recordFieldSymbol.getName().get()));
            recordFieldMap.remove(includedRecordField.getKey());
        }
    }

    /**
     * Encapsulates the context of included fields in a record for processing.
     *
     * @param recordFieldMap         A map containing record field symbols.
     * @param recordName             The name of the record being processed.
     * @param typeInclusion          The type symbol representing type inclusions in the record.
     * @param includedRecordField    An entry representing the included record field and its symbol.
     * @param recordFieldSymbol      The symbol of the current record field being processed.
     * @param includedRecordFieldValue The symbol of the field in the included record.
     */
    public record IncludedFieldContext(
            Map<String, RecordFieldSymbol> recordFieldMap,
            String recordName,
            TypeSymbol typeInclusion,
            Map.Entry<String, RecordFieldSymbol> includedRecordField,
            RecordFieldSymbol recordFieldSymbol,
            RecordFieldSymbol includedRecordFieldValue) {
    }

    /**
     * Encapsulates the context needed for mapping record fields to schemas.
     *
     * @param recordFieldMap        A map containing record field symbols.
     * @param components            Components used for managing and storing schemas during mapping.
     * @param requiredFields        A set of field names that are required in the mapped schema.
     * @param recordName            The name of the record being processed.
     * @param treatNilableAsOptional Flag indicating whether nilable fields should be treated as optional.
     * @param inferNameFromJsonData Flag indicating whether field names should be inferred from JSON data.
     * @param additionalData        Additional data required for schema generation and field processing.
     * @param fieldsOnlyForRequiredList A set of fields that should be exclusively marked as required.
     */
    public record RecordFieldMappingContext(
            Map<String, RecordFieldSymbol> recordFieldMap,
            Components components,
            Set<String> requiredFields,
            String recordName,
            boolean treatNilableAsOptional,
            boolean inferNameFromJsonData,
            AdditionalData additionalData,
            Set<String> fieldsOnlyForRequiredList) {
    }

    public static Map<String, Schema> mapRecordFields(RecordFieldMappingContext context) {
        Map<String, RecordFieldSymbol> recordFieldMap = context.recordFieldMap();
        Components components = context.components();
        Set<String> requiredFields = context.requiredFields();
        String recordName = context.recordName();
        boolean treatNilableAsOptional = context.treatNilableAsOptional();
        boolean inferNameFromJsonData = context.inferNameFromJsonData();
        AdditionalData additionalData = context.additionalData();
        Set<String> fieldsOnlyForRequiredList = context.fieldsOnlyForRequiredList();
        Map<String, Schema> properties = new LinkedHashMap<>();

        for (Map.Entry<String, RecordFieldSymbol> recordField : recordFieldMap.entrySet()) {
            RecordFieldSymbol recordFieldSymbol = recordField.getValue();
            String recordFieldName = getRecordFieldName(inferNameFromJsonData, recordField,
                    additionalData.semanticModel());
            if (!recordFieldSymbol.isOptional() && !recordFieldSymbol.hasDefaultValue() &&
                    (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(recordFieldSymbol.typeDescriptor()))) {
                requiredFields.add(recordFieldName);
            }
            if (!fieldsOnlyForRequiredList.isEmpty()) {
                requiredFields.addAll(fieldsOnlyForRequiredList);
            }
            String recordFieldDescription = getRecordFieldTypeDescription(recordFieldSymbol);
            Schema recordFieldSchema = TypeMapperImpl.getTypeSchema(recordFieldSymbol.typeDescriptor(),
                    components, additionalData);
            if (Objects.nonNull(recordFieldDescription) && Objects.nonNull(recordFieldSchema)) {
                recordFieldSchema = recordFieldSchema.description(recordFieldDescription);
            }
            if (recordFieldSymbol.hasDefaultValue()) {
                Optional<Object> recordFieldDefaultValueOpt = getRecordFieldDefaultValue(recordName, recordFieldName,
                        additionalData.moduleMemberVisitor(), additionalData.semanticModel());
                if (recordFieldDefaultValueOpt.isPresent()) {
                    TypeMapper.setDefaultValue(recordFieldSchema, recordFieldDefaultValueOpt.get());
                } else {
                    ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_124,
                            recordFieldName);
                    additionalData.diagnostics().add(error);
                }
            }
            // For never field types, the schema will be null
            if (Objects.nonNull(recordFieldSchema)) {
                properties.put(recordFieldName, recordFieldSchema);
            }
        }
        return properties;
    }

    private static String getRecordFieldName(boolean inferNameFromJsonData,
                                             Map.Entry<String, RecordFieldSymbol> recordFieldEntry,
                                             SemanticModel semanticModel) {
        String defaultName = MapperCommonUtils.unescapeIdentifier(recordFieldEntry.getKey().trim());
        return inferNameFromJsonData ? getNameFromAnnotation(JSON_DATA, NAME_CONFIG, VALUE, semanticModel,
                defaultName, recordFieldEntry.getValue()) : defaultName;
    }

    public static Optional<Object> getRecordFieldDefaultValue(String recordName, String fieldName,
                                                              ModuleMemberVisitor moduleMemberVisitor,
                                                              SemanticModel semanticModel) {
        Optional<TypeDefinitionNode> recordDefNodeOpt = moduleMemberVisitor.getTypeDefinitionNode(recordName);
        if (recordDefNodeOpt.isPresent() &&
                recordDefNodeOpt.get().typeDescriptor() instanceof RecordTypeDescriptorNode recordDefNode) {
            return getRecordFieldDefaultValue(fieldName, recordDefNode, semanticModel);
        }
        return Optional.empty();
    }

    private static Optional<Object> getRecordFieldDefaultValue(String fieldName,
                                                               RecordTypeDescriptorNode recordDefNode,
                                                               SemanticModel semanticModel) {
        NodeList<Node> recordFields = recordDefNode.fields();
        RecordFieldWithDefaultValueNode defaultValueNode = recordFields.stream()
                .filter(field -> field instanceof RecordFieldWithDefaultValueNode)
                .map(field -> (RecordFieldWithDefaultValueNode) field)
                .filter(field -> MapperCommonUtils.unescapeIdentifier(field.fieldName().toString().trim()).
                        equals(fieldName)).findFirst().orElse(null);
        if (Objects.isNull(defaultValueNode)) {
            return Optional.empty();
        }
        ExpressionNode defaultValueExpression = defaultValueNode.expression();
        Optional<Symbol> symbol = semanticModel.symbol(defaultValueExpression);
        Optional<Object> value = getConstantValues(symbol);
        if (value.isPresent()) {
            return value;
        }
        if (MapperCommonUtils.isNotSimpleValueLiteralKind(defaultValueExpression.kind())) {
            return Optional.empty();
        }
        return Optional.of(MapperCommonUtils.parseBalSimpleLiteral(defaultValueExpression.toString().trim()));
    }

    public static RecordTypeInfo getDirectRecordType(TypeSymbol typeSymbol, String recordName) {
        if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
            return new RecordTypeInfo(recordName, (RecordTypeSymbol) typeSymbol);
        } else if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeSymbol referredType = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
            String referredRecordName = MapperCommonUtils.getTypeName(typeSymbol);
            return getDirectRecordType(referredType, referredRecordName);
        } else if (typeSymbol.typeKind() == TypeDescKind.INTERSECTION) {
            // Only readonly record intersection types are supported
            List<TypeSymbol> memberTypeSymbols = ((IntersectionTypeSymbol) typeSymbol).memberTypeDescriptors();
            if (memberTypeSymbols.size() == 2) {
                if (memberTypeSymbols.get(0).typeKind() == TypeDescKind.READONLY) {
                    return getDirectRecordType(memberTypeSymbols.get(1), recordName);
                } else if (memberTypeSymbols.get(1).typeKind() == TypeDescKind.READONLY) {
                    return getDirectRecordType(memberTypeSymbols.get(0), recordName);
                }
            }
        } else if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            // Only nilable record union types are supported
            List<TypeSymbol> memberTypeSymbols = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors();
            if (memberTypeSymbols.size() == 2) {
                if (memberTypeSymbols.get(0).typeKind() == TypeDescKind.NIL) {
                    return getDirectRecordType(memberTypeSymbols.get(1), recordName);
                } else if (memberTypeSymbols.get(1).typeKind() == TypeDescKind.NIL) {
                    return getDirectRecordType(memberTypeSymbols.get(0), recordName);
                }
            }
        }
        return null;
    }

    public record RecordTypeInfo(String name, RecordTypeSymbol typeSymbol) {

    }
}
