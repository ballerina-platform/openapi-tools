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
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
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
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.OpenApiConverterException;
import io.ballerina.openapi.converter.utils.ConverterUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import static io.ballerina.openapi.converter.Constants.HTTP;

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
                // Handle in further implementation
                // Temporary solution
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.description("Accepted");
                apiResponses.put("202", apiResponse);

            } else {
                handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, typeNode);
            }
        } else {
            //Return type is not given generate 200 response with text/plain
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
    private ApiResponses handleResponseWithoutAnnotationType(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                             Node typeNode) throws OpenApiConverterException {

        if (typeNode.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
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
            apiResponses = getAPIResponsesForReturnUnionType(operationAdaptor, apiResponses,
                    (UnionTypeDescriptorNode) typeNode);
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
        return apiResponses;
    }

    /**
     * Handle the response has union type.
     *
     * <per>
     *     resource function post reservation(@http:Payload Reservation reservation)
     *                 returns ReservationCreated|ReservationConflict {
     *         ReservationCreated created = createReservation(reservation);
     *         return created;
     *     }
     * </per>
     */
    private ApiResponses getAPIResponsesForReturnUnionType(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                           UnionTypeDescriptorNode typeNode)
            throws OpenApiConverterException {

        UnionTypeDescriptorNode unionNode = typeNode;
        TypeDescriptorNode rightNode = unionNode.rightTypeDesc();
        TypeDescriptorNode leftNode = unionNode.leftTypeDesc();
        // Handle leftNode because it is main node
        apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses,
                leftNode);
        // Handle rest of the union type
        if (rightNode instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
            while (traversRightNode.rightTypeDesc() != null) {
                if (leftNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                    leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
                    apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses, leftNode);
                }
            }
        } else {
            apiResponses = handleResponseWithoutAnnotationType(operationAdaptor, apiResponses,
                    rightNode);
        }
        return apiResponses;
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

    private String generateApiResponseCode(String identifier) throws OpenApiConverterException {
        switch (identifier) {
            case "Continue":
                return "100";
            case "SwitchingProtocols":
                return "101";
            case "Ok":
                return "200";
            case "Created":
                return "201";
            case "Accepted":
                return "202";
            case "NonAuthoritativeInformation":
                return "203";
            case "NoContent":
                return "204";
            case "RestContent":
                return "205";
            case "PartialContent":
                return "206";
            case "MultipleChoices":
                return "300";
            case "MovedPermanently":
                return "301";
            case "Found":
                return "302";
            case "SeeOther":
                return "303";
            case "NotModified":
                return "304";
            case "UseProxy":
                return "305";
            case "TemporaryRedirect":
                return "308";
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
            case "NotAccepted":
                return "406";
            case "ProxyAuthenticationRequires":
                return "407";
            case "RequestTimeOut":
                return "408";
            case "Conflict":
                return "409";
            case "Gone":
                return "410";
            case "LengthRequired":
                return "411";
            case "PreconditionFailed":
                return "412";
            case "UriTooLong":
                return "413";
            case "UnsupportedMediaType":
                return "414";
            case "RangeNotSatisfied":
                return "415";
            case "ExpectationFailed":
                return "416";
            case "UpgradeRequired":
                return "426";
            case "RequestHeaderFieldsTooLarge":
                return "431";
            case "InternalServerError":
                return "500";
            case "NotImplemented":
                return "501";
            case "BadGateway":
                return "502";
            case "ServiceUnavailable":
                return "503";
            case "GatewayTimeOut":
                return "504";
            case "HttpVersionNotSupported":
                return "505";
            default:
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
        if (referenceNode.parent().kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(new Schema().$ref(referenceNode.name().toString().trim()));
            media.setSchema(arraySchema);
            apiResponse.description("Ok");
            apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
            apiResponses.put("200", apiResponse);

        } else if (typeSymbol.typeKind().getName().equals("typeReference")) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            if (typeReferenceTypeSymbol.typeDescriptor().typeKind().getName().equals("record")) {
                RecordTypeSymbol returnRecord = (RecordTypeSymbol) typeReferenceTypeSymbol.typeDescriptor();
                List<TypeSymbol> typeInclusions = returnRecord.typeInclusions();
                if (!typeInclusions.isEmpty()) {
                    boolean isHttpModule = false;
                    for (TypeSymbol typeInSymbol : typeInclusions) {
                        if (HTTP.equals(typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                            isHttpModule = true;
                            String code = generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                            Map<String, RecordFieldSymbol> fieldsOfRecord = returnRecord.fieldDescriptors();
                            // Handle the content of the response
                            RecordFieldSymbol body = fieldsOfRecord.get("body");
                            RecordFieldSymbol mediaType = fieldsOfRecord.get("mediaType");
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
                        apiResponse.description("Ok");
                        apiResponses.put("200", apiResponse);
                    }
                } else {
                    componentMapper.handleRecordNode(referenceNode, schema, typeSymbol);
                    media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                }
            }
        }
        operationAdaptor.getOperation().setResponses(apiResponses);
    }
}
