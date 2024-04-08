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
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;

public class TypesDocCommentGenerator implements DocCommentsGenerator {
    OpenAPI openAPI;
    SyntaxTree syntaxTree;

    public TypesDocCommentGenerator(SyntaxTree syntaxTree, OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.syntaxTree = syntaxTree;
    }
    @Override
    public SyntaxTree updateSyntaxTreeWithDocComments() {
        //generate type doc comments
        Node rootNode = syntaxTree.rootNode();
        //iterate through the root node and add doc comments
        Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
        ModulePartNode modulePartNode = (ModulePartNode) rootNode;
        NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
        List<TypeDefinitionNode> updatedList = new ArrayList<>();

        members.forEach(member -> {
            TypeDefinitionNode typeDef = (TypeDefinitionNode) member;
            // Find a matching schema based on type name
            TypeDefinitionNode finalTypeDef = typeDef;
            if (schemas == null) {
                updatedList.add(typeDef);
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
                    if (schema.getProperties() != null) {
                        schema.getProperties().forEach((key, value) -> {
                            fields.forEach(field -> {
                                RecordFieldNode recordField = (RecordFieldNode) field;
                                if (recordField.fieldName().text().replace("'", "")
                                        .trim().equals(key)) {
                                    if (value.getDescription() != null) {
                                        Optional<MetadataNode> metadata = recordField.metadata();
                                        MetadataNode metadataNode = updateMetadataNode(metadata, value);
                                        recordField = recordField.modify(metadataNode,
                                                recordField.readonlyKeyword().orElse(null),
                                                recordField.typeName(),
                                                recordField.fieldName(),
                                                recordField.questionMarkToken().orElse(null),
                                                recordField.semicolonToken());
                                    }
                                    updatedFields.add(recordField);
                                }
                            });
                        });
                    }
                    typeDef = typeDef.modify(typeDef.metadata().orElse(null),
                            typeDef.visibilityQualifier().get(),
                            typeDef.typeKeyword(),
                            typeDef.typeName(),
                            NodeFactory.createRecordTypeDescriptorNode(
                                    record.recordKeyword(),
                                    record.bodyStartDelimiter(),
                                    updatedFields.isEmpty()? fields : createNodeList(updatedFields),
                                    record.recordRestDescriptor().orElse(null),
                                    record.bodyEndDelimiter()),
                            typeDef.semicolonToken());
                }
                Optional<MetadataNode> metadata = typeDef.metadata();
                if (schema.getDescription() != null || schema.getDeprecated() != null){
                    MetadataNode metadataNode = updateMetadataNode(metadata, schema);
                    typeDef = typeDef.modify(metadataNode,
                            typeDef.visibilityQualifier().get(),
                            typeDef.typeKeyword(),
                            typeDef.typeName(),
                            typeDef.typeDescriptor(),
                            typeDef.semicolonToken());
                }
                updatedList.add(typeDef);
            } else {
                updatedList.add(typeDef);
            }
        });
        NodeList<ModuleMemberDeclarationNode> typeMembers = AbstractNodeFactory.createNodeList(
                updatedList.toArray(new TypeDefinitionNode[updatedList.size()]));

        modulePartNode = modulePartNode.modify(modulePartNode.imports(), typeMembers, modulePartNode.eofToken());
        return syntaxTree.modifyWith(modulePartNode);
    }

    private static MetadataNode updateMetadataNode(Optional<MetadataNode> metadata, Schema<?> schema) {
        List<Node> schemaDoc = new ArrayList<>();
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
        if (schema.getDescription() != null) {
            schemaDoc.addAll(DocCommentsGeneratorUtil
                    .createAPIDescriptionDoc(schema.getDescription(), false));
        }
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
