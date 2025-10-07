/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.core.generators.common;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.core.generators.common.diagnostic.CommonDiagnostic;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.core.generators.common.diagnostic.CommonDiagnosticMessages.OAS_COMMON_102;

/**
 * This class is responsible for fixing language level limitations related to record fields.
 * <p>
 *     1. When a same field is included by more than one included record, the field should be resolved in the final
 *        record. If the field types are different, a diagnostic is reported and the first field is taken.
 *     2. If a record includes another record that has fields without any metadata (annotations), those fields are
 *        included again since included record does not copy the metadata of the fields.
 * </p>
 * @since 2.4.0
 */
public class TypeFixer {

    private final Map<String, TypeDefinitionNode> types;
    private final List<Diagnostic> diagnostics;

    public TypeFixer(Map<String, TypeDefinitionNode> types, List<Diagnostic> diagnostics) {
        this.types = types;
        this.diagnostics = diagnostics;
    }

    public void apply() {
        types.keySet().forEach(this::fixType);
    }

    private void fixType(String typeName) {
        TypeDefinitionNode typeDefinition = types.get(typeName);
        if (typeDefinition == null) {
            return;
        }

        Node typeDesc = typeDefinition.typeDescriptor();
        if (!(typeDesc instanceof RecordTypeDescriptorNode recordTypeDesc)) {
            return;
        }

        Map<String, RecordFieldNode> definedFields = new HashMap<>();
        Map<String, List<RecordFieldNode>> includedFields = new HashMap<>();

        processRecordFields(recordTypeDesc.fields(), definedFields, includedFields);
        addResolvedFieldsToRecord(typeName, recordTypeDesc, definedFields, includedFields);
    }

    private void processRecordFields(NodeList<Node> fields, Map<String, RecordFieldNode> definedFields,
                                     Map<String, List<RecordFieldNode>> includedFields) {
        for (Node field : fields) {
            if (field instanceof TypeReferenceNode typeRef) {
                processIncludedType(typeRef, includedFields);
            } else if (field instanceof RecordFieldNode recordField) {
                definedFields.put(recordField.fieldName().text(), recordField);
            }
        }
    }

    private void processIncludedType(TypeReferenceNode typeRef, Map<String, List<RecordFieldNode>> includedFields) {
        String typeName = extractTypeName(typeRef);
        if (typeName != null) {
            Map<String, List<RecordFieldNode>> fields = collectRecordFields(typeName,
                    new ArrayList<>(List.of(typeName)));
            mergeFields(fields, includedFields);
        }
    }

    private static String extractTypeName(TypeReferenceNode typeRef) {
        Node typeName = typeRef.typeName();
        return (typeName instanceof IdentifierToken token) ? token.text() : null;
    }

    private void addResolvedFieldsToRecord(String parentRecordName, RecordTypeDescriptorNode recordTypeDesc,
                                           Map<String, RecordFieldNode> definedFields,
                                           Map<String, List<RecordFieldNode>> includedFields) {
        Map<String, RecordFieldNode> resolvedFields = resolveFieldConflicts(includedFields, parentRecordName);

        for (Map.Entry<String, RecordFieldNode> entry : resolvedFields.entrySet()) {
            if (!definedFields.containsKey(entry.getKey())) {
                recordTypeDesc = recordTypeDesc.modify()
                        .withFields(recordTypeDesc.fields().add(entry.getValue()))
                        .apply();
                this.types.put(parentRecordName, this.types.get(parentRecordName).modify()
                        .withTypeDescriptor(recordTypeDesc).apply());
            }
        }
    }

    private Map<String, List<RecordFieldNode>> collectRecordFields(String recordTypeName,
                                                                   List<String> visitedRecords) {
        if (visitedRecords.contains(recordTypeName) || !types.containsKey(recordTypeName)) {
            return new HashMap<>();
        }

        TypeDefinitionNode typeDefinition = types.get(recordTypeName);
        Node typeDesc = typeDefinition.typeDescriptor();
        if (!(typeDesc instanceof RecordTypeDescriptorNode recordTypeDesc)) {
            return new HashMap<>();
        }

        Map<String, List<RecordFieldNode>> recordFields = new HashMap<>();
        visitedRecords.add(recordTypeName);

        for (Node field : recordTypeDesc.fields()) {
            if (field instanceof TypeReferenceNode typeRef) {
                String includedTypeName = extractTypeName(typeRef);
                if (includedTypeName != null) {
                    Map<String, List<RecordFieldNode>> nestedFields =
                        collectRecordFields(includedTypeName, new ArrayList<>(visitedRecords));
                    mergeFields(nestedFields, recordFields);
                }
            } else if (field instanceof RecordFieldNode recordField) {
                addFieldToMap(recordField, recordFields);
            }
        }

        return recordFields;
    }

    private static void addFieldToMap(RecordFieldNode field, Map<String, List<RecordFieldNode>> fieldsMap) {
        String fieldName = field.fieldName().text();
        fieldsMap.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(field);
    }

    private static void mergeFields(Map<String, List<RecordFieldNode>> source,
                                  Map<String, List<RecordFieldNode>> target) {
        source.forEach((key, value) ->
            target.merge(key, value, (existing, incoming) -> {
                existing.addAll(incoming);
                return existing;
            })
        );
    }

    private Map<String, RecordFieldNode> resolveFieldConflicts(Map<String, List<RecordFieldNode>> fields,
                                                               String parentRecordName) {
        Map<String, RecordFieldNode> resolvedFields = new HashMap<>();

        for (Map.Entry<String, List<RecordFieldNode>> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            List<RecordFieldNode> fieldList = entry.getValue();

            RecordFieldNode resolvedField = resolveField(fieldList, parentRecordName);
            if (resolvedField != null) {
                resolvedFields.put(fieldName, resolvedField);
            }
        }

        return resolvedFields;
    }

    private RecordFieldNode resolveField(List<RecordFieldNode> fields, String parentRecordName) {
        if (fields.isEmpty()) {
            return null;
        }

        if (fields.size() == 1) {
            RecordFieldNode field = fields.getFirst();
            return field.metadata().isPresent() ? field : null;
        }

        String firstFieldType = fields.getFirst().typeName().toString();
        boolean allTypesMatch = fields.stream()
            .allMatch(field -> field.typeName().toString().equals(firstFieldType));
        if (!allTypesMatch) {
            String fieldName = fields.getFirst().fieldName().text();
            CommonDiagnostic diagnostic = new CommonDiagnostic(OAS_COMMON_102, fieldName, parentRecordName);
            diagnostics.add(diagnostic);
        }

        return fields.getFirst();
    }
}
