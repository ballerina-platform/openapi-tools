/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

/**
 * OpenAPIRequestBodyMapper provides functionality for converting ballerina payload to OAS request body model.
 */
public class OpenAPIRequestBodyMapper {
    Components components;
    OperationAdaptor operationAdaptor;
    SemanticModel semanticModel;


    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel) {
        this.components = components;
        this.operationAdaptor = operationAdaptor;
        this.semanticModel = semanticModel;
    }

    /**
     * This method convert ballerina payload node to OAS model {@link io.swagger.v3.oas.models.parameters.RequestBody}.
     *
     * @param payloadNode   - PayloadNode from resource function
     * @param schema        - Current schema map from OAS
     * @param annotation    - Payload annotation details from resource function
     */
    public void handlePayloadAnnotation(RequiredParameterNode payloadNode, Map<String, Schema> schema,
                                        AnnotationNode annotation) {

        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD)) {
            // Creating request body - required.
            RequestBody bodyParameter = new RequestBody();
            MappingConstructorExpressionNode mapMime = annotation.annotValue().orElse(null);
            SeparatedNodeList<MappingFieldNode> fields = null;
            if (mapMime != null) {
                fields = mapMime.fields();
            }
            if (fields != null && (!fields.isEmpty() || fields.size() != 0)) {
                handleMultipleMIMETypes(bodyParameter, fields, payloadNode, schema);
            }  else {
                //TODO : fill with rest of media types
                String consumes = "application/" + payloadNode.typeName().toString().trim();
                switch (consumes) {
                    case MediaType.APPLICATION_JSON:
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_JSON);
                        break;
                    case MediaType.APPLICATION_XML:
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_XML);
                        break;
                    case MediaType.TEXT_PLAIN:
                    case "application/string":
                        addConsumes(operationAdaptor, bodyParameter, MediaType.TEXT_PLAIN);
                        break;
                    case "application/byte[]":
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_OCTET_STREAM);
                        break;
                    default:
                        Node node = payloadNode.typeName();
                        if (node instanceof SimpleNameReferenceNode) {
                            SimpleNameReferenceNode record = (SimpleNameReferenceNode) node;
                            handleReferencePayload(record, schema, MediaType.APPLICATION_JSON, new RequestBody());
                        } else if (node instanceof ArrayTypeDescriptorNode) {
                            handleArrayTypePayload(schema, (ArrayTypeDescriptorNode) node,
                                    MediaType.APPLICATION_JSON, new RequestBody());
                        }
                        break;
                }
            }
        }
    }

    /**
     * Handle {@link io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode} in ballerina request payload.
     */
    private void handleArrayTypePayload(Map<String, Schema> schema, ArrayTypeDescriptorNode arrayNode, String mimeType,
                                        RequestBody requestBody) {

        ArraySchema arraySchema = new ArraySchema();
        TypeDescriptorNode typeDescriptorNode = arrayNode.memberTypeDesc();
        // Nested array not allowed
        if (typeDescriptorNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
            //handel record for components
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) typeDescriptorNode;
            Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
            TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
            OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, semanticModel);
            componentMapper.handleRecordNode(referenceNode, schema, typeSymbol);
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
            if (fieldNode.children() != null) {
                ChildNodeList nodeList = fieldNode.children();
                Iterator<Node> itNode = nodeList.iterator();
                while (itNode.hasNext()) {
                    Node nextNode = itNode.next();
                    if (nextNode instanceof ListConstructorExpressionNode) {
                        SeparatedNodeList mimeList = ((ListConstructorExpressionNode) nextNode).expressions();
                        if (mimeList.size() != 0) {
                            RequestBody requestBody = new RequestBody();
                            for (Object mime : mimeList) {
                                if (mime instanceof BasicLiteralNode) {
                                    String mimeType = ((BasicLiteralNode) mime).literalToken().text().
                                            replaceAll("\"", "");
                                    if (payloadNode.typeName() instanceof SimpleNameReferenceNode) {
                                        SimpleNameReferenceNode record =
                                                (SimpleNameReferenceNode) payloadNode.typeName();
                                        handleReferencePayload(record, schema, mimeType, requestBody);
                                    } else if (payloadNode.typeName() instanceof ArrayTypeDescriptorNode) {
                                        ArrayTypeDescriptorNode arrayTypeDescriptorNode =
                                                (ArrayTypeDescriptorNode) payloadNode.typeName();
                                        handleArrayTypePayload(schema, arrayTypeDescriptorNode, mimeType, requestBody);
                                    } else {

                                        io.swagger.v3.oas.models.media.MediaType media =
                                                new io.swagger.v3.oas.models.media.MediaType();
                                        Schema mimeSchema;
                                        if (bodyParameter.getContent() != null) {
                                            media = new io.swagger.v3.oas.models.media.MediaType();
                                            mimeSchema = ConverterUtils.getOpenApiSchema(mimeType.split("/")[1]
                                                    .toLowerCase(Locale.ENGLISH));
                                            media.setSchema(mimeSchema);
                                            Content content = bodyParameter.getContent();
                                            content.addMediaType(mimeType, media);
                                        } else {
                                            mimeSchema = ConverterUtils.getOpenApiSchema(mimeType.split("/")[1].
                                                    toLowerCase(Locale.ENGLISH));
                                            media.setSchema(mimeSchema);
                                            bodyParameter.setContent(new Content().addMediaType(mimeType,
                                                    media));
                                            operationAdaptor.getOperation().setRequestBody(bodyParameter);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle record type request payload.
     */
    private void handleReferencePayload(SimpleNameReferenceNode referenceNode,
                                        Map<String, Schema> schema, String mediaType, RequestBody bodyParameter) {

        // Creating request body - required.
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handel record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, semanticModel);
        componentMapper.handleRecordNode(referenceNode, schema, typeSymbol);

        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
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
        Schema schema = ConverterUtils.getOpenApiSchema(type);
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        mediaType.setSchema(schema);
        bodyParameter.setContent(new Content().addMediaType(applicationType, mediaType));
        operationAdaptor.getOperation().setRequestBody(bodyParameter);
    }

}
