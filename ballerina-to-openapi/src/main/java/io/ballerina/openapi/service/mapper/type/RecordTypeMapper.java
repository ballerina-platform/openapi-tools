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
package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
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
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
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

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getRecordFieldTypeDescription;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getTypeName;

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
        ObjectSchema schema = new ObjectSchema();
        Set<String> requiredFields = new HashSet<>();

        Map<String, RecordFieldSymbol> recordFieldMap = new LinkedHashMap<>(typeSymbol.fieldDescriptors());
        List<Schema> allOfSchemaList = mapIncludedRecords(typeSymbol, components, recordFieldMap, additionalData);

        Map<String, Schema> properties = mapRecordFields(recordFieldMap, components, requiredFields,
                recordName, false, additionalData);

        Optional<TypeSymbol> restFieldType = typeSymbol.restTypeDescriptor();
        if (restFieldType.isPresent()) {
            if (!additionalData.semanticModel().types().JSON.subtypeOf(restFieldType.get())) {
                Schema restFieldSchema = TypeMapperImpl.getTypeSchema(restFieldType.get(), components, additionalData);
                schema.additionalProperties(restFieldSchema);
            }
        } else {
            schema.additionalProperties(false);
        }

        schema.setProperties(properties);
        schema.setRequired(requiredFields.stream().toList());
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
                                           AdditionalData additionalData) {
        List<Schema> allOfSchemaList = new ArrayList<>();
        List<TypeSymbol> typeInclusions = typeSymbol.typeInclusions();
        for (TypeSymbol typeInclusion : typeInclusions) {
            // Type inclusion in a record is a TypeReferenceType and the referred type is a RecordType
            if (typeInclusion.typeKind() == TypeDescKind.TYPE_REFERENCE &&
                    ((TypeReferenceTypeSymbol) typeInclusion).typeDescriptor().typeKind() == TypeDescKind.RECORD) {
                Schema includedRecordSchema = new Schema();
                includedRecordSchema.set$ref(getTypeName(typeInclusion));
                allOfSchemaList.add(includedRecordSchema);
                TypeMapperImpl.createComponentMapping((TypeReferenceTypeSymbol) typeInclusion,
                        components, additionalData);

                RecordTypeSymbol includedRecordTypeSymbol = (RecordTypeSymbol) ((TypeReferenceTypeSymbol) typeInclusion)
                        .typeDescriptor();
                Map<String, RecordFieldSymbol> includedRecordFieldMap = includedRecordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> includedRecordField : includedRecordFieldMap.entrySet()) {
                    recordFieldMap.remove(includedRecordField.getKey());
                }
            }
        }
        return allOfSchemaList;
    }

    public static Map<String, Schema> mapRecordFields(Map<String, RecordFieldSymbol> recordFieldMap,
                                                      Components components, Set<String> requiredFields,
                                                      String recordName, boolean treatNilableAsOptional,
                                                      AdditionalData additionalData) {
        Map<String, Schema> properties = new LinkedHashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> recordField : recordFieldMap.entrySet()) {
            RecordFieldSymbol recordFieldSymbol = recordField.getValue();
            String recordFieldName = MapperCommonUtils.unescapeIdentifier(recordField.getKey().trim());
            if (!recordFieldSymbol.isOptional() && !recordFieldSymbol.hasDefaultValue() &&
                    (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(recordFieldSymbol.typeDescriptor()))) {
                requiredFields.add(recordFieldName);
            }
            String recordFieldDescription = getRecordFieldTypeDescription(recordFieldSymbol);
            Schema recordFieldSchema = TypeMapperImpl.getTypeSchema(recordFieldSymbol.typeDescriptor(),
                    components, additionalData);
            if (Objects.nonNull(recordFieldDescription) && Objects.nonNull(recordFieldSchema)) {
                recordFieldSchema = recordFieldSchema.description(recordFieldDescription);
            }
            if (recordFieldSymbol.hasDefaultValue()) {
                Object recordFieldDefaultValue = getRecordFieldDefaultValue(recordName, recordFieldName,
                        additionalData.moduleMemberVisitor());
                if (Objects.nonNull(recordFieldDefaultValue)) {
                    TypeMapper.setDefaultValue(recordFieldSchema, recordFieldDefaultValue);
                } else {
                    DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_124;
                    IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(message,
                            null, recordFieldName);
                    additionalData.diagnostics().add(error);
                }
            }
            properties.put(recordFieldName, recordFieldSchema);
        }
        return properties;
    }

    public static Object getRecordFieldDefaultValue(String recordName, String fieldName,
                                                    ModuleMemberVisitor moduleMemberVisitor) {
        TypeDefinitionNode recordDefNode = moduleMemberVisitor.getTypeDefinitionNode(recordName);
        if (Objects.isNull(recordDefNode) || !(recordDefNode.typeDescriptor() instanceof RecordTypeDescriptorNode)) {
            return null;
        }
        return getRecordFieldDefaultValue(fieldName, (RecordTypeDescriptorNode) recordDefNode.typeDescriptor());
    }

    private static Object getRecordFieldDefaultValue(String fieldName, RecordTypeDescriptorNode recordDefNode) {
        NodeList<Node> recordFields = recordDefNode.fields();
        RecordFieldWithDefaultValueNode defaultValueNode = recordFields.stream()
                .filter(field -> field instanceof RecordFieldWithDefaultValueNode)
                .map(field -> (RecordFieldWithDefaultValueNode) field)
                .filter(field -> field.fieldName().toString().trim().equals(fieldName)).findFirst().orElse(null);
        if (Objects.isNull(defaultValueNode)) {
            return null;
        }
        ExpressionNode defaultValueExpression = defaultValueNode.expression();
        if (MapperCommonUtils.isNotSimpleValueLiteralKind(defaultValueExpression.kind())) {
            return null;
        }
        return MapperCommonUtils.parseBalSimpleLiteral(defaultValueExpression.toString().trim());
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
