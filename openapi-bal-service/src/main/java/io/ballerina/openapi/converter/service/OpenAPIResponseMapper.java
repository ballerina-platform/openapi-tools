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
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

/**
 * This class uses to map the Ballerina return details to the OAS response.
 *
 * @since 2.0.0
 */
public class OpenAPIResponseMapper {
    private SemanticModel semanticModel;
    private Components components = new Components();

    public OpenAPIResponseMapper(SemanticModel semanticModel, Components components) {
        this.semanticModel = semanticModel;
        this.components = components;
    }

    public void parseResponsesAnnotationAttachment(FunctionDefinitionNode resource,
                                                   OperationAdaptor operationAdaptor) {

        FunctionSignatureNode functionSignatureNode = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptorNode = functionSignatureNode.returnTypeDesc();
        io.swagger.v3.oas.models.Operation operation = operationAdaptor.getOperation();
        ApiResponses apiResponses = new ApiResponses();
        if (returnTypeDescriptorNode.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptorNode.orElseThrow();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Node typeNode = returnNode.type();
            if (!annotations.isEmpty()) {
                // Handle in further implementation
                // Temporary solution
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.description("Accepted");
                apiResponses.put("202", apiResponse);

            } else {
                if (typeNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                    QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                    if (qNode.modulePrefix().toString().trim().equals("http")) {
                        ApiResponse apiResponse = new ApiResponse();
                        String code = generateApiResponseCode(qNode.identifier().toString().trim());
                        apiResponse.description(qNode.identifier().toString().trim());
                        apiResponses.put(code, apiResponse);
                    }
                } else if (typeNode instanceof BuiltinSimpleNameReferenceNode) {
                    ApiResponse apiResponse = new ApiResponse();
                    String type =  typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
                    Schema schema = ConverterUtils.getOpenApiSchema(type);
                    io.swagger.v3.oas.models.media.MediaType mediaType =
                            new io.swagger.v3.oas.models.media.MediaType();
                    String media = generateMIMETypeForBallerinaType(type);
                    mediaType.setSchema(schema);
                    apiResponse.description("Successful");
                    apiResponse.content(new Content().addMediaType(media, mediaType));
                    apiResponses.put("200", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.JSON_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                    Schema schema = new ObjectSchema();
                    mediaType.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, mediaType));
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.XML_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                    Schema schema = new ObjectSchema();
                    mediaType.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_XML, mediaType));
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                } else if (typeNode instanceof SimpleNameReferenceNode) {
                    SimpleNameReferenceNode recordNode  = (SimpleNameReferenceNode) typeNode;
                    Map<String, Schema> schema = components.getSchemas();
                    handleReferenceInResponse(operationAdaptor, recordNode, schema, apiResponses);
                } else if (typeNode.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
                    //line-No 304 - 330 commented due to handle advance response scenario in further implementation.
                    //check until right typeDec empty
//                    UnionTypeDescriptorNode unionNode = (UnionTypeDescriptorNode) typeNode;
//                    TypeDescriptorNode rightNode = unionNode.rightTypeDesc();
//                    TypeDescriptorNode leftNode = unionNode.leftTypeDesc();

//                    if (rightNode instanceof UnionTypeDescriptorNode) {
//                        UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
//                        while (traversRightNode.rightTypeDesc() != null) {
//                            if (leftNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
//                                String identifier = ((QualifiedNameReferenceNode)
//                                leftNode).identifier().text().trim();
//                                String code = generateApiResponseCode(identifier);
//                                ApiResponse apiResponse = new ApiResponse();
//                                apiResponse.description(identifier);
//                                apiResponses.put(code, apiResponse);
//// casting exception return.
////                                rightNode = ((UnionTypeDescriptorNode) rightNode).rightTypeDesc();
////                                leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
//                            }
//                        }
//                    }
//                    if (rightNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
//                        String identifier = ((QualifiedNameReferenceNode) rightNode).identifier().text().trim();
//                        String code = generateApiResponseCode(identifier);
//                        ApiResponse apiResponse = new ApiResponse();
//                        apiResponse.description(identifier);
//                        apiResponses.put(code, apiResponse);
//                    }
                    //Return type is union type  generate 200 response with text/plain
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Ok");
                    apiResponse.content(new Content().addMediaType("text/plain",
                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    apiResponses.put("200", apiResponse);

                } else if (typeNode.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    RecordTypeDescriptorNode record  = (RecordTypeDescriptorNode) typeNode;
                    NodeList<Node> fields = record.fields();
                    String identifier = null;
                    Schema schema = null;
                    String mediaType = null;
                    for (Node field: fields) {
                        if (field.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                            TypeReferenceNode typeFieldNode = (TypeReferenceNode) field;
                            if (typeFieldNode.typeName().kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                                QualifiedNameReferenceNode identifierNode =
                                        (QualifiedNameReferenceNode) typeFieldNode.typeName();
                                identifier = generateApiResponseCode(identifierNode.identifier().text().trim());
                                apiResponse.description(identifierNode.identifier().text().trim());
                            }
                        }
                        if (field.kind().equals(SyntaxKind.RECORD_FIELD)) {
                            RecordFieldNode recordField = (RecordFieldNode) field;
                            Node type = recordField.typeName();
                            String nodeType = type.toString().toLowerCase(Locale.ENGLISH).trim();
                            mediaType = generateMIMETypeForBallerinaType(nodeType);
                            if (type.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                                Map<String, Schema> schemas = components.getSchemas();
                                mediaType = MediaType.APPLICATION_JSON;
                                SimpleNameReferenceNode nameRefNode =  (SimpleNameReferenceNode) type;
                                handleReferenceInResponse(operationAdaptor, nameRefNode, schemas, apiResponses);
                            } else {
                                schema = ConverterUtils.getOpenApiSchema(nodeType);
                            }
                        }
                    }
                    io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
                    media.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(mediaType, media));
                    apiResponses.put(identifier, apiResponse);

                } else if (typeNode.toString().trim().equals("error") || typeNode.toString().trim().equals("error?")) {
                    //Return type is not given as error or error? in ballerina it will generate 500 response.
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Found unexpected output");
                    apiResponse.content(new Content().addMediaType("text/plain",
                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    apiResponses.put("500", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
                    ArrayTypeDescriptorNode array = (ArrayTypeDescriptorNode) typeNode;
                    Map<String, Schema> schemas = components.getSchemas();
                    if (array.memberTypeDesc().kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                        handleReferenceInResponse(operationAdaptor, (SimpleNameReferenceNode) array.memberTypeDesc(),
                                schemas, apiResponses);
                    } else {
                        ApiResponse apiResponse = new ApiResponse();
                        ArraySchema arraySchema = new ArraySchema();
                        io.swagger.v3.oas.models.media.MediaType mediaType =
                                new io.swagger.v3.oas.models.media.MediaType();
                        String type =
                                array.memberTypeDesc().kind().toString().trim().split("_")[0].
                                        toLowerCase(Locale.ENGLISH);
                        Schema openApiSchema = ConverterUtils.getOpenApiSchema(type);
                        String mimeType = generateMIMETypeForBallerinaType(type);
                        arraySchema.setItems(openApiSchema);
                        mediaType.setSchema(arraySchema);
                        apiResponse.description("Ok");
                        apiResponse.content(new Content().addMediaType(mimeType, mediaType));
                        apiResponses.put("200", apiResponse);

                    }
                } else {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                }
            }
        } else {
            //Return type is not given generate 200 response with text/plain
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.description("Accepted");
            apiResponses.put("202", apiResponse);
        }
        operation.setResponses(apiResponses);
    }

    private String generateMIMETypeForBallerinaType(String type) {
        switch (type) {
            case Constants.STRING:
                return MediaType.TEXT_PLAIN;
            case Constants.JSON:
                return MediaType.APPLICATION_JSON;
            case Constants.XML:
                return MediaType.APPLICATION_XML;
            case Constants.BYTE_ARRAY:
                return MediaType.APPLICATION_OCTET_STREAM;
            default:
                return MediaType.TEXT_PLAIN;
        }
    }

    private String generateApiResponseCode(String identifier) {
        switch (identifier) {
            case "Continue":
                return "100";
            case "Ok":
                return "200";
            case "Created":
                return "201";
            case "Accepted":
                return "202";
            case "BadRequest":
                return "400";
            case "Unauthorized":
                return "401";
            case "PaymentRequired":
                return "402";
            case "Forbidden":
                return "403";
            case "NotFound":
                return "404";
            case "MethodNotAllowed":
                return "405";
            case "500":
                return "InternalServerError";
            case "InternalServerError":
                return "501";
            case "BadGateway":
                return "502";
            case "ServiceUnavailable":
                return "503";
            default:
                return "200";
        }
    }

    private void handleReferenceInResponse(OperationAdaptor operationAdaptor, SimpleNameReferenceNode recordNode,
                                           Map<String, Schema> schema, ApiResponses apiResponses) {

        // Creating request body - required.
        SimpleNameReferenceNode referenceNode = recordNode;
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handel record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
        componentMapper.handleRecordNode(recordNode, schema, typeSymbol);
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        if (recordNode.parent().kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
            ArraySchema arraySchema = new ArraySchema();
            ApiResponse apiResponse = new ApiResponse();
            arraySchema.setItems(new Schema().$ref(referenceNode.name().toString().trim()));
            media.setSchema(arraySchema);
            apiResponse.description("Ok");
            apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
            apiResponses.put("200", apiResponse);

        } else {
            media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
            apiResponse.description("Ok");
            apiResponses.put("200", apiResponse);
        }
        operationAdaptor.getOperation().setResponses(apiResponses);
    }


}
