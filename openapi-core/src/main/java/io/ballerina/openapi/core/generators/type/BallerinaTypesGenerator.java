/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.core.generators.type;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.generators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.ArrayTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.PrimitiveTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.RecordTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.TypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.UnionTypeGenerator;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.type.model.TypeGeneratorResult;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * This class wraps the {@link Schema} from openapi models inorder to overcome complications
 * while populating syntax tree.
 *
 * @since 1.3.0
 */
public class BallerinaTypesGenerator {

    private final List<Diagnostic> diagnostics = new ArrayList<>();

    /**
     * This public constructor is used to generate record and other relevant data type when the nullable flag is
     * enabled in the openapi command.
     *
     * @param openAPI    OAS definition
     * @param isNullable nullable value
     */
    public BallerinaTypesGenerator(OpenAPI openAPI, boolean isNullable) {
        GeneratorMetaData.createInstance(openAPI, isNullable);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public TypeGeneratorResult generateTypeDescriptorNodeForOASSchema(Schema<?> schema, boolean overrideNullable) {
        HashMap<String, TypeDefinitionNode> subtypesMap = new HashMap<>();
        Optional<TypeDescriptorNode> typeDescriptorNode;
        try {
            typeDescriptorNode = generateTypeDescriptorNodeForOASSchema(schema, overrideNullable,
                    subtypesMap, new HashMap<>());
        } catch (BallerinaOpenApiException | OASTypeGenException e) {
            // todo : diagnostic this exception
            return new TypeGeneratorResult(null, subtypesMap);
        }
        return new TypeGeneratorResult(typeDescriptorNode, subtypesMap);
    }

    /**
     * Generate typeDescriptor for given schema.
     */
    private Optional<TypeDescriptorNode> generateTypeDescriptorNodeForOASSchema(
            Schema<?> schema, boolean overrideNullable, HashMap<String, TypeDefinitionNode> subTypesMap,
            HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws BallerinaOpenApiException, OASTypeGenException {
        if (schema == null) {
            return Optional.empty();
        }

        if (schema.get$ref() != null) {
            String schemaName = GeneratorUtils.extractReferenceType(schema.get$ref());
            String recordName = GeneratorUtils.getValidName(schemaName, true);
            String typeName = GeneratorUtils.escapeIdentifier(recordName);
            if (!pregeneratedTypeMap.containsKey(typeName)) {
                schema = GeneratorMetaData.getInstance()
                        .getOpenAPI().getComponents().getSchemas().get(schemaName);
                if (schema == null) {
                    //this check for safe method because swagger parser has a issue with representing the wrong name for reference
                    schema = GeneratorMetaData.getInstance()
                            .getOpenAPI().getComponents().getSchemas().get(recordName);
                }
                TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, GeneratorUtils.getValidName(
                        recordName.trim(), true), null, overrideNullable,
                        subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
                TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                        createToken(PUBLIC_KEYWORD),
                        createToken(TYPE_KEYWORD),
                        createIdentifierToken(typeName),
                        typeDescriptorNode,
                        createToken(SEMICOLON_TOKEN));
                this.diagnostics.addAll(typeGenerator.getDiagnostics());
                pregeneratedTypeMap.put(typeName, getSimpleNameReferenceNode(typeName));
                subTypesMap.put(typeName, typeDefinitionNode);
            }
            return Optional.ofNullable(getSimpleNameReferenceNode(typeName));
        } else if (GeneratorUtils.isMapSchema(schema)) {
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, overrideNullable,
                    subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode record = recordTypeGenerator.generateTypeDescriptorNode();
            this.diagnostics.addAll(recordTypeGenerator.getDiagnostics());
            return Optional.ofNullable(record);
        } else if (GeneratorUtils.getOpenAPIType(schema) != null) {
            String schemaType = GeneratorUtils.getOpenAPIType(schema);
            boolean isPrimitiveType = schemaType.equals(GeneratorConstants.INTEGER) ||
                    schemaType.equals(GeneratorConstants.NUMBER) || schemaType.equals(GeneratorConstants.BOOLEAN) ||
                    schemaType.equals(GeneratorConstants.STRING);
            if (GeneratorUtils.isArraySchema(schema)) {
                ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(schema, null, overrideNullable,
                        schemaType, subTypesMap, pregeneratedTypeMap);
                return arrayTypeGenerator.getTypeDescNodeForArraySchema(schema, subTypesMap);
            } else if (isPrimitiveType) {
                PrimitiveTypeGenerator primitiveTypeGenerator = new PrimitiveTypeGenerator(schema, null,
                        overrideNullable, subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = primitiveTypeGenerator.generateTypeDescriptorNode();
                this.diagnostics.addAll(primitiveTypeGenerator.getDiagnostics());
                return Optional.ofNullable(typeDescriptorNode);
                //This returns identifier node for the types: int, float, decimal, boolean, string
//                IdentifierToken identifierToken = createIdentifierToken(
//                        GeneratorUtils.convertOpenAPITypeToBallerina(schema),
//                        AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
//                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            } else if (schemaType.equals(GeneratorConstants.OBJECT) || GeneratorUtils.isObjectSchema(schema)) {
                RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, overrideNullable,
                        subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = recordTypeGenerator.generateTypeDescriptorNode();
                this.diagnostics.addAll(recordTypeGenerator.getDiagnostics());
                return Optional.ofNullable(typeDescriptorNode);
            } else {
                return Optional.empty();
            }
        } else if (schema.getOneOf() != null) {
            return Optional.ofNullable(getUnionNodeForOneOf(schema, overrideNullable,
                    subTypesMap, pregeneratedTypeMap));
        } else if (schema.getAllOf() != null) {
            AllOfRecordTypeGenerator allOfRecordTypeGenerator = new AllOfRecordTypeGenerator(schema, null,
                    overrideNullable, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode typeDescriptorNode = allOfRecordTypeGenerator.generateTypeDescriptorNode();
            this.diagnostics.addAll(allOfRecordTypeGenerator.getDiagnostics());
            return Optional.ofNullable(typeDescriptorNode);
        } else if (schema.getAnyOf() != null) {
            UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(schema, null, overrideNullable,
                    subTypesMap, pregeneratedTypeMap);
            return Optional.ofNullable(unionTypeGenerator.generateTypeDescriptorNode());
        }
        return Optional.empty();
    }

    private TypeDescriptorNode getUnionNodeForOneOf(Schema<?> schema, boolean overrideNullable,
                                                    HashMap<String, TypeDefinitionNode> subTypesMap,
                                                    HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws OASTypeGenException, BallerinaOpenApiException {
        Iterator<Schema> iterator = schema.getOneOf().iterator();
        List<NameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");
        while (iterator.hasNext()) {
            Schema<?> contentType = iterator.next();
            Optional<TypeDescriptorNode> qualifiedNodeType = generateTypeDescriptorNodeForOASSchema(contentType,
                    overrideNullable, subTypesMap, pregeneratedTypeMap);
            if (qualifiedNodeType.isEmpty()) {
                continue;
            }
            if (qualifiedNodeType.get() instanceof NameReferenceNode nameReferenceNode) {
                qualifiedNodes.add(nameReferenceNode);
            } else {
                qualifiedNodes.add(createSimpleNameReferenceNode(createIdentifierToken(qualifiedNodeType.get()
                        .toSourceCode())));
            }
        }
        NameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        NameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

//    public TypeGeneratorResult createTypeInclusionRecord(String includedType, TypeDescriptorNode type) {
//        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
//        SimpleNameReferenceNode nameReferenceNode = createTypeInclusionRecord(includedType, type, subTypesMap);
//        return new TypeGeneratorResult(Optional.of(nameReferenceNode), subTypesMap, new ArrayList<>());
//    }

//    /**
//     * Create recordType TypeDescriptor.
//     */
//    private SimpleNameReferenceNode createTypeInclusionRecord(String includedType, TypeDescriptorNode type,
//                                                              HashMap<String, TypeDefinitionNode> subTypesMap) {
//        //todo refactor this implementation with
//        String recordName = includedType + GeneratorUtils.getValidName(type.toString(), true);
//        Token recordKeyWord = createToken(RECORD_KEYWORD);
//        Token bodyStartDelimiter = createIdentifierToken("{|");
//        // Create record fields
//        List<Node> recordFields = new ArrayList<>();
//        // Type reference node
//        Token asteriskToken = createIdentifierToken("*");
//        QualifiedNameReferenceNode typeNameField = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
//                includedType);
//        TypeReferenceNode typeReferenceNode = createTypeReferenceNode(
//                asteriskToken,
//                typeNameField,
//                createToken(SyntaxKind.SEMICOLON_TOKEN));
//        recordFields.add(typeReferenceNode);
//
//        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.BODY, GeneratorUtils.SINGLE_WS_MINUTIAE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE);
//        RecordFieldNode recordFieldNode = createRecordFieldNode(
//                null, null,
//                type,
//                fieldName, null,
//                createToken(SyntaxKind.SEMICOLON_TOKEN));
//        recordFields.add(recordFieldNode);
//
//        NodeList<Node> fieldsList = createSeparatedNodeList(recordFields);
//        Token bodyEndDelimiter = createIdentifierToken("|}");
//
//        RecordTypeDescriptorNode recordTypeDescriptorNode = createRecordTypeDescriptorNode(
//                recordKeyWord,
//                bodyStartDelimiter,
//                fieldsList, null,
//                bodyEndDelimiter);
//
//        TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
//                createToken(PUBLIC_KEYWORD),
//                createToken(TYPE_KEYWORD),
//                createIdentifierToken(recordName),
//                recordTypeDescriptorNode,
//                createToken(SEMICOLON_TOKEN));
//        subTypesMap.put(typeDefinitionNode.typeName().text(), typeDefinitionNode);
//        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
//    }
//
//    //todo: revist the place we are using this method
//    public TypeDescriptorReturnType generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName) throws
//            OASTypeGenException {
//        HashMap<String, TypeDefinitionNode> subtypesMap = new HashMap<>();
//        Optional<TypeDescriptorNode> typeDescriptorNode =
//                generateTypeDescriptorForJsonContent(schema, recordName, subtypesMap);
//        return new TypeDescriptorReturnType(typeDescriptorNode, subtypesMap);
//    }
//
//    public TypeDescriptorReturnType getReferencedQueryParamTypeFromSchema(Schema<?> schema, String typeName) throws OASTypeGenException {
//        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
//        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, typeName, null, subTypesMap, new HashMap<>());
//        TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
//        subTypesMap.put(typeName, createTypeDefinitionNode(null,
//                createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.TYPE_KEYWORD),
//                createIdentifierToken(typeName), typeDescriptorNode, createToken(SyntaxKind.SEMICOLON_TOKEN)));
//        return new TypeDescriptorReturnType(Optional.of(createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(typeName))),
//                subTypesMap);
//    }
//
//    private Optional<TypeDescriptorNode> generateTypeDescriptorForJsonContent(Schema<?> schema, String recordName,
//                                                                              HashMap<String, TypeDefinitionNode> subTypesMap) throws
//            OASTypeGenException {
//        Optional<TypeDescriptorNode> typeDecNode =
//                generateTypeDescriptorNodeForOASSchema(schema, subTypesMap, new HashMap<>());
//        if (typeDecNode.isEmpty()) {
//            return Optional.of(getSimpleNameReferenceNode(GeneratorConstants.JSON));
//        } else if (typeDecNode.get() instanceof RecordTypeDescriptorNode recordNode) {
//            if (recordName == null) {
//                return typeDecNode;
//            }
//            Token rname;
//            if (schema.get$ref() != null) {
//                rname = createIdentifierToken(GeneratorUtils.getValidName(GeneratorUtils.extractReferenceType(schema.get$ref()), true));
//            } else {
//                rname = createIdentifierToken(recordName);
//            }
//            TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
//                    createToken(PUBLIC_KEYWORD),
//                    createToken(TYPE_KEYWORD),
//                    rname,
//                    recordNode,
//                    createToken(SEMICOLON_TOKEN));
//            subTypesMap.put(recordName, typeDefinitionNode);
//            return Optional.of(recordNode);
//        }
//        return typeDecNode;
//    }
//
//    //todo make this function common name
//    public TokenReturnType getQueryParamTypeToken(Schema<?> schema) throws OASTypeGenException {
//        HashMap<String, TypeDefinitionNode> subTypesMap = new HashMap<>();
//        Token token = getQueryParamTypeToken(schema, subTypesMap);
//        return new TokenReturnType(Optional.of(token), subTypesMap);
//    }
//
//    private Token getQueryParamTypeToken(Schema<?> schema, HashMap<String, TypeDefinitionNode> subTypesMap)
//            throws OASTypeGenException {
//        if (schema instanceof MapSchema) {
//            // handle inline record open
//            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null,
//                    subTypesMap, new HashMap<>());
//            TypeDescriptorNode recordNode = recordTypeGenerator.generateTypeDescriptorNode();
//            return createIdentifierToken(recordNode.toSourceCode(),
//                    GeneratorUtils.SINGLE_WS_MINUTIAE,
//                    GeneratorUtils.SINGLE_WS_MINUTIAE);
//        } else {
//            return createIdentifierToken(GeneratorUtils.convertOpenAPITypeToBallerina(schema),
//                    GeneratorUtils.SINGLE_WS_MINUTIAE,
//                    GeneratorUtils.SINGLE_WS_MINUTIAE);
//        }
//    }
//
//    public ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(TypeDescriptorNode typeDescriptorNode) {
//        return ArrayTypeGenerator.getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
//    }
//
    private SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return createSimpleNameReferenceNode(createIdentifierToken(name));
    }

}
