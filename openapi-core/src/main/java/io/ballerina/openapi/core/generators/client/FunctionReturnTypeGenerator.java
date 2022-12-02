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
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.GeneratorConstants.DEFAULT_RETURN;
import static io.ballerina.openapi.core.GeneratorConstants.ERROR;
import static io.ballerina.openapi.core.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.GeneratorUtils.isValidSchemaName;

/**
 * This util class for maintain the operation response with ballerina return type.
 *
 * @since 1.3.0
 */
public class FunctionReturnTypeGenerator {
    private OpenAPI openAPI;
    private BallerinaTypesGenerator ballerinaSchemaGenerator;
    private Set<TypeDefinitionNode> typeDefinitionNodeList = new LinkedHashSet<>();

    public FunctionReturnTypeGenerator() {

    }

    public FunctionReturnTypeGenerator(OpenAPI openAPI, BallerinaTypesGenerator ballerinaSchemaGenerator,
                                       Set<TypeDefinitionNode> typeDefinitionNodeList) {

        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
    }

    /**
     * Get return type of the remote function.
     *
     * @param operation swagger operation.
     * @return string with return type.
     * @throws BallerinaOpenApiException - throws exception if creating return type fails.
     */
    public String getReturnType(Operation operation, boolean isSignature) throws BallerinaOpenApiException {
        //TODO: Handle multiple media-type
        Set<String> returnTypes = new HashSet<>();
        boolean noContentResponseFound = false;
        if (operation.getResponses() != null) {
            ApiResponses responses = operation.getResponses();
            for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
                String statusCode = entry.getKey();
                ApiResponse response = entry.getValue();
                if (statusCode.startsWith("2")) {
                    Content content = response.getContent();
                    if (content != null && content.size() > 0) {
                        Set<Map.Entry<String, MediaType>> mediaTypes = content.entrySet();
                        for (Map.Entry<String, MediaType> media : mediaTypes) {
                            String type = "";
                            if (media.getValue().getSchema() != null) {
                                Schema schema = media.getValue().getSchema();
                                type = getDataType(operation, isSignature, response, media, type, schema);
                            } else {
                                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
                            }
                            returnTypes.add(type);
                            // Currently support for first media type
                            break;
                        }
                    } else {
                        noContentResponseFound = true;
                    }
                }
            }
        }
        if (returnTypes.size() > 0) {
            StringBuilder finalReturnType = new StringBuilder();
            finalReturnType.append(String.join(PIPE_TOKEN.stringValue(), returnTypes));
            finalReturnType.append(PIPE_TOKEN.stringValue());
            finalReturnType.append(ERROR);
            if (noContentResponseFound) {
                finalReturnType.append(NILLABLE);
            }
            return finalReturnType.toString();
        } else {
            return DEFAULT_RETURN;
        }
    }

    /**
     * Get return data type by traversing OAS schemas.
     */
    private String getDataType(Operation operation, boolean isSignature, ApiResponse response,
                               Map.Entry<String, MediaType> media, String type, Schema schema)
            throws BallerinaOpenApiException {

        if (schema instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schema;
            type = generateReturnDataTypeForComposedSchema(operation, type, composedSchema, isSignature);
        } else if (schema instanceof ObjectSchema) {
            ObjectSchema objectSchema = (ObjectSchema) schema;
            type = handleInLineRecordInResponse(operation, media, objectSchema);
        } else if (schema instanceof MapSchema) {
            MapSchema mapSchema = (MapSchema) schema;
            type = handleResponseWithMapSchema(operation, media, mapSchema);
        } else if (schema.get$ref() != null) {
            type = getValidName(extractReferenceType(schema.get$ref()), true);
            Schema componentSchema = openAPI.getComponents().getSchemas().get(type);
            if (!isValidSchemaName(type)) {
                String operationId = operation.getOperationId();
                type = Character.toUpperCase(operationId.charAt(0)) + operationId.substring(1) +
                        "Response";
                List<Node> responseDocs = new ArrayList<>();
                if (response.getDescription() != null && !response.getDescription().isBlank()) {
                    responseDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                            response.getDescription(), false));
                }
                TypeDefinitionNode typeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
                        (componentSchema, type, responseDocs);
                typeDefinitionNodeList.add(typeDefinitionNode);
            }
        } else if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            // TODO: Nested array when response has
            type = generateReturnTypeForArraySchema(media, arraySchema, isSignature);
        } else if (schema.getType() != null) {
            type = convertOpenAPITypeToBallerina(schema.getType());
        } else if (media.getKey().trim().equals("application/xml")) {
            type = generateCustomTypeDefine("xml", "XML", isSignature);
        } else {
            type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
        }
        return type;
    }

    /**
     * Get the return data type according to the OAS ArraySchema.
     */
    private String generateReturnTypeForArraySchema(Map.Entry<String, MediaType> media, ArraySchema arraySchema,
                                                    boolean isSignature) throws BallerinaOpenApiException {

        String type;
        if (arraySchema.getItems().get$ref() != null) {
            String name = getValidName(extractReferenceType(arraySchema.getItems().get$ref()), true);
            type = name + "[]";
            String typeName = name + "Arr";
            TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null, null,
                    createIdentifierToken("public type"),
                    createIdentifierToken(typeName),
                    createSimpleNameReferenceNode(createIdentifierToken(type)),
                    createToken(SEMICOLON_TOKEN));
            // Check already typeDescriptor has same name
            typeDefinitionNodeList.add(typeDefNode);
            if (!isSignature) {
                type = typeName;
            }
        } else if (arraySchema.getItems().getType() == null) {
            if (media.getKey().trim().equals("application/xml")) {
                type = generateCustomTypeDefine("xml[]", "XMLArr", isSignature);
            } else if (media.getKey().trim().equals("application/pdf") ||
                    media.getKey().trim().equals("image/png") ||
                    media.getKey().trim().equals("application/octet-stream")) {
                String typeName = GeneratorUtils.getBallerinaMediaType(media.getKey().trim()) + "Arr";
                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
                type = generateCustomTypeDefine(type, typeName, isSignature);
            } else {
                String typeName = GeneratorUtils.getBallerinaMediaType(media.getKey().trim()) + "Arr";
                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim()) + "[]";
                type = generateCustomTypeDefine(type, typeName, isSignature);
            }
        } else {
            String typeName;
            if (arraySchema.getItems() instanceof ArraySchema) {
                Schema nestedSchema = arraySchema.getItems();
                ArraySchema nestedArraySchema = (ArraySchema) nestedSchema;
                String inlineArrayType = convertOpenAPITypeToBallerina(nestedArraySchema.getItems().getType());
                typeName = inlineArrayType + "NestedArr";
                type = inlineArrayType + "[][]";
            } else {
                typeName = convertOpenAPITypeToBallerina(Objects.requireNonNull(arraySchema.getItems()).getType()) +
                        "Arr";
                type = convertOpenAPITypeToBallerina(arraySchema.getItems().getType()) + "[]";
            }
            type = generateCustomTypeDefine(type, getValidName(typeName, true), isSignature);
        }
        return type;
    }

    /**
     * Get the return data type according to the OAS ComposedSchemas ex: AllOf, OneOf, AnyOf.
     */
    private String generateReturnDataTypeForComposedSchema(Operation operation, String type,
                                                           ComposedSchema composedSchema, boolean isSignature)
            throws BallerinaOpenApiException {

        if (composedSchema.getOneOf() != null) {
            // Get oneOfUnionType name
            String typeName = "OneOf" + getValidName(operation.getOperationId().trim(), true) + "Response";
            TypeDefinitionNode typeDefNode = ballerinaSchemaGenerator.getTypeDefinitionNode(
                    composedSchema, typeName, new ArrayList<>());
            typeDefinitionNodeList.add(typeDefNode);
            type = typeDefNode.typeDescriptor().toString();
            if (!isSignature) {
                type = typeName;
            }
        } else if (composedSchema.getAllOf() != null) {
            String recordName = "Compound" + getValidName(operation.getOperationId(), true) +
                    "Response";
            TypeDefinitionNode allOfTypeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
                    (composedSchema, recordName, new ArrayList<>());
            typeDefinitionNodeList.add(allOfTypeDefinitionNode);
            type = recordName;
        }
        return type;
    }

    /**
     * Handle inline record by generating record with name for response in OAS type ObjectSchema.
     */
    private String handleInLineRecordInResponse(Operation operation, Map.Entry<String, MediaType> media,
                                                ObjectSchema objectSchema)
            throws BallerinaOpenApiException {

        Map<String, Schema> properties = objectSchema.getProperties();
        String ref = objectSchema.get$ref();
        String type = getValidName(operation.getOperationId(), true) + "Response";

        if (ref != null) {
            type = extractReferenceType(ref.trim());
        } else if (properties != null) {
            if (properties.isEmpty()) {
                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
            } else {
                List<Node> returnTypeDocs = new ArrayList<>();
                String description = operation.getResponses().entrySet().iterator().next().getValue().getDescription();
                if (description != null) {
                    returnTypeDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                            description, false));
                }
                TypeDefinitionNode recordNode = ballerinaSchemaGenerator.getTypeDefinitionNode
                        (objectSchema, type, returnTypeDocs);
                typeDefinitionNodeList.add(recordNode);
            }
        } else {
            type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
        }
        return type;
    }

    /**
     * Get the return data type according to the OAS MapSchema type.
     */
    private String handleResponseWithMapSchema(Operation operation, Map.Entry<String, MediaType> media,
                                               MapSchema mapSchema) throws BallerinaOpenApiException {

        Map<String, Schema> properties = mapSchema.getProperties();
        String ref = mapSchema.get$ref();
        String type = getValidName(operation.getOperationId(), true) + "Response";

        if (ref != null) {
            type = extractReferenceType(ref.trim());
        } else if (properties != null) {
            if (properties.isEmpty()) {
                type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
            } else {
                List<Node> schemaDocs = new ArrayList<>();
                String description = operation.getResponses().entrySet().iterator().next().getValue().getDescription();
                if (description != null) {
                    schemaDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                            description, false));
                }
                TypeDefinitionNode recordNode = ballerinaSchemaGenerator.getTypeDefinitionNode
                        (mapSchema, type, schemaDocs);
                typeDefinitionNodeList.add(recordNode);
            }
        } else {
            type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim());
        }
        return type;
    }

    /**
     * Generate Type for datatype that can not bind to the targetType.
     *
     * @param type     - Data Type.
     * @param typeName - Created datType name.
     * @return return dataType
     */
    private String generateCustomTypeDefine(String type, String typeName, boolean isSignature) {

        TypeDefinitionNode typeDefNode = createTypeDefinitionNode(null,
                null, createIdentifierToken("public type"),
                createIdentifierToken(typeName),
                createSimpleNameReferenceNode(createIdentifierToken(type)),
                createToken(SEMICOLON_TOKEN));
        typeDefinitionNodeList.add(typeDefNode);
        if (!isSignature) {
            return typeName;
        } else {
            return type;
        }
    }

}
