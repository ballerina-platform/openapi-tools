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
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.type.diagnostic.TypeGenerationDiagnosticMessages;
import io.ballerina.openapi.core.generators.type.diagnostic.TypeGeneratorDiagnostic;
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

    public TypeGeneratorResult generateTypeDescriptorNodeForOASSchema(Schema<?> schema, boolean ignoreNullableFlag) {
        HashMap<String, TypeDefinitionNode> subtypesMap = new HashMap<>();
        Optional<TypeDescriptorNode> typeDescriptorNode;
        try {
            typeDescriptorNode = generateTypeDescriptorNodeForOASSchema(schema, ignoreNullableFlag,
                    subtypesMap, new HashMap<>());
        } catch (InvalidReferenceException | OASTypeGenException e) {
            TypeGeneratorDiagnostic diagnostic = new TypeGeneratorDiagnostic(
                    TypeGenerationDiagnosticMessages.OAS_TYPE_103, e.getMessage());
            diagnostics.add(diagnostic);
            return new TypeGeneratorResult(Optional.empty(), subtypesMap);
        }
        return new TypeGeneratorResult(typeDescriptorNode, subtypesMap);
    }

    /**
     * Generate typeDescriptor for given schema.
     */
    private Optional<TypeDescriptorNode> generateTypeDescriptorNodeForOASSchema(
            Schema<?> schema, boolean ignoreNullableFlag, HashMap<String, TypeDefinitionNode> subTypesMap,
            HashMap<String, NameReferenceNode> pregeneratedTypeMap) throws InvalidReferenceException,
            OASTypeGenException {
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
                    //this check for safe method because swagger parser has a issue with representing
                    // the wrong name for reference
                    schema = GeneratorMetaData.getInstance()
                            .getOpenAPI().getComponents().getSchemas().get(recordName);
                }
                TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, GeneratorUtils.getValidName(
                        recordName.trim(), true), null, ignoreNullableFlag,
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
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, ignoreNullableFlag,
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
                ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(schema, null, ignoreNullableFlag,
                        schemaType, subTypesMap, pregeneratedTypeMap);
                return arrayTypeGenerator.getTypeDescNodeForArraySchema(schema, subTypesMap);
            } else if (isPrimitiveType) {
                PrimitiveTypeGenerator primitiveTypeGenerator = new PrimitiveTypeGenerator(schema, null,
                        ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = primitiveTypeGenerator.generateTypeDescriptorNode();
                this.diagnostics.addAll(primitiveTypeGenerator.getDiagnostics());
                return Optional.ofNullable(typeDescriptorNode);
                //This returns identifier node for the types: int, float, decimal, boolean, string
//                IdentifierToken identifierToken = createIdentifierToken(
//                        GeneratorUtils.convertOpenAPITypeToBallerina(schema),
//                        AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
//                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            } else if (schemaType.equals(GeneratorConstants.OBJECT) || GeneratorUtils.isObjectSchema(schema)) {
                RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null, ignoreNullableFlag,
                        subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = recordTypeGenerator.generateTypeDescriptorNode();
                this.diagnostics.addAll(recordTypeGenerator.getDiagnostics());
                return Optional.ofNullable(typeDescriptorNode);
            } else {
                return Optional.empty();
            }
        } else if (schema.getOneOf() != null) {
            return Optional.ofNullable(getUnionNodeForOneOf(schema, ignoreNullableFlag,
                    subTypesMap, pregeneratedTypeMap));
        } else if (schema.getAllOf() != null) {
            AllOfRecordTypeGenerator allOfRecordTypeGenerator = new AllOfRecordTypeGenerator(schema, null,
                    ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode typeDescriptorNode = allOfRecordTypeGenerator.generateTypeDescriptorNode();
            this.diagnostics.addAll(allOfRecordTypeGenerator.getDiagnostics());
            return Optional.ofNullable(typeDescriptorNode);
        } else if (schema.getAnyOf() != null) {
            UnionTypeGenerator unionTypeGenerator = new UnionTypeGenerator(schema, null, ignoreNullableFlag,
                    subTypesMap, pregeneratedTypeMap);
            return Optional.ofNullable(unionTypeGenerator.generateTypeDescriptorNode());
        }
        return Optional.empty();
    }

    private TypeDescriptorNode getUnionNodeForOneOf(Schema<?> schema, boolean ignoreNullableFlag,
                                                    HashMap<String, TypeDefinitionNode> subTypesMap,
                                                    HashMap<String, NameReferenceNode> pregeneratedTypeMap)
            throws OASTypeGenException, InvalidReferenceException {
        Iterator<Schema> iterator = schema.getOneOf().iterator();
        List<TypeDescriptorNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");
        while (iterator.hasNext()) {
            Schema<?> contentType = iterator.next();
            Optional<TypeDescriptorNode> qualifiedNodeType = generateTypeDescriptorNodeForOASSchema(contentType,
                    ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
            if (qualifiedNodeType.isEmpty()) {
                continue;
            }
            qualifiedNodes.add(qualifiedNodeType.get());
        }
        TypeDescriptorNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        TypeDescriptorNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
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

    private SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return createSimpleNameReferenceNode(createIdentifierToken(name));
    }

}
