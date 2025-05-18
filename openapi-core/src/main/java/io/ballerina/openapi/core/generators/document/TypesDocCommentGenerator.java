/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil.getSchemaDescription;

public class TypesDocCommentGenerator implements DocCommentsGenerator {
    OpenAPI openAPI;
    SyntaxTree syntaxTree;

    public TypesDocCommentGenerator(SyntaxTree syntaxTree, OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
    }
    @Override
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        if (openAPI.getComponents() == null) {
            return syntaxTree;
        }
        //generate type doc comments
        Node rootNode = syntaxTree.rootNode();
        //iterate through the root node and add doc comments
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        ModulePartNode modulePartNode = (ModulePartNode) rootNode;
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        List<ModuleMemberDeclarationNode> updatedMemberTypesList = new ArrayList<>();

        members.stream().forEach(member -> {
            if (!(member instanceof TypeDefinitionNode typeDef)) {
                updatedMemberTypesList.add(member);
                return;
            }
            // Find a matching schema based on type name
            TypeDefinitionNode finalTypeDef = typeDef;
            if (schemas == null) {
                updatedMemberTypesList.add(typeDef);
                return;
            }
            Schema<?> schema = schemas.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(finalTypeDef.typeName().text()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElse(null); // handle if no matching schema is found

            if (schema != null) {
                if (finalTypeDef.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                    List<Node> updatedFields = new ArrayList<>();
                    RecordTypeDescriptorNode record = (RecordTypeDescriptorNode) finalTypeDef.typeDescriptor();
                    NodeList<Node> fields = record.fields();
                    if (schema instanceof ComposedSchema composedSchema) {
                        List<Schema> allOf = composedSchema.getAllOf();
                        //handle special scenarios which allOf has inline objects
                        Map<String, Schema> properties = new HashMap<>();
                        if (allOf != null) {
                            for (Schema<?> allOfSchema: allOf) {
                                if (allOfSchema.getProperties() != null) {
                                    properties.putAll(allOfSchema.getProperties());
                                }
                            }
                        }
                        schema.setProperties(properties);
                    }

                    updateRecordFields(schema, updatedFields, fields, openAPI);

                    typeDef = typeDef.modify(typeDef.metadata().orElse(null),
                            typeDef.visibilityQualifier().get(),
                            typeDef.typeKeyword(),
                            typeDef.typeName(),
                            NodeFactory.createRecordTypeDescriptorNode(
                                    record.recordKeyword(),
                                    record.bodyStartDelimiter(),
                                    updatedFields.isEmpty() ? fields : createNodeList(updatedFields),
                                    record.recordRestDescriptor().orElse(null),
                                    record.bodyEndDelimiter()),
                            typeDef.semicolonToken());
                }
                Optional<MetadataNode> metadata = typeDef.metadata();
                Optional<String> schemaDescription = getSchemaDescription(schema, openAPI.getComponents());
                if (schemaDescription.isPresent() || Objects.nonNull(schema.getDeprecated())) {
                    MetadataNode metadataNode = updateMetadataNode(metadata, schema, schemaDescription);
                    typeDef = typeDef.modify(metadataNode,
                            typeDef.visibilityQualifier().get(),
                            typeDef.typeKeyword(),
                            typeDef.typeName(),
                            typeDef.typeDescriptor(),
                            typeDef.semicolonToken());
                }
                updatedMemberTypesList.add(typeDef);
            } else {
                updatedMemberTypesList.add(typeDef);
            }
        });
        NodeList<ModuleMemberDeclarationNode> updatedMembers = AbstractNodeFactory.createNodeList(
                updatedMemberTypesList.toArray(new ModuleMemberDeclarationNode[updatedMemberTypesList.size()]));

        modulePartNode = modulePartNode.modify(modulePartNode.imports(), updatedMembers, modulePartNode.eofToken());
        return syntaxTree.modifyWith(modulePartNode);
    }

    private static void updateRecordFields(Schema<?> schema, List<Node> updatedFields, NodeList<Node> fields,
                                           OpenAPI openAPI) {
        if (schema.getProperties() != null) {
            for (Node field : fields) {
                boolean isUpdated = false;
                for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
                    String key = getKey(entry);
                    Schema<?> value = entry.getValue();
                    if (field instanceof RecordFieldNode recordFieldNode) {
                        if (recordFieldNode.fieldName().text().trim().equals(key)) {
                            Optional<String> valueDescription = getSchemaDescription(value, openAPI.getComponents());
                            if (valueDescription.isPresent() || Objects.nonNull(value.getDeprecated())) {
                                Optional<MetadataNode> metadata = recordFieldNode.metadata();
                                MetadataNode metadataNode = updateMetadataNode(metadata, value,
                                        valueDescription);
                                recordFieldNode = recordFieldNode.modify(metadataNode,
                                        recordFieldNode.readonlyKeyword().orElse(null),
                                        recordFieldNode.typeName(),
                                        recordFieldNode.fieldName(),
                                        recordFieldNode.questionMarkToken().orElse(null),
                                        recordFieldNode.semicolonToken());
                                updatedFields.add(recordFieldNode);
                                isUpdated = true;
                            }
                        }

                    } else if (field instanceof RecordFieldWithDefaultValueNode
                            recordFieldWithDefaultValueNode) {
                        if (recordFieldWithDefaultValueNode.fieldName().text().trim().equals(key)) {
                            Optional<String> valueDescription = getSchemaDescription(value, openAPI.getComponents());
                            if (valueDescription.isPresent() || Objects.nonNull(value.getDeprecated())) {
                                Optional<MetadataNode> metadata = recordFieldWithDefaultValueNode
                                        .metadata();
                                MetadataNode metadataNode = updateMetadataNode(metadata, value,
                                        valueDescription);
                                recordFieldWithDefaultValueNode = recordFieldWithDefaultValueNode
                                        .modify(metadataNode,
                                                recordFieldWithDefaultValueNode.readonlyKeyword().orElse(null),
                                                recordFieldWithDefaultValueNode.typeName(),
                                                recordFieldWithDefaultValueNode.fieldName(),
                                                recordFieldWithDefaultValueNode.equalsToken(),
                                                recordFieldWithDefaultValueNode.expression(),
                                                recordFieldWithDefaultValueNode.semicolonToken());

                                updatedFields.add(recordFieldWithDefaultValueNode);
                                isUpdated = true;
                            }
                        }
                    }
                    if (isUpdated) {
                        break;
                    }
                }
                if (!isUpdated) {
                    updatedFields.add(field);
                }
            }
        }
    }

    private static String getKey(Map.Entry<String, Schema> entry) {
        return GeneratorUtils.getBallerinaNameExtension(entry.getValue())
                .orElse(GeneratorUtils.escapeIdentifier(entry.getKey()));
    }

    private static MetadataNode updateMetadataNode(Optional<MetadataNode> metadata, Schema<?> schema,
                                                   Optional<String> schemaDescription) {
        List<Node> schemaDoc = new ArrayList<>();
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
        schemaDescription.ifPresent(description ->
                schemaDoc.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(description, false)));
        if (schema.getDeprecated() != null) {
            DocCommentsGeneratorUtil.extractDeprecatedAnnotation(schema.getExtensions(),
                    schemaDoc, typeAnnotations);
        }
        MarkdownDocumentationNode documentationNode = NodeFactory.createMarkdownDocumentationNode(
                createNodeList(schemaDoc));
        MetadataNode metadataNode;
        if (metadata.isEmpty()) {
            metadataNode = NodeFactory.createMetadataNode(documentationNode, createNodeList(typeAnnotations));

        } else {
            metadataNode = metadata.get();
            NodeList<AnnotationNode> annotations = metadataNode.annotations();
            annotations = annotations.addAll(typeAnnotations);
            metadataNode = NodeFactory.createMetadataNode(documentationNode, annotations);
        }
        return metadataNode;
    }
}
