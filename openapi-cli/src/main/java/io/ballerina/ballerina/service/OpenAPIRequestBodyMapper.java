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

package io.ballerina.ballerina.service;

import io.ballerina.ballerina.Constants;
import io.ballerina.ballerina.ConverterUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

/**
 * OpenAPIRequestBodyMapper provides functionality for converting ballerina payload to OAS request body model.
 */
public class OpenAPIRequestBodyMapper {
    ConverterUtils converterUtils = new ConverterUtils();
    Components components;
    OperationAdaptor operationAdaptor;
    SemanticModel semanticModel;


    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel) {
        this.components = components;
        this.operationAdaptor = operationAdaptor;
        this.semanticModel = semanticModel;
    }

    public void handlePayloadAnnotation(RequiredParameterNode expr, RequiredParameterNode queryParam, Map<String,
            Schema> schema, AnnotationNode annotation) {

        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD)) {
            // Creating request body - required.
            RequestBody bodyParameter = new RequestBody();
            MappingConstructorExpressionNode mapMime = annotation.annotValue().orElseThrow();
            SeparatedNodeList<MappingFieldNode> fields = mapMime.fields();
            if (!fields.isEmpty() || fields.size() != 0) {
                handleMultipleMIMETypes(bodyParameter, fields, expr, queryParam, schema);
            }  else {
                String consumes = "application/" + expr.typeName().toString().trim();
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
                        Node node = queryParam.typeName();
                        if (node instanceof SimpleNameReferenceNode) {
                            SimpleNameReferenceNode record = (SimpleNameReferenceNode) node;
                            handleReferencePayload(operationAdaptor, record, schema,
                                    MediaType.APPLICATION_JSON, new RequestBody());
                        }
                        break;
                }
            }
        }
    }

    private void handleMultipleMIMETypes(RequestBody bodyParameter,
                                         SeparatedNodeList<MappingFieldNode> fields, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Schema> schema) {

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
                                    if (queryParam.typeName() instanceof SimpleNameReferenceNode) {
                                        SimpleNameReferenceNode record =
                                                (SimpleNameReferenceNode) queryParam.typeName();
                                        handleReferencePayload(operationAdaptor, record, schema, mimeType,
                                                requestBody);
                                    } else {

                                        io.swagger.v3.oas.models.media.MediaType media =
                                                new io.swagger.v3.oas.models.media.MediaType();
                                        Schema mimeSchema;
                                        if (bodyParameter.getContent() != null) {
                                            media = new io.swagger.v3.oas.models.media.MediaType();
                                            mimeSchema = converterUtils.getOpenApiSchema(mimeType.split("/")[1]
                                                    .toLowerCase(Locale.ENGLISH));
                                            media.setSchema(mimeSchema);
                                            Content content = bodyParameter.getContent();
                                            content.addMediaType(mimeType, media);
                                        } else {
                                            mimeSchema = converterUtils.getOpenApiSchema(mimeType.split("/")[1].
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

    private void handleReferencePayload(OperationAdaptor operationAdaptor, SimpleNameReferenceNode recordNode,
                                        Map<String, Schema> schema, String mediaType,
                                        @Nullable RequestBody bodyParameter) {

        // Creating request body - required.
        SimpleNameReferenceNode referenceNode = recordNode;
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handel record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, semanticModel);
        componentMapper.handleRecordPayload(recordNode, schema, typeSymbol);

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
        Schema schema = converterUtils.getOpenApiSchema(type);
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        mediaType.setSchema(schema);
        bodyParameter.setContent(new Content().addMediaType(applicationType, mediaType));
        operationAdaptor.getOperation().setRequestBody(bodyParameter);
    }

}
