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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.type.BallerinaTypesGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_RETURN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ERROR;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OPTIONAL_ERROR;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isComposedSchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isMapSchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isObjectSchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isValidSchemaName;

/**
 * This util class for maintain the operation response with ballerina return type.
 *
 * @since 1.3.0
 */
public class FunctionReturnTypeGeneratorImp implements FunctionReturnTypeGenerator {
    private OpenAPI openAPI;
    private Operation operation;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();

    public FunctionReturnTypeGeneratorImp(Operation operation, OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.operation = operation;
    }

    /**
     * Get return type of the remote function.
     *
     * @return string with return type.
     * @throws BallerinaOpenApiException - throws exception if creating return type fails.
     */
    @Override
    public Optional<ReturnTypeDescriptorNode> getReturnType() {
        //TODO: Handle multiple media-type
        //Todo handle reference reusable response schema
        List<TypeDescriptorNode> returnTypes = new ArrayList<>();
        boolean noContentResponseFound = false;
        if (operation.getResponses() != null) {
            ApiResponses responses = operation.getResponses();
            for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
                noContentResponseFound = false;
                String statusCode = entry.getKey();
                ApiResponse response = entry.getValue();
                if (statusCode.startsWith("2")) {
                    Content content = response.getContent();
                    if (content != null && content.size() > 0) {
                        Set<Map.Entry<String, MediaType>> mediaTypes = content.entrySet();
                        for (Map.Entry<String, MediaType> media : mediaTypes) {
                            TypeDescriptorNode type;
                            if (media.getValue().getSchema() != null) {
                                Schema schema = media.getValue().getSchema();
                                Optional<TypeDescriptorNode> dataType = getDataType(media, schema);
                                if (dataType.isPresent()) {
                                    type = dataType.get();
                                } else {
                                    String mediaType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
                                    type = createSimpleNameReferenceNode(createIdentifierToken(mediaType));
                                }
                            } else {
                                String mediaType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
                                type = createSimpleNameReferenceNode(createIdentifierToken(mediaType));
                            }
                            returnTypes.add(type);
                            // Currently support for first media type
                            break;
                        }
                    } else {
                        noContentResponseFound = true;
                    }
                }
                if ((statusCode.startsWith("1") || statusCode.startsWith("2") || statusCode.startsWith("3")) &&
                        response.getContent() == null) {
                    noContentResponseFound = true;
                }
            }
        }
        if (!returnTypes.isEmpty()) {
            if (noContentResponseFound) {
                returnTypes.add(createSimpleNameReferenceNode(createIdentifierToken(ERROR+NILLABLE)));
            } else {
                returnTypes.add(createSimpleNameReferenceNode(createIdentifierToken(ERROR)));
            }

            return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                    createUnionTypeDescriptorNode(returnTypes.get(0), createToken(PIPE_TOKEN), returnTypes.get(1))));
        } else if (noContentResponseFound) {
            return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                    createIdentifierToken(OPTIONAL_ERROR)));
        } else {
            return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                    createIdentifierToken(DEFAULT_RETURN)));
        }
    }

    /**
     * Get return data type by traversing OAS schemas.
     */
    private Optional<TypeDescriptorNode> getDataType(Map.Entry<String, MediaType> media, Schema schema) {
        //todo if the composedSchema is there then the it send it as referencetype

//        if (isComposedSchema(schema)) {
//            type = generateReturnDataTypeForComposedSchema(operation, type, schema, isSignature);
//        } else if (isObjectSchema(schema)) {
//            type = handleInLineRecordInResponse(operation, media, schema);
//        } else if (isMapSchema(schema)) {
//            type = handleResponseWithMapSchema(operation, media, schema);
//        } else if (schema.get$ref() != null) {
//            type = getValidName(extractReferenceType(schema.get$ref()), true);
//            Schema componentSchema = openAPI.getComponents().getSchemas().get(type);
//            this logic is to move in the openAPI validator level
//            if (!isValidSchemaName(type)) {
//                String operationId = operation.getOperationId();
//                type = Character.toUpperCase(operationId.charAt(0)) + operationId.substring(1) +
//                        "Response";
//                todo doc comments
//                List<Node> responseDocs = new ArrayList<>();
//                if (response.getDescription() != null && !response.getDescription().isBlank()) {
//                    responseDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                            response.getDescription(), false));
//                }
//                TypeDefinitionNode typeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
//                        (componentSchema, type, responseDocs);
//                GeneratorUtils.updateTypeDefNodeList(type, typeDefinitionNode, typeDefinitionNodeList);
//            }
//        } else if (isArraySchema(schema)) {
//            // TODO: Nested array when response has
//            type = generateReturnTypeForArraySchema(media, schema, isSignature);
//        } else if (getOpenAPIType(schema) != null) {
//            type = convertOpenAPITypeToBallerina(schema);
//        } else
        //add regex to check all JSON types
        if (media.getKey().trim().matches(".*/json") && schema != null) {
            return TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
//            return Optional.of(createSimpleNameReferenceNode(createIdentifierToken("xml")));
//            type = generateCustomTypeDefine("xml", "XML", isSignature);
        } else {
            String type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
            return Optional.of(createSimpleNameReferenceNode(createIdentifierToken(type)));
        }
//        return Optional.of(type);type
    }

//    /**
//     * Get the return data type according to the OAS ArraySchema.
//     */
//    private String generateReturnTypeForArraySchema(Map.Entry<String, MediaType> media, Schema arraySchema,
//                                                    boolean isSignature) throws BallerinaOpenApiException {
//
//        String type;
//        if (arraySchema.getItems().get$ref() != null) {
//            String name = getValidName(extractReferenceType(arraySchema.getItems().get$ref()), true);
//            type = name + "[]";
//            String typeName = name + "Arr";
//            TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null, null,
//                    createIdentifierToken("public type"),
//                    createIdentifierToken(typeName),
//                    createSimpleNameReferenceNode(createIdentifierToken(type)),
//                    createToken(SEMICOLON_TOKEN));
//            // Check already typeDescriptor has same name
//            GeneratorUtils.updateTypeDefNodeList(typeName, typeDefNode, typeDefinitionNodeList);
//            if (!isSignature) {
//                type = typeName;
//            }
//        } else if (getOpenAPIType(arraySchema.getItems()) == null) {
//            if (media.getKey().trim().equals("application/xml")) {
//                type = generateCustomTypeDefine("xml[]", "XMLArr", isSignature);
//            } else if (media.getKey().trim().equals("application/pdf") ||
//                    media.getKey().trim().equals("image/png") ||
//                    media.getKey().trim().equals("application/octet-stream")) {
//                String typeName = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false) + "Arr";
//                String mappedType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
//                type = generateCustomTypeDefine(mappedType, typeName, isSignature);
//            } else {
//                String typeName = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false) + "Arr";
//                String mappedType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false) + "[]";
//                type = generateCustomTypeDefine(mappedType, typeName, isSignature);
//            }
//        } else {
//            String typeName;
//            if (isArraySchema(arraySchema.getItems())) {
//                Schema nestedSchema = arraySchema.getItems();
//                String inlineArrayType = convertOpenAPITypeToBallerina(nestedSchema.getItems());
//                typeName = inlineArrayType + "NestedArr";
//                type = inlineArrayType + "[][]";
//            } else {
//                typeName = convertOpenAPITypeToBallerina(Objects.requireNonNull(arraySchema.getItems())) +
//                        "Arr";
//                type = convertOpenAPITypeToBallerina(arraySchema.getItems()) + "[]";
//            }
//            type = generateCustomTypeDefine(type, getValidName(typeName, true), isSignature);
//        }
//        return type;
//    }

//    /**
//     * Get the return data type according to the OAS ComposedSchemas ex: AllOf, OneOf, AnyOf.
//     */
//    private String generateReturnDataTypeForComposedSchema(Operation operation, String type,
//                                                           Schema composedSchema, boolean isSignature)
//            throws BallerinaOpenApiException {
//
//        if (composedSchema.getOneOf() != null) {
//            // Get oneOfUnionType name
//            String typeName = "OneOf" + getValidName(operation.getOperationId().trim(), true) + "Response";
//            TypeDefinitionNode typeDefNode = ballerinaSchemaGenerator.getTypeDefinitionNode(
//                    composedSchema, typeName, new ArrayList<>());
//            GeneratorUtils.updateTypeDefNodeList(typeName, typeDefNode, typeDefinitionNodeList);
//            type = typeDefNode.typeDescriptor().toString();
//            if (!isSignature) {
//                type = typeName;
//            }
//        } else if (composedSchema.getAllOf() != null) {
//            String recordName = "Compound" + getValidName(operation.getOperationId(), true) +
//                    "Response";
//            TypeDefinitionNode allOfTypeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
//                    (composedSchema, recordName, new ArrayList<>());
//            GeneratorUtils.updateTypeDefNodeList(recordName, allOfTypeDefinitionNode, typeDefinitionNodeList);
//            type = recordName;
//        }
//        return type;
//    }

    /**
     * Handle inline record by generating record with name for response in OAS type ObjectSchema.
     */
    private Optional<TypeDescriptorNode> handleInLineRecordInResponse(Map.Entry<String, MediaType> media, Schema objectSchema) throws BallerinaOpenApiException {

        String ref = objectSchema.get$ref();
        String type;
        if (ref != null) {
            Optional<TypeDescriptorNode> result = TypeHandler.getInstance().getTypeNodeFromOASSchema(objectSchema);
            type = extractReferenceType(ref.trim());
//        } else if (properties != null) {
//            if (properties.isEmpty()) {
//                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
//            } else {
//                List<Node> returnTypeDocs = new ArrayList<>();
//                String description = operation.getResponses().entrySet().iterator().next().getValue().getDescription();
//                if (description != null) {
//                    returnTypeDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                            description, false));
//                }
//                TypeDefinitionNode recordNode = ballerinaSchemaGenerator.getTypeDefinitionNode
//                        (objectSchema, type, returnTypeDocs);
//                GeneratorUtils.updateTypeDefNodeList(type, recordNode, typeDefinitionNodeList);
//            }
        } else {
            type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
        }
        return Optional.of(createSimpleNameReferenceNode(createIdentifierToken(type)));
    }

//    /**
//     * Get the return data type according to the OAS MapSchema type.
//     */
//    private String handleResponseWithMapSchema(Operation operation, Map.Entry<String, MediaType> media,
//                                               Schema mapSchema) throws BallerinaOpenApiException {
//
//        Map<String, Schema> properties = mapSchema.getProperties();
//        String ref = mapSchema.get$ref();
//        String type = getValidName(operation.getOperationId(), true) + "Response";
//
//        if (ref != null) {
//            type = extractReferenceType(ref.trim());
//        } else if (properties != null) {
//            if (properties.isEmpty()) {
//                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
//            } else {
//                List<Node> schemaDocs = new ArrayList<>();
//                String description = operation.getResponses().entrySet().iterator().next().getValue().getDescription();
//                if (description != null) {
//                    schemaDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                            description, false));
//                }
//                TypeDefinitionNode recordNode = ballerinaSchemaGenerator.getTypeDefinitionNode
//                        (mapSchema, type, schemaDocs);
//                GeneratorUtils.updateTypeDefNodeList(type, recordNode, typeDefinitionNodeList);
//            }
//        } else {
//            type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
//        }
//        return type;
//    }

//    /**
//     * Generate Type for datatype that can not bind to the targetType.
//     *
//     * @param type     - Data Type.
//     * @param typeName - Created datType name.
//     * @return return dataType
//     */
//    private String generateCustomTypeDefine(String type, String typeName, boolean isSignature) {
//
//        TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null,
//                null, createIdentifierToken("public type"),
//                createIdentifierToken(typeName),
//                createSimpleNameReferenceNode(createIdentifierToken(type)),
//                createToken(SEMICOLON_TOKEN));
//        GeneratorUtils.updateTypeDefNodeList(typeName, typeDefNode, typeDefinitionNodeList);
//        if (!isSignature) {
//            return typeName;
//        } else {
//            return type;
//        }
//    }
        @Override
        public List<ClientDiagnostic> getDiagnostics() {
            return diagnostics;
        }
}
