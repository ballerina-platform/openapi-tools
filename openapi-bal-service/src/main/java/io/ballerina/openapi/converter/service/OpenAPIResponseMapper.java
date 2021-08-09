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

package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_FIELD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_REFERENCE;
import static io.ballerina.openapi.converter.Constants.HTTP;
import static io.ballerina.openapi.converter.Constants.HTTP_200;
import static io.ballerina.openapi.converter.Constants.HTTP_200_DESCRIPTION;
import static io.ballerina.openapi.converter.Constants.HTTP_CODES;

/**
 * This class uses to map the Ballerina return details to the OAS response.
 *
 * @since 2.0.0
 */
public class OpenAPIResponseMapper {
    private final SemanticModel semanticModel;
    private Components components = new Components();

    public OpenAPIResponseMapper(SemanticModel semanticModel, Components components) {
        this.semanticModel = semanticModel;
        this.components = components;
    }

    /**
     * This function for mapping the function return type details with OAS response schema.
     *
     * @param resource          FunctionDefinitionNode for relevant resource function.
     * @param operationAdaptor  OperationAdaptor model for holding operation details specific to HTTP operation.
     * @throws OpenApiConverterException    when the mapping process fail.
     */
    public void parseResponsesAnnotationAttachment(FunctionDefinitionNode resource,
                                                   OperationAdaptor operationAdaptor) throws OpenApiConverterException {

        FunctionSignatureNode functionSignatureNode = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptorNode = functionSignatureNode.returnTypeDesc();
        io.swagger.v3.oas.models.Operation operation = operationAdaptor.getOperation();
        ApiResponses apiResponses = new ApiResponses();
        if (returnTypeDescriptorNode.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptorNode.orElseThrow();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Node typeNode = returnNode.type();
            if (!annotations.isEmpty()) {
               //TODO annotation handle
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.description("Accepted");
                apiResponses.put("202", apiResponse);

            } else {
                handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, typeNode);
            }
        } else {
            // When the return type is not mention in the resource function.
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.description("Accepted");
            apiResponses.put("202", apiResponse);
        }
        operation.setResponses(apiResponses);
    }

    /**
     * This methods for handle the return type has non annotations.
     *
     * @return {@link io.swagger.v3.oas.models.responses.ApiResponses} for operation.
     */
    private ApiResponses handleResponseWithoutAnnotationType(OperationAdaptor operationAdaptor,
                                                             ApiResponses apiResponses,
                                                             Node typeNode) throws OpenApiConverterException {

        ApiResponse apiResponse = new ApiResponse();
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();

        switch (typeNode.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                if (qNode.modulePrefix().toString().trim().equals(HTTP)) {
                    String code = generateApiResponseCode(qNode.identifier().toString().trim());
                    apiResponse.description(qNode.identifier().toString().trim());
                    apiResponses.put(code, apiResponse);
                    return apiResponses;
                }
                break;
            case INT_TYPE_DESC:
            case STRING_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
                String type =  typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
                Schema schema = ConverterCommonUtils.getOpenApiSchema(type);
                String media = convertBallerinaMIMEToOASMIMETypes(type);
                mediaType.setSchema(schema);
                apiResponse.description(HTTP_200_DESCRIPTION);
                apiResponse.content(new Content().addMediaType(media, mediaType));
                apiResponses.put(HTTP_200, apiResponse);
                return apiResponses;
            case JSON_TYPE_DESC:
                mediaType.setSchema(new ObjectSchema());
                apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, mediaType));
                apiResponse.description(HTTP_200_DESCRIPTION);
                apiResponses.put(HTTP_200, apiResponse);
                return apiResponses;
            case XML_TYPE_DESC:
                mediaType.setSchema(new ObjectSchema());
                apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_XML, mediaType));
                apiResponse.description(HTTP_200_DESCRIPTION);
                apiResponses.put(HTTP_200, apiResponse);
                return apiResponses;
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode recordNode  = (SimpleNameReferenceNode) typeNode;
                Map<String, Schema> schemas = components.getSchemas();
                handleReferenceInResponse(operationAdaptor, recordNode, schemas, apiResponses);
                return apiResponses;
            case UNION_TYPE_DESC:
                return getAPIResponsesForReturnUnionType(operationAdaptor, apiResponses,
                        (UnionTypeDescriptorNode) typeNode);
            case RECORD_TYPE_DESC:
                return getApiResponsesForInlineRecords(operationAdaptor, apiResponses,
                        (RecordTypeDescriptorNode) typeNode, apiResponse, mediaType);
            case ARRAY_TYPE_DESC:
                return getApiResponsesForArrayTypes(operationAdaptor, apiResponses,
                        (ArrayTypeDescriptorNode) typeNode, apiResponse, mediaType);
            case ERROR_TYPE_DESC:
                // Return type is not given as error or error? in ballerina it will generate 500 response.
                apiResponse.description("Found unexpected output");
                apiResponses.put("500", apiResponse);
                return apiResponses;
            case OPTIONAL_TYPE_DESC:
                return handleResponseWithoutAnnotationType(operationAdaptor, apiResponses,
                        ((OptionalTypeDescriptorNode) typeNode).typeDescriptor());
            default:
                throw new OpenApiConverterException("Unexpected value: " + typeNode.kind());
        }
        return apiResponses;
    }

    /**
     * Handle return has array types.
     */
    private ApiResponses getApiResponsesForArrayTypes(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                      ArrayTypeDescriptorNode typeNode, ApiResponse apiResponse,
                                                      io.swagger.v3.oas.models.media.MediaType mediaType)
            throws OpenApiConverterException {

        ArrayTypeDescriptorNode array = typeNode;
        Map<String, Schema> schemas02 = components.getSchemas();
        if (array.memberTypeDesc().kind().equals(SIMPLE_NAME_REFERENCE)) {
            handleReferenceInResponse(operationAdaptor, (SimpleNameReferenceNode) array.memberTypeDesc(),
                    schemas02, apiResponses);
        } else {
            ArraySchema arraySchema = new ArraySchema();
            String type02 = array.memberTypeDesc().kind().toString().trim().split("_")[0].
                            toLowerCase(Locale.ENGLISH);
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(type02);
            String mimeType = convertBallerinaMIMEToOASMIMETypes(type02);
            arraySchema.setItems(openApiSchema);
            mediaType.setSchema(arraySchema);
            apiResponse.description(HTTP_200_DESCRIPTION);
            apiResponse.content(new Content().addMediaType(mimeType, mediaType));
            apiResponses.put(HTTP_200, apiResponse);
        }
        return apiResponses;
    }

    /**
     * Handle response has inline record as return type.
     */
    private ApiResponses getApiResponsesForInlineRecords(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                         RecordTypeDescriptorNode typeNode, ApiResponse apiResponse,
                                                         io.swagger.v3.oas.models.media.MediaType mediaType)
            throws OpenApiConverterException {

        NodeList<Node> fields = typeNode.fields();
        String identifier = HTTP_200;
        Schema inlineSchema = new Schema();
        String description = HTTP_200_DESCRIPTION;
        String mediaTypeResponse = MediaType.APPLICATION_JSON;
        boolean ishttpTypeInclusion = false;
        Map<String , Schema> properties = new HashMap<>();
        if (fields.stream().anyMatch(module -> module.kind().equals(TYPE_REFERENCE))) {
            ishttpTypeInclusion = true;
        }
        // 1- scenarios returns record {| *http:Ok; Person body;|}
        // 2- scenarios returns record {| *http:Ok; string body;|}
        // 3- scenarios returns record {| int id; string body;|} - done
        // 4- scenarios returns record {| int id; Person body;|} - done

        for (Node field: fields) {
            // TODO Handle http typeInclusion, check it comes from http module
            if (field.kind().equals(TYPE_REFERENCE)) {
                TypeReferenceNode typeFieldNode = (TypeReferenceNode) field;
                if (typeFieldNode.typeName().kind() == QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode identifierNode = (QualifiedNameReferenceNode) typeFieldNode.typeName();
                    identifier = generateApiResponseCode(identifierNode.identifier().text().trim());
                    description = identifierNode.identifier().text().trim();
                    apiResponse.description(identifierNode.identifier().text().trim());
                }
            }
            if (field.kind().equals(RECORD_FIELD)) {
                RecordFieldNode recordField = (RecordFieldNode) field;
                Node type01 = recordField.typeName();
                if (recordField.typeName().kind() == SIMPLE_NAME_REFERENCE) {
                    Map<String, Schema> componentsSchemas = components.getSchemas();
                    SimpleNameReferenceNode nameRefNode =  (SimpleNameReferenceNode) type01;
                    handleReferenceInResponse(operationAdaptor, nameRefNode, componentsSchemas, apiResponses);
                    Schema referenceSchema = new Schema();
                    referenceSchema.set$ref(recordField.typeName().toString().trim());
                    properties.put(recordField.fieldName().text(), referenceSchema);
                } else {
                    //TODO array fields handling
                    mediaTypeResponse = convertBallerinaMIMEToOASMIMETypes(recordField.typeName().toString().trim());
                    Schema propertySchema = ConverterCommonUtils.getOpenApiSchema(
                            recordField.typeName().toString().trim());
                    properties.put(recordField.fieldName().text(), propertySchema);
                }
            }
        }
        if (!ishttpTypeInclusion) {
            inlineSchema = new ObjectSchema();
            inlineSchema.setProperties(properties);
            mediaTypeResponse = MediaType.APPLICATION_JSON;
        }
        mediaType.setSchema(inlineSchema);
        apiResponse.description(description);
        apiResponse.content(new Content().addMediaType(mediaTypeResponse, mediaType));
        apiResponses.put(identifier, apiResponse);
        return apiResponses;
    }

    /**
     * Handle the response has union type.
     *
     * <pre>
     *     resource function post reservation(@http:Payload Reservation reservation)
     *                 returns ReservationCreated|ReservationConflict {
     *         ReservationCreated created = createReservation(reservation);
     *         return created;
     *     }
     * </pre>
     */
    private ApiResponses getAPIResponsesForReturnUnionType(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                           UnionTypeDescriptorNode typeNode)
            throws OpenApiConverterException {

        TypeDescriptorNode rightNode = typeNode.rightTypeDesc();
        TypeDescriptorNode leftNode = typeNode.leftTypeDesc();
        // Handle leftNode because it is main node
        apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, leftNode);
        // Handle rest of the union type
        if (rightNode instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
            while (traversRightNode.rightTypeDesc() != null) {
                if (leftNode.kind() == QUALIFIED_NAME_REFERENCE) {
                    leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
                    apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, leftNode);
                }
            }
        } else {
            apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, rightNode);
        }
        return apiResponses;
    }

    /**
     * Convert ballerina MIME types to OAS MIME types.
     */

    private String convertBallerinaMIMEToOASMIMETypes(String type) {
        switch (type) {
            case Constants.JSON:
                return MediaType.APPLICATION_JSON;
            case Constants.XML:
                return MediaType.APPLICATION_XML;
            case Constants.BYTE_ARRAY:
                return MediaType.APPLICATION_OCTET_STREAM;
            case Constants.STRING:
            default:
                return MediaType.TEXT_PLAIN;
        }
    }

    /**
     * Get related http status code.
     */
    private String generateApiResponseCode(String identifier) throws OpenApiConverterException {
        if (HTTP_CODES.containsKey(identifier)) {
            return HTTP_CODES.get(identifier);
        } else {
            throw new OpenApiConverterException("No related status code for :" + identifier);
        }
    }

    private void handleReferenceInResponse(OperationAdaptor operationAdaptor, SimpleNameReferenceNode referenceNode,
                                           Map<String, Schema> schema, ApiResponses apiResponses)
            throws OpenApiConverterException {
        ApiResponse apiResponse = new ApiResponse();
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handel record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        // Check typeInclusion is related to the http status code
        if (referenceNode.parent().kind().equals(ARRAY_TYPE_DESC)) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(new Schema().$ref(referenceNode.name().toString().trim()));
            media.setSchema(arraySchema);
            apiResponse.description(HTTP_200_DESCRIPTION);
            apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
            apiResponses.put(HTTP_200, apiResponse);

        } else if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            if (typeReferenceTypeSymbol.typeDescriptor().typeKind().getName().equals("record")) {
                RecordTypeSymbol returnRecord = (RecordTypeSymbol) typeReferenceTypeSymbol.typeDescriptor();
                List<TypeSymbol> typeInclusions = returnRecord.typeInclusions();
                if (!typeInclusions.isEmpty()) {
                    handleRecordHasHttpTypeInclusionField(referenceNode, schema, apiResponses, apiResponse, typeSymbol,
                            componentMapper, media, returnRecord, typeInclusions);
                } else {
                    componentMapper.handleRecordNode(referenceNode, schema, typeSymbol);
                    media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
                    apiResponse.description(HTTP_200_DESCRIPTION);
                    apiResponses.put(HTTP_200, apiResponse);
                }
            }
        }
        operationAdaptor.getOperation().setResponses(apiResponses);
    }

    private void handleRecordHasHttpTypeInclusionField(SimpleNameReferenceNode referenceNode,
                                                       Map<String, Schema> schema, ApiResponses apiResponses,
                                                       ApiResponse apiResponse, TypeSymbol typeSymbol,
                                                       OpenAPIComponentMapper componentMapper,
                                                       io.swagger.v3.oas.models.media.MediaType media,
                                                       RecordTypeSymbol returnRecord, List<TypeSymbol> typeInclusions)
            throws OpenApiConverterException {

        boolean isHttpModule = false;
        for (TypeSymbol typeInSymbol : typeInclusions) {
            if (HTTP.equals(typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                isHttpModule = true;
                String code = generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                Map<String, RecordFieldSymbol> fieldsOfRecord = returnRecord.fieldDescriptors();
                // Handle the content of the response
                RecordFieldSymbol body = fieldsOfRecord.get("body");
                switch (body.typeDescriptor().typeKind()) {
                    case TYPE_REFERENCE:
                        componentMapper.handleRecordNode(referenceNode, schema,
                                (TypeReferenceTypeSymbol) body.typeDescriptor());
                        media.setSchema(new Schema().$ref(body.typeDescriptor().getName().orElseThrow().trim()));
                        apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
                        apiResponses.put(code, apiResponse);
                        break;
                    default:
                        apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                        apiResponses.put(code, apiResponse);
                        break;
                }
            }
        }
        if (!isHttpModule) {
            componentMapper.handleRecordNode(referenceNode, schema, typeSymbol);
            media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
            apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
            apiResponse.description(HTTP_200_DESCRIPTION);
            apiResponses.put(HTTP_200, apiResponse);
        }
    }
}
