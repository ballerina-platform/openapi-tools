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
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import static io.ballerina.openapi.converter.Constants.APPLICATION_PREFIX;
import static io.ballerina.openapi.converter.Constants.JSON_POSTFIX;
import static io.ballerina.openapi.converter.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.converter.Constants.OCTECT_STREAM_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_PREFIX;
import static io.ballerina.openapi.converter.Constants.XML_POSTFIX;
import static io.ballerina.openapi.converter.Constants.X_WWW_FROM_URLENCODED_POSTFIX;

/**
 * OpenAPIRequestBodyMapper provides functionality for converting ballerina payload to OAS request body model.
 *
 * @since 2.0.0
 */
public class OpenAPIRequestBodyMapper {
    private final Components components;
    private final OperationAdaptor operationAdaptor;
    private final SemanticModel semanticModel;
    private final String customMediaType;

    /**
     * This constructor uses to create OpenAPIRequestBodyMapper instance when customMedia type enable.
     *
     * @param components        - OAS Components
     * @param operationAdaptor  - Model of operation
     * @param semanticModel     - Semantic model for given ballerina service
     * @param customMediaType   - custom media type
     */
    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel, String customMediaType) {
        this.components = components;
        this.operationAdaptor = operationAdaptor;
        this.semanticModel = semanticModel;
        this.customMediaType = customMediaType;
    }

    /**
     * This constructor uses to create OpenAPIRequestBodyMapper instance when customMedia type absent.
     *
     * @param components        - OAS Components
     * @param operationAdaptor  - Model of operation
     * @param semanticModel     - Semantic model for given ballerina service
     */
    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel) {
        this(components, operationAdaptor, semanticModel, null);
    }

    /**
     * This method convert ballerina payload node to OAS model {@link io.swagger.v3.oas.models.parameters.RequestBody}.
     *
     * @param payloadNode   - PayloadNode from resource function
     * @param schema        - Current schema map from OAS
     * @param annotation    - Payload annotation details from resource function
     */
    public void handlePayloadAnnotation(RequiredParameterNode payloadNode, Map<String, Schema> schema,
                                        AnnotationNode annotation, Map<String, String> apiDocs) {

        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD)) {
            // Creating request body - required.
            RequestBody bodyParameter = new RequestBody();
            MappingConstructorExpressionNode mapMime = annotation.annotValue().orElse(null);
            SeparatedNodeList<MappingFieldNode> fields = null;
            // Add api doc to request body description
            if (!apiDocs.isEmpty() && payloadNode.paramName().isPresent()
                    && apiDocs.containsKey(payloadNode.paramName().get().text().trim())) {
                bodyParameter.setDescription(apiDocs.get(payloadNode.paramName().get().text().trim()));
            }
            if (mapMime != null) {
                fields = mapMime.fields();
            }
            if (fields != null && (!fields.isEmpty() || fields.size() != 0)) {
                handleMultipleMIMETypes(bodyParameter, fields, payloadNode, schema);
            }  else {
                //TODO : fill with rest of media types
                handleSinglePayloadType(payloadNode, schema, bodyParameter, customMediaType);
            }
        }
    }

    /**
     * This function is used to handle when payload has one mime type.
     *<pre>
     *     resource function post pets(@http:Payload json payload){}
     *</pre>
     */
    private void handleSinglePayloadType(RequiredParameterNode payloadNode, Map<String, Schema> schema,
                                         RequestBody bodyParameter, String customMediaPrefix) {

        String consumes = payloadNode.typeName().toString().trim();
        String mediaTypeString;
        switch (consumes) {
            case Constants.JSON:
                mediaTypeString = customMediaPrefix == null ? MediaType.APPLICATION_JSON :
                        APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX;
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString);
                break;
            case Constants.XML:
                mediaTypeString = customMediaPrefix == null ? MediaType.APPLICATION_XML :
                        APPLICATION_PREFIX + customMediaPrefix + XML_POSTFIX;
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString);
                break;
            case Constants.STRING:
                mediaTypeString = customMediaPrefix == null ? MediaType.TEXT_PLAIN :
                        TEXT_PREFIX + customMediaPrefix + TEXT_POSTFIX;
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString);
                break;
            case Constants.BYTE_ARRAY:
                mediaTypeString = customMediaPrefix == null ? MediaType.APPLICATION_OCTET_STREAM :
                        APPLICATION_PREFIX + customMediaPrefix + OCTECT_STREAM_POSTFIX;
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString);
                break;
            case Constants.MAP_STRING:
                mediaTypeString = customMediaPrefix == null ? MediaType.APPLICATION_FORM_URLENCODED :
                        APPLICATION_PREFIX + customMediaPrefix + X_WWW_FROM_URLENCODED_POSTFIX;
                Schema objectSchema = new ObjectSchema();
                objectSchema.additionalProperties(new StringSchema());
                io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                mediaType.setSchema(objectSchema);
                bodyParameter.setContent(new Content().addMediaType(mediaTypeString, mediaType));
                operationAdaptor.getOperation().setRequestBody(bodyParameter);
                break;
            default:
                Node node = payloadNode.typeName();
                mediaTypeString = customMediaPrefix == null ? MediaType.APPLICATION_JSON :
                        APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX;
                if (node.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    SimpleNameReferenceNode record = (SimpleNameReferenceNode) node;
                    // Creating request body - required.
                    TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(record));
                    String recordName = record.name().toString().trim();
                    handleReferencePayload(typeSymbol, recordName, schema, mediaTypeString, bodyParameter);
                } else if (node instanceof ArrayTypeDescriptorNode) {
                    handleArrayTypePayload(schema, (ArrayTypeDescriptorNode) node, mediaTypeString, bodyParameter);
                } else if (node.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode separateRecord = (QualifiedNameReferenceNode) node;
                    TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(separateRecord));
                    String recordName = ((QualifiedNameReferenceNode) payloadNode.typeName()).identifier().text();
                    handleReferencePayload(typeSymbol, recordName, schema, mediaTypeString, bodyParameter);
                }
                break;
        }
    }

    private TypeSymbol getReferenceTypeSymbol(Optional<Symbol> symbol) {
        return (TypeSymbol) symbol.orElseThrow();
    }

    /**
     * Handle {@link io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode} in ballerina request payload.
     */
    private void handleArrayTypePayload(Map<String, Schema> schema, ArrayTypeDescriptorNode arrayNode,
                                        String mimeType,
                                        RequestBody requestBody) {

        ArraySchema arraySchema = new ArraySchema();
        TypeDescriptorNode typeDescriptorNode = arrayNode.memberTypeDesc();
        // Nested array not allowed
        if (typeDescriptorNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
            //handle record for components
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) typeDescriptorNode;
            TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(referenceNode));
            OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
            componentMapper.createComponentSchema(schema, typeSymbol);
            Schema itemSchema = new Schema();
            arraySchema.setItems(itemSchema.$ref(referenceNode.name().text().trim()));
            io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
            media.setSchema(arraySchema);
            if (requestBody.getContent() != null) {
                Content content = requestBody.getContent();
                content.addMediaType(mimeType, media);
            } else {
                requestBody.setContent(new Content().addMediaType(mimeType, media));
            }
            //  Adding conditional check for http delete operation as it cannot have body
            //  parameter.
            if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
                operationAdaptor.getOperation().setRequestBody(requestBody);
            }
        }
    }

    /**
     * Handle multiple mime type when attached with request payload annotation.
     * ex: <pre> @http:Payload { mediaType:["application/json", "application/xml"] } Pet payload </pre>
     */
    private void handleMultipleMIMETypes(RequestBody bodyParameter,
                                         SeparatedNodeList<MappingFieldNode> fields,
                                         RequiredParameterNode payloadNode, Map<String, Schema> schema) {

        for (MappingFieldNode fieldNode: fields) {
            if (((SpecificFieldNode) fieldNode).fieldName().toString().equals(MEDIA_TYPE)) {
                if (fieldNode.children() != null) {
                    RequestBody requestBody = new RequestBody();
                    SpecificFieldNode field = (SpecificFieldNode) fieldNode;
                    if (field.valueExpr().isPresent()) {
                        ExpressionNode valueNode = field.valueExpr().get();
                        if (valueNode instanceof ListConstructorExpressionNode) {
                            SeparatedNodeList mimeList = ((ListConstructorExpressionNode) valueNode).expressions();
                            if (mimeList.size() != 0) {
                                for (Object mime : mimeList) {
                                    if (mime instanceof BasicLiteralNode) {
                                        createRequestBody(bodyParameter, payloadNode, schema, requestBody,
                                                ((BasicLiteralNode) mime).literalToken().text());
                                    }
                                }
                            }
                        } else {
                            createRequestBody(bodyParameter, payloadNode, schema, requestBody,
                                    valueNode.toString());
                        }
                    }
                }
            }
        }
    }

    private void createRequestBody(RequestBody bodyParameter, RequiredParameterNode payloadNode,
                                   Map<String, Schema> schema, RequestBody requestBody, String mime) {

        String mimeType = mime.replaceAll("\"", "");
        if (payloadNode.typeName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode record = (SimpleNameReferenceNode) payloadNode.typeName();
            TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(record));
            String recordName = record.name().toString().trim();
            handleReferencePayload(typeSymbol, recordName, schema, mimeType, requestBody);
        } else if (payloadNode.typeName() instanceof ArrayTypeDescriptorNode) {
            ArrayTypeDescriptorNode arrayTypeDescriptorNode = (ArrayTypeDescriptorNode) payloadNode.typeName();
            handleArrayTypePayload(schema, arrayTypeDescriptorNode, mimeType, requestBody);
        } else {
            io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
            Schema mimeSchema = ConverterCommonUtils.getOpenApiSchema(mimeType.split("/")[1]
                    .toLowerCase(Locale.ENGLISH));
            media.setSchema(mimeSchema);
            if (bodyParameter.getContent() != null) {
                Content content = bodyParameter.getContent();
                content.addMediaType(mimeType, media);
            } else {
                bodyParameter.setContent(new Content().addMediaType(mimeType, media));
                operationAdaptor.getOperation().setRequestBody(bodyParameter);
            }
        }
    }

    /**
     * Handle record type request payload.
     */
    private void handleReferencePayload(TypeSymbol typeSymbol, String recordName,
                                        Map<String, Schema> schema, String mediaType, RequestBody bodyParameter) {
        //handle record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
        componentMapper.createComponentSchema(schema, typeSymbol);
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        media.setSchema(new Schema().$ref(recordName));
        if (bodyParameter.getContent() != null) {
            Content content = bodyParameter.getContent();
            content.addMediaType(mediaType, media);
        } else {
            bodyParameter.setContent(new Content().addMediaType(mediaType, media));
        }
        //  Adding conditional check for http delete operation as it cannot have body
        //  parameter.
        if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
            operationAdaptor.getOperation().setRequestBody(bodyParameter);
        }
    }

    private void addConsumes(OperationAdaptor operationAdaptor, RequestBody bodyParameter, String applicationType) {
        String type = applicationType.split("/")[1];
        Schema schema = ConverterCommonUtils.getOpenApiSchema(type);
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        mediaType.setSchema(schema);
        bodyParameter.setContent(new Content().addMediaType(applicationType, mediaType));
        operationAdaptor.getOperation().setRequestBody(bodyParameter);
    }
}
