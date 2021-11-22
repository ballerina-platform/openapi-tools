/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
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
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.DocCommentsGenerator;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ANYDATA_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.generators.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.generators.GeneratorConstants.MAX_ARRAY_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.generators.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;
import static io.ballerina.openapi.generators.GeneratorUtils.isValidSchemaName;

/**
 * This class wraps the {@link Schema} from openapi models inorder to overcome complications
 * while populating syntax tree.
 */
public class BallerinaSchemaGenerator {
    private List<TypeDefinitionNode> typeDefinitionNodeList;
    private final boolean isNullable;
    private final OpenAPI openAPI;

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     */
    public BallerinaSchemaGenerator(OpenAPI openAPI, boolean isNullable) {
        this.openAPI = openAPI;
        this.isNullable = isNullable;
        this.typeDefinitionNodeList = new LinkedList<>();
    }

    /**
     * This public constructor is used to generate record and other relevant data type when the absent of the nullable
     * flag in the openapi command.
     *
     * @param openAPI OAS definition
     */
    public BallerinaSchemaGenerator(OpenAPI openAPI) {
        this(openAPI, false);
    }

    /**
     * Returns a list of type definition nodes.
     */
    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }

    /**
     * Set the typeDefinitionNodeList.
     */
    public void setTypeDefinitionNodeList(List<TypeDefinitionNode> typeDefinitionNodeList) {
        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }

    /**
     * Generate syntaxTree for component schema.
     */
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {

        if (openAPI.getComponents() != null) {
            // Refactor schema name with valid name
            // Create typeDefinitionNode
            Components components = openAPI.getComponents();
            Map<String, Schema> componentsSchemas = components.getSchemas();
            if (componentsSchemas != null) {
                Map<String, Schema> refacSchema = new HashMap<>();
                for (Map.Entry<String, Schema> schemaEntry : componentsSchemas.entrySet()) {
                    String name = getValidName(schemaEntry.getKey(), true);
                    refacSchema.put(name, schemaEntry.getValue());
                }
                openAPI.getComponents().setSchemas(refacSchema);
            }
            Map<String, Schema> schemas = components.getSchemas();
            if (schemas != null) {
                for (Map.Entry<String, Schema> schema : schemas.entrySet()) {
                    List<Node> schemaDoc = new ArrayList<>();
                    if (schema.getValue().getDescription() != null) {
                        schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                                schema.getValue().getDescription(), false));
                    }
                    List<String> required = schema.getValue().getRequired();
                    if (isValidSchemaName(schema.getKey().trim())) {
                        generateTypeDefinitionNode(typeDefinitionNodeList, schema.getValue(), schemaDoc, required,
                                schema.getKey().trim());
                    }
                }
            }
        }
        //Create imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createEmptyNodeList();
        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                typeDefinitionNodeList.toArray(new TypeDefinitionNode[typeDefinitionNodeList.size()]));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generated typeDefinitionNode for openAPI schema. This types includes in the types.bal file.
     */
    private void generateTypeDefinitionNode(List<TypeDefinitionNode> typeDefinitionNodeList,
                                            Schema<?> schemaValue, List<Node> schemaDoc, List<String> required,
                                            String recordName) throws BallerinaOpenApiException {

        IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(recordName);
        String defineDatatypeName = getValidName(recordName, true);
        List<AnnotationNode> typeAnnotations = new ArrayList<>();
        if (schemaValue.getDeprecated() != null && schemaValue.getDeprecated()) {
            DocCommentsGenerator.extractDeprecatedAnnotation(schemaValue.getExtensions(),
                    schemaDoc, typeAnnotations);
        }
        // record field list for given schema.
        List<Node> recordFieldList = new ArrayList<>();
        TypeDefinitionNode typeDefNode;
        if (schemaValue instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schemaValue;
            getTypeDefinitionNodeForComposedSchema(typeDefinitionNodeList, schemaDoc, required, typeName,
                    defineDatatypeName, typeAnnotations, recordFieldList, composedSchema);
        } else if (schemaValue.getProperties() != null || schemaValue instanceof ObjectSchema) {
            Map<String, Schema> fields = schemaValue.getProperties();
            typeDefNode = getTypeDefinitionNodeForObjectSchema(required, typeName, recordFieldList, fields,
                    schemaDoc, createNodeList(typeAnnotations));
            typeDefinitionNodeList.add(typeDefNode);
        } else if (schemaValue.getType() != null) {
            if (schemaValue instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) schemaValue;
                typeDefNode = getArrayTypeDefinition(arraySchema, defineDatatypeName, schemaDoc, typeAnnotations);
                typeDefinitionNodeList.add(typeDefNode);
            } else {
                MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(
                        createNodeList(schemaDoc));
                MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
                if (schemaValue.getType() != null) {
                    String typeDescriptorName = convertOpenAPITypeToBallerina(schemaValue.getType().trim());
                    typeDescriptorName = getNullableType(schemaValue, typeDescriptorName);
                    typeDefNode = getTypeDefinitionNode(createIdentifierToken(typeDescriptorName),
                            getValidName((defineDatatypeName), true),
                            metadataNode);
                    typeDefinitionNodeList.add(typeDefNode);
                }
            }
        } else if (schemaValue.get$ref() != null) {
            Token typeRef = AbstractNodeFactory.createIdentifierToken(getValidName(
                    extractReferenceType(schemaValue.get$ref()), true));
            MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
            MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
            String typeDescriptorName = typeRef.text();
            typeDescriptorName = getNullableType(schemaValue, typeDescriptorName);
            typeDefNode = getTypeDefinitionNode(createIdentifierToken(typeDescriptorName),
                    getValidName((defineDatatypeName), true), metadataNode);
            typeDefinitionNodeList.add(typeDefNode);
        } else if (schemaValue.getType() == null) {

            MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
            MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));

            String anyData = getNullableType(schemaValue, GeneratorConstants.ANY_DATA);
            typeDefNode = getTypeDefinitionNode(createIdentifierToken(anyData), getValidName((defineDatatypeName),
                    true), metadataNode);
            typeDefinitionNodeList.add(typeDefNode);
        } else {
            throw new BallerinaOpenApiException("Unsupported OAS schema type.");
        }
    }

    /**
     * Generate {@code TypeDefinitionNode} for given composed schema.
     */
    private void getTypeDefinitionNodeForComposedSchema(List<TypeDefinitionNode> typeDefinitionNodeList,
                                                        List<Node> schemaDoc, List<String> required,
                                                        IdentifierToken typeName, String defineDatatypeName,
                                                        List<AnnotationNode> typeAnnotations,
                                                        List<Node> recordFieldList, ComposedSchema composedSchema)
            throws BallerinaOpenApiException {

        TypeDefinitionNode typeDefNode;
        if (composedSchema.getAllOf() != null) {
            List<Schema> allOf = composedSchema.getAllOf();
            typeDefNode = getAllOfTypeDefinitionNode(schemaDoc, required, typeName, recordFieldList, allOf);
            typeDefinitionNodeList.add(typeDefNode);
        } else if (composedSchema.getOneOf() != null) {
            List<Schema> oneOf = composedSchema.getOneOf();
            String unionTypeCont = GeneratorUtils.getOneOfUnionType(oneOf);
            String type = escapeIdentifier(defineDatatypeName.trim());
            MetadataNode metadataNode = createMetadataNode(
                    createMarkdownDocumentationNode(createNodeList(schemaDoc)), createNodeList(typeAnnotations));
            typeDefNode = getTypeDefinitionNode(createIdentifierToken(unionTypeCont), type, metadataNode);
            typeDefinitionNodeList.add(typeDefNode);
        } else if (composedSchema.getAnyOf() != null) {
            List<Schema> anyOf = composedSchema.getAnyOf();
            String unionTypeCont = GeneratorUtils.getOneOfUnionType(anyOf);
            String type = escapeIdentifier(defineDatatypeName.trim());
            MetadataNode metadataNode = createMetadataNode(
                    createMarkdownDocumentationNode(createNodeList(schemaDoc)), createNodeList(typeAnnotations));
            typeDefNode = getTypeDefinitionNode(createIdentifierToken(unionTypeCont), type, metadataNode);
            typeDefinitionNodeList.add(typeDefNode);
        }
    }

    /**
     * Util function for generate typeDefinition node.
     */
    private TypeDefinitionNode getTypeDefinitionNode(IdentifierToken nameReference, String type,
                                                     MetadataNode metadataNode) {

        return createTypeDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD),
                createIdentifierToken(type), createSimpleNameReferenceNode(nameReference),
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate Node for allOf data binding.
     *
     * @param schemaDoc       - API docs list
     * @param required        - required fields
     * @param typeName        - main record name
     * @param recordFieldList - generated previous record field list
     * @param allOf           - allOf schema values
     * @return TypeDefinitionNode for allOf
     * @throws BallerinaOpenApiException
     */
    public TypeDefinitionNode getAllOfTypeDefinitionNode(List<Node> schemaDoc, List<String> required,
                                                         IdentifierToken typeName, List<Node> recordFieldList,
                                                         List<Schema> allOf)
            throws BallerinaOpenApiException {

        RecordTypeDescriptorNode recordTypeDescriptorNode = getAllOfRecordTypeDescriptorNode(schemaDoc, required,
                recordFieldList, allOf);
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        return NodeFactory.createTypeDefinitionNode(metadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName, recordTypeDescriptorNode,
                AbstractNodeFactory.createToken(SEMICOLON_TOKEN));
    }

    /**
     * Create {@code RecordTypeDescriptorNode} for OAS AllOf data model.
     */
    private RecordTypeDescriptorNode getAllOfRecordTypeDescriptorNode(List<Node> schemaDoc,
                                                                      List<String> required, List<Node> recordFieldList,
                                                                      List<Schema> allOf)
            throws BallerinaOpenApiException {

        for (Schema<?> allOfSchema : allOf) {
            if (allOfSchema.getType() == null && allOfSchema.get$ref() != null) {
                //Generate typeReferenceNode
                getAllOfRecordFieldForReference(schemaDoc, recordFieldList, allOfSchema);
            } else if (allOfSchema.getProperties() != null) {
                if (allOfSchema.getDescription() != null) {
                    schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(allOfSchema.getDescription(),
                            false));
                }
                Map<String, Schema> properties = allOfSchema.getProperties();
                for (Map.Entry<String, Schema> field : properties.entrySet()) {
                    addRecordFields(required, recordFieldList, field);
                }
            } else if (allOfSchema instanceof ComposedSchema) {
                ComposedSchema allOfNested = (ComposedSchema) allOfSchema;
                if (allOfNested.getAllOf() != null) {
                    for (Schema schema : allOfNested.getAllOf()) {
                        if (schema instanceof ObjectSchema) {
                            ObjectSchema objectSchema = (ObjectSchema) schema;
                            List<String> requiredField = objectSchema.getRequired();
                            Map<String, Schema> properties = objectSchema.getProperties();
                            // TODO: add api documentation
                            for (Map.Entry<String, Schema> field : properties.entrySet()) {
                                addRecordFields(requiredField, recordFieldList, field);
                            }
                        }
                    }
                }
                // TODO handle OneOf, AnyOf
            }
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
        return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD), createToken(OPEN_BRACE_TOKEN),
                fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
    }

    private void getAllOfRecordFieldForReference(List<Node> schemaDoc, List<Node> recordFieldList, Schema allOfSchema)
            throws BallerinaOpenApiException {

        Token typeRef = AbstractNodeFactory.createIdentifierToken(getValidName(
                extractReferenceType(allOfSchema.get$ref()), true));
        TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN), typeRef,
                createToken(SEMICOLON_TOKEN));
        recordFieldList.add(recordField);
        if (allOfSchema.getDescription() != null) {
            schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(allOfSchema.getDescription(), false));
        }
    }

    /**
     * This function is used to create typeDefinitionNode for objectSchema.
     *
     * @param required        - This string list include required fields in properties.
     * @param typeName        - Record Name
     * @param recordFieldList - RecordFieldList
     * @param fields          - schema properties map
     * @return This return record TypeDefinitionNode
     */

    public TypeDefinitionNode getTypeDefinitionNodeForObjectSchema(List<String> required,
                                                                   IdentifierToken typeName,
                                                                   List<Node> recordFieldList,
                                                                   Map<String, Schema> fields,
                                                                   List<Node> schemaDoc,
                                                                   NodeList<AnnotationNode> annotationNodes)
            throws BallerinaOpenApiException {
        TypeDefinitionNode typeDefinitionNode;
        if (fields != null) {
            for (Map.Entry<String, Schema> field : fields.entrySet()) {
                addRecordFields(required, recordFieldList, field);
            }
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
            RecordTypeDescriptorNode recordTypeDescriptorNode = NodeFactory.createRecordTypeDescriptorNode(
                    createToken(SyntaxKind.RECORD_KEYWORD), createToken(OPEN_BRACE_TOKEN), fieldNodes,
                    null, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            MarkdownDocumentationNode documentationNode =
                    createMarkdownDocumentationNode(createNodeList(schemaDoc));
            MetadataNode metadataNode = createMetadataNode(documentationNode, annotationNodes);
            typeDefinitionNode = NodeFactory.createTypeDefinitionNode(metadataNode,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD), typeName, recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
        } else {
            RecordTypeDescriptorNode recordTypeDescriptorNode = NodeFactory.createRecordTypeDescriptorNode(
                    createToken(SyntaxKind.RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN), createEmptyNodeList(), null,
                    createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            MarkdownDocumentationNode documentationNode =
                    createMarkdownDocumentationNode(createNodeList(schemaDoc));
            MetadataNode metadataNode = createMetadataNode(documentationNode, annotationNodes);
            typeDefinitionNode = NodeFactory.createTypeDefinitionNode(metadataNode,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD), typeName,
                    recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
        }
        return typeDefinitionNode;
    }

    /**
     * This util for generating record field with given schema properties.
     */
    private void addRecordFields(List<String> required, List<Node> recordFieldList, Map.Entry<String, Schema> field)
            throws BallerinaOpenApiException {
        // TODO: Handle allOf , oneOf, anyOf
        RecordFieldNode recordFieldNode;
        // API doc generations
        List<Node> schemaDoc = new ArrayList<>();
        String fieldN = escapeIdentifier(field.getKey().trim());
        if (field.getValue().getDescription() != null) {
            schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    field.getValue().getDescription(), false));
        } else if (field.getValue().get$ref() != null) {
            String[] split = field.getValue().get$ref().trim().split("/");
            String componentName = getValidName(split[split.length - 1], true);
            if (openAPI.getComponents().getSchemas().get(componentName) != null) {
                Schema<?> schema = openAPI.getComponents().getSchemas().get(componentName);
                if (schema.getDescription() != null) {
                    schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                            schema.getDescription(), false));
                }
            }
        }
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(fieldN);
        TypeDescriptorNode fieldTypeName = extractOpenAPISchema(field.getValue());
        Token questionMarkToken = AbstractNodeFactory.createIdentifierToken(NILLABLE);
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        if (required != null) {
            if (!required.contains(field.getKey().trim())) {
                recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                        fieldTypeName, fieldName, questionMarkToken, createToken(SEMICOLON_TOKEN));
            } else {
                recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                        fieldTypeName, fieldName, null, createToken(SEMICOLON_TOKEN));
            }
        } else {
            recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                    fieldTypeName, fieldName, questionMarkToken, createToken(SEMICOLON_TOKEN));
        }
        recordFieldList.add(recordFieldNode);
    }

    /**
     * Common method to extract OpenApi Schema type objects in to Ballerina type compatible schema objects.
     *
     * @param schema - OpenApi Schema
     */
    private TypeDescriptorNode extractOpenAPISchema(Schema<?> schema) throws BallerinaOpenApiException {

        if (schema.getType() != null || schema.getProperties() != null) {

            List<String> primitiveTypeList = new ArrayList<>(Arrays.asList(INTEGER, NUMBER, STRING, BOOLEAN));
            boolean isOASPrimitive = primitiveTypeList.contains(schema.getType());

            if (schema.getType() != null && (isOASPrimitive)) {
                String type = convertOpenAPITypeToBallerina(schema.getType().trim());
                if (schema.getType().equals(NUMBER)) {
                    if (schema.getFormat() != null) {
                        type = convertOpenAPITypeToBallerina(schema.getFormat().trim());
                    }
                }
                type = getNullableType(schema, type);
                Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            } else if (schema instanceof ArraySchema) {
                final ArraySchema arraySchema = (ArraySchema) schema;
                if (arraySchema.getItems() != null) {
                    // single array
                    return getArrayTypeDescriptorNode(arraySchema);
                }
            } else if ((schema.getType() != null && schema.getType().equals("object")) ||
                    (schema.getProperties() != null)) {
                if (schema.getProperties() != null) {
                    Map<String, Schema> properties = schema.getProperties();
                    List<Node> recordFList = new ArrayList<>();
                    List<String> required = schema.getRequired();
                    for (Map.Entry<String, Schema> property : properties.entrySet()) {
                        addRecordFields(required, recordFList, property);
                    }
                    NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);

                    return NodeFactory.createRecordTypeDescriptorNode(
                            createToken(RECORD_KEYWORD, SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE),
                            createToken(OPEN_BRACE_TOKEN, SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE), fieldNodes,
                            null, createToken(CLOSE_BRACE_TOKEN,
                                    SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE));
                } else if (schema.get$ref() != null) {
                    String type = getValidName(extractReferenceType(schema.get$ref()), true);
                    Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
                    type = getNullableType(refSchema, type);
                    Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                    return createBuiltinSimpleNameReferenceNode(null, typeName);
                } else {
                    Token typeName = AbstractNodeFactory.createIdentifierToken(
                            convertOpenAPITypeToBallerina(schema.getType().trim()));
                    return createBuiltinSimpleNameReferenceNode(null, typeName);
                }
            } else if (schema.getType() != null && schema.getType().equals("object")) {
                String type = convertOpenAPITypeToBallerina(schema.getType().trim());
                Token typeName = AbstractNodeFactory.createIdentifierToken(type);
                return createBuiltinSimpleNameReferenceNode(null, typeName);
            } else {
                throw new BallerinaOpenApiException("Unsupported OAS data type `" + schema.getType() + "`.");
            }
        } else if (schema.get$ref() != null) {
            String type = extractReferenceType(schema.get$ref());
            type = getValidName(type, true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
            type = getNullableType(refSchema, type);
            Token typeName = AbstractNodeFactory.createIdentifierToken(type);
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        } else if (schema instanceof ComposedSchema) {
            //TODO: API doc generator
            ComposedSchema composedSchema = (ComposedSchema) schema;
            return getTypeDescriptorNodeForComposedSchema(composedSchema, new ArrayList<>(), false);
        } else if (schema.getType() == null) {
            //This contains a fallback to Ballerina common type `anydata` if the OpenApi specification type is not
            // defined.
            String type = GeneratorConstants.ANY_DATA;
            type = getNullableType(schema, type);
            Token typeName = AbstractNodeFactory.createIdentifierToken(type);
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        } else {
            //This contains the OpenApi specification type is not compatible with any of the current Ballerina types.
            throw new BallerinaOpenApiException("Unsupported OAS data type.");
        }
        String type = GeneratorConstants.ANY_DATA;
        type = getNullableType(schema, type);
        Token typeName = AbstractNodeFactory.createIdentifierToken(type);
        return createBuiltinSimpleNameReferenceNode(null, typeName);
    }

    /**
     * Generate proper type name considering the nullable configurations.
     * Scenario 1 : schema.getNullable() != null && schema.getNullable() == true && nullable == true -> string?
     * Scenario 2 : schema.getNullable() != null && schema.getNullable() == true && nullable == false -> string?
     * Scenario 3 : schema.getNullable() != null && schema.getNullable() == false && nullable == false -> string
     * Scenario 4 : schema.getNullable() != null && schema.getNullable() == false && nullable == true -> string
     * Scenario 5 : schema.getNullable() == null && nullable == true -> string?
     * Scenario 6 : schema.getNullable() == null && nullable == false -> string
     *
     * @param schema Schema of the property
     * @param type   Type name
     * @return Final type of the field
     */
    private String getNullableType(Schema<?> schema, String type) {
        if (schema.getNullable() != null) {
            if (schema.getNullable()) {
                type = type + NILLABLE;
            }
        } else if (isNullable) {
            type = type + NILLABLE;
        }
        return type;
    }

    /**
     * This function is used to handle the array schema under the OAS components.
     */
    private TypeDefinitionNode getArrayTypeDefinition(ArraySchema arraySchema, String arrayName, List<Node> schemaDoc,
                                                      List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        TypeDescriptorNode fieldTypeName;
        if (arraySchema.getItems() != null) {
            fieldTypeName = extractOpenAPISchema(arraySchema.getItems());
        } else {
            Token type = AbstractNodeFactory.createToken(ANYDATA_KEYWORD);
            fieldTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null, type);
        }
        if (arraySchema.getDescription() != null) {
            schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(arraySchema.getDescription(),
                    false));
        }
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
        String fieldTypeNameStr = fieldTypeName.toString().trim();
        if (fieldTypeName.toString().endsWith(NILLABLE)) {
            fieldTypeNameStr = fieldTypeNameStr.substring(0, fieldTypeNameStr.length() - 1);
        }
        String arrayBrackets = "[]";
        if (arraySchema.getMaxItems() != null) {
            if (arraySchema.getMaxItems() <= MAX_ARRAY_LENGTH) {
                arrayBrackets = "[" + arraySchema.getMaxItems() + "]";
            } else {
                throw new BallerinaOpenApiException("Maximum item count defined in the definition exceeds the " +
                        "maximum ballerina array length.");
            }
        }
        fieldTypeNameStr = getNullableType(arraySchema, fieldTypeNameStr + arrayBrackets);
        return getTypeDefinitionNode(createIdentifierToken(fieldTypeNameStr), escapeIdentifier(arrayName),
                metadataNode);
    }

    /**
     * Generate {@code TypeDescriptorNode} for Array Schema.
     *
     * @param arraySchema - Array schema
     * @throws BallerinaOpenApiException when failure happens while processing
     */
    public TypeDescriptorNode getArrayTypeDescriptorNode(ArraySchema arraySchema) throws BallerinaOpenApiException {

        String type;
        Token typeName;
        TypeDescriptorNode memberTypeDesc;
        Token openSBracketToken = AbstractNodeFactory.createIdentifierToken("[");
        Schema<?> schemaItem = arraySchema.getItems();
        Token closeSBracketToken = AbstractNodeFactory.createIdentifierToken(getNullableType(arraySchema, "]"));
        if (schemaItem.get$ref() != null) {
            type = getValidName(extractReferenceType(arraySchema.getItems().get$ref()), true);
            typeName = AbstractNodeFactory.createIdentifierToken(type);
            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
        } else if (schemaItem instanceof ArraySchema) {
            memberTypeDesc = extractOpenAPISchema(arraySchema.getItems());
        } else if (schemaItem instanceof ObjectSchema) {
            // Array has inline record
            ObjectSchema inlineSchema = (ObjectSchema) schemaItem;
            memberTypeDesc = extractOpenAPISchema(inlineSchema);
            return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                    null, closeSBracketToken);
        } else if (schemaItem instanceof ComposedSchema) {
            // TODO: API Doc generator
            ComposedSchema composedSchema = (ComposedSchema) schemaItem;
            memberTypeDesc = getTypeDescriptorNodeForComposedSchema(composedSchema, new ArrayList<>(), true);
        } else if (schemaItem.getType() != null) {
            type = schemaItem.getType();
            closeSBracketToken = AbstractNodeFactory.createIdentifierToken(getNullableType(arraySchema, "]"));
            typeName = AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(type));
            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
        } else {
            closeSBracketToken = AbstractNodeFactory.createIdentifierToken(getNullableType(arraySchema, "]"));
            typeName = AbstractNodeFactory.createToken(ANYDATA_KEYWORD);
            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, typeName);
        }

        if (arraySchema.getMaxItems() != null) {
            if (arraySchema.getMaxItems() <= MAX_ARRAY_LENGTH) {
                return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                        createIdentifierToken(arraySchema.getMaxItems().toString()), closeSBracketToken);
            } else {
                throw new BallerinaOpenApiException("Maximum item count defined in the definition exceeds the maximum" +
                        " ballerina array length.");
            }
        }
        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken,
                null, closeSBracketToken);
    }

    private TypeDescriptorNode getTypeDescriptorNodeForComposedSchema(ComposedSchema composedSchema,
                                                                      List<Node> schemaDoc, boolean isArray)
            throws BallerinaOpenApiException {

        TypeDescriptorNode memberTypeDesc;
        String typeName;
        boolean nullableComposedSchema = false;
        if (composedSchema.getNullable() != null) {
            if (composedSchema.getNullable()) {
                nullableComposedSchema = true;
            }
        } else if (isNullable) {
            nullableComposedSchema = true;
        }
        if (composedSchema.getOneOf() != null) {
            typeName = GeneratorUtils.getOneOfUnionType(composedSchema.getOneOf());
        } else if (composedSchema.getAnyOf() != null) {
            typeName = GeneratorUtils.getOneOfUnionType(composedSchema.getAnyOf());
        } else if (composedSchema.getAllOf() != null) {
            return getAllOfRecordTypeDescriptorNode(schemaDoc, composedSchema.getRequired(), new ArrayList<>(),
                    composedSchema.getAllOf());
        } else if (composedSchema.getType() != null) {
            typeName = convertOpenAPITypeToBallerina(composedSchema.getType().trim());

        } else if (composedSchema.get$ref() != null) {
            typeName = getValidName(extractReferenceType(composedSchema.get$ref().trim()), true);

        } else {
            throw new BallerinaOpenApiException("Unsupported OAS data type.");
        }
        if (nullableComposedSchema && !isArray) {
            typeName = typeName + NILLABLE;
            return createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(typeName));
        } else {
            memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(typeName));
            return memberTypeDesc;
        }
    }
}
