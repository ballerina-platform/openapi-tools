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
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import static io.ballerina.openapi.converter.Constants.APPLICATION_PREFIX;
import static io.ballerina.openapi.converter.Constants.DELETE;
import static io.ballerina.openapi.converter.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.converter.Constants.JSON_POSTFIX;
import static io.ballerina.openapi.converter.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.converter.Constants.OCTECT_STREAM_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_PREFIX;
import static io.ballerina.openapi.converter.Constants.XML_POSTFIX;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.getOpenApiSchema;

/**
 * OpenAPIRequestBodyMapper provides functionality for converting ballerina payload to OAS request body model.
 *
 * @since 2.0.0
 */
public class OpenAPIRequestBodyMapper {
    private final Components components;
    private final OperationAdaptor operationAdaptor;
    private final SemanticModel semanticModel;
    private final String customMediaPrefix;
    private final List<OpenAPIConverterDiagnostic> diagnostics;
    private final ModuleMemberVisitor moduleMemberVisitor;

    /**
     * This constructor uses to create OpenAPIRequestBodyMapper instance when customMedia type enable.
     *
     * @param components        - OAS Components
     * @param operationAdaptor  - Model of operation
     * @param semanticModel     - Semantic model for given ballerina service
     * @param customMediaType   - custom media type
     */
    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel, String customMediaType,
                                    ModuleMemberVisitor moduleMemberVisitor) {
        this.components = components;
        this.operationAdaptor = operationAdaptor;
        this.semanticModel = semanticModel;
        this.customMediaPrefix = customMediaType;
        this.diagnostics = new ArrayList<>();
        this.moduleMemberVisitor = moduleMemberVisitor;
    }

    /**
     * This constructor uses to create OpenAPIRequestBodyMapper instance when customMedia type absent.
     *
     * @param components        - OAS Components
     * @param operationAdaptor  - Model of operation
     * @param semanticModel     - Semantic model for given ballerina service
     */
    public OpenAPIRequestBodyMapper(Components components, OperationAdaptor operationAdaptor,
                                    SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor) {
        this(components, operationAdaptor, semanticModel, null, moduleMemberVisitor);
    }

    public List<OpenAPIConverterDiagnostic> getDiagnostics() {
        return diagnostics;
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

            // Handle multiple mime type when attached with request payload annotation.
            // ex: <pre> @http:Payload { mediaType:["application/json", "application/xml"] } Pet payload </pre>
            if (fields != null && !fields.isEmpty()) {
                List<String> mimeTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE, annotation,
                        semanticModel);
                RequestBody requestBody = new RequestBody();
                for (String mimeType: mimeTypes) {
                    createRequestBody(bodyParameter, payloadNode, schema, requestBody, mimeType);
                }
            }  else {
                handlePayloadType((TypeDescriptorNode) payloadNode.typeName(), schema, bodyParameter);
            }
        }
    }

    /**
     * This function is used to handle when payload has one mime type.
     *<pre>
     *     resource function post pets(@http:Payload json payload){}
     *</pre>
     */
    private void handlePayloadType(TypeDescriptorNode payloadNode, Map<String, Schema> schema,
                                   RequestBody bodyParameter) {
        SyntaxKind kind = payloadNode.kind();
        String mediaTypeString = getMediaTypeForSyntaxKind(payloadNode);
        if (!isTypeDeclarationExtractable(kind, mediaTypeString)) {
            //Warning message for unsupported request payload type in Ballerina resource.
            IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(
                    DiagnosticMessages.OAS_CONVERTOR_117, payloadNode.location(), payloadNode.toSourceCode().trim());
            diagnostics.add(error);
            return;
        }
        switch (kind) {
            case INT_TYPE_DESC:
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
            case JSON_TYPE_DESC:
            case XML_TYPE_DESC:
            case BYTE_TYPE_DESC:
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString, kind);
                break;
            case STRING_TYPE_DESC:
                mediaTypeString = customMediaPrefix == null ? MediaType.TEXT_PLAIN :
                        TEXT_PREFIX + customMediaPrefix + TEXT_POSTFIX;
                addConsumes(operationAdaptor, bodyParameter, mediaTypeString, kind);
                break;
            case MAP_TYPE_DESC:
                Schema objectSchema = new ObjectSchema();
                MapTypeDescriptorNode mapTypeDescriptorNode = (MapTypeDescriptorNode) payloadNode;
                SyntaxKind mapMemberType = (mapTypeDescriptorNode.mapTypeParamsNode()).typeNode().kind();
                Schema<?> openApiSchema = getOpenApiSchema(mapMemberType);
                objectSchema.additionalProperties(openApiSchema);
                io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                if (bodyParameter.getContent() != null) {
                    Content content = bodyParameter.getContent();
                    if (content.containsKey(mediaTypeString)) {
                        ComposedSchema oneOfSchema = new ComposedSchema();
                        oneOfSchema.addOneOfItem(content.get(mediaTypeString).getSchema());
                        oneOfSchema.addOneOfItem(objectSchema);
                        mediaType.setSchema(oneOfSchema);
                        content.addMediaType(mediaTypeString, mediaType);
                    } else {
                        mediaType.setSchema(objectSchema);
                        content.addMediaType(mediaTypeString, mediaType);
                    }
                } else {
                    mediaType.setSchema(objectSchema);
                    bodyParameter.setContent(new Content().addMediaType(mediaTypeString, mediaType));
                }
                operationAdaptor.getOperation().setRequestBody(bodyParameter);
                break;
            case OPTIONAL_TYPE_DESC:
                OptionalTypeDescriptorNode optionalTypeDescriptorNode = (OptionalTypeDescriptorNode) payloadNode;
                handlePayloadType((TypeDescriptorNode) optionalTypeDescriptorNode.typeDescriptor(),
                        schema, bodyParameter);
                break;
            case UNION_TYPE_DESC:
                UnionTypeDescriptorNode unionTypeDescriptorNode = (UnionTypeDescriptorNode) payloadNode;
                TypeDescriptorNode leftTypeDesc = unionTypeDescriptorNode.leftTypeDesc();
                TypeDescriptorNode rightTypeDesc = unionTypeDescriptorNode.rightTypeDesc();
                handlePayloadType(leftTypeDesc, schema, bodyParameter);
                handlePayloadType(rightTypeDesc, schema, bodyParameter);
                break;
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode record = (SimpleNameReferenceNode) payloadNode;
                TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(record));
                String recordName = record.name().toString().trim();
                handleReferencePayload(typeSymbol, mediaTypeString, recordName, schema, bodyParameter);
                break;
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode separateRecord = (QualifiedNameReferenceNode) payloadNode;
                typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(separateRecord));
                recordName = ((QualifiedNameReferenceNode) payloadNode).identifier().text();
                TypeDescKind typeDesc = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor().typeKind();
                String mimeType = getMediaTypeForTypeDecKind(typeDesc);

                handleReferencePayload(typeSymbol, mimeType, recordName, schema, bodyParameter);
                break;
            case ARRAY_TYPE_DESC:
                ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) payloadNode;
                mediaTypeString = getMediaTypeForSyntaxKind(arrayNode.memberTypeDesc());
                handleArrayTypePayload(schema, arrayNode, mediaTypeString, bodyParameter);
                break;
            default:
                //Warning message for unsupported request payload type in Ballerina resource.
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(
                        DiagnosticMessages.OAS_CONVERTOR_117, payloadNode.location(),
                        payloadNode.toSourceCode().trim());
                diagnostics.add(error);
                break;
        }
    }

    private static boolean isTypeDeclarationExtractable(SyntaxKind kind, String mediaTypeString) {
        return mediaTypeString != null || kind == SyntaxKind.UNION_TYPE_DESC ||
                kind == SyntaxKind.QUALIFIED_NAME_REFERENCE || kind == SyntaxKind.OPTIONAL_TYPE_DESC ||
                kind == SyntaxKind.ARRAY_TYPE_DESC;
    }

    /**
     * This function is used to select the media type according to the syntax kind.
     *
     * @param payloadNode - payload type descriptor node
     */
    private String getMediaTypeForSyntaxKind(TypeDescriptorNode payloadNode) {
        SyntaxKind kind = payloadNode.kind();
        switch (kind) {
            case RECORD_TYPE_DESC:
            case MAP_TYPE_DESC:
            case INT_TYPE_DESC:
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
            case JSON_TYPE_DESC:
                return customMediaPrefix == null ? MediaType.APPLICATION_JSON :
                        APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX;
            case XML_TYPE_DESC:
                return customMediaPrefix == null ? MediaType.APPLICATION_XML :
                        APPLICATION_PREFIX + customMediaPrefix + XML_POSTFIX;
            case STRING_TYPE_DESC:
                return customMediaPrefix == null ? MediaType.TEXT_PLAIN :
                        TEXT_PREFIX + customMediaPrefix + TEXT_POSTFIX;
            case BYTE_TYPE_DESC:
                return customMediaPrefix == null ? MediaType.APPLICATION_OCTET_STREAM :
                        APPLICATION_PREFIX + customMediaPrefix + OCTECT_STREAM_POSTFIX;
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode record = (SimpleNameReferenceNode) payloadNode;
                TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(record));
                if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                    typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
                }
                if (typeSymbol instanceof IntersectionTypeSymbol) {
                    typeSymbol = ((IntersectionTypeSymbol) typeSymbol).effectiveTypeDescriptor();
                }
                return getMediaTypeForTypeDecKind(typeSymbol.typeKind());
            default:
                // Default the warning will be added in the handlePayloadType function.
                break;
        }
        return null;
    }

    /**
     * This function is used to select the media type according to the type descriptor kind.
     */
    private String getMediaTypeForTypeDecKind(TypeDescKind type) {
        switch (type) {
            case RECORD:
            case MAP:
            case INT:
            case INT_SIGNED32:
            case INT_SIGNED16:
            case FLOAT:
            case DECIMAL:
            case BOOLEAN:
            case JSON:
                return customMediaPrefix == null ? MediaType.APPLICATION_JSON :
                        APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX;
            case XML:
                return customMediaPrefix == null ? MediaType.APPLICATION_XML :
                        APPLICATION_PREFIX + customMediaPrefix + XML_POSTFIX;
            case STRING:
                return customMediaPrefix == null ? MediaType.TEXT_PLAIN :
                        TEXT_PREFIX + customMediaPrefix + TEXT_POSTFIX;
            case BYTE:
                return customMediaPrefix == null ? MediaType.APPLICATION_OCTET_STREAM :
                        APPLICATION_PREFIX + customMediaPrefix + OCTECT_STREAM_POSTFIX;
            default:
                // Default return the null and the warning will be added in the handlePayloadType function.
                break;
        }
        return null;
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
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        if (typeDescriptorNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
            //handle record for components
            SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) typeDescriptorNode;
            TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(referenceNode));
            OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, moduleMemberVisitor);
            componentMapper.createComponentSchema(schema, typeSymbol);
            diagnostics.addAll(componentMapper.getDiagnostics());
            Schema itemSchema = new Schema();
            arraySchema.setItems(itemSchema.$ref(ConverterCommonUtils.unescapeIdentifier(
                    referenceNode.name().text().trim())));
            media.setSchema(arraySchema);
        } else if (typeDescriptorNode.kind() == SyntaxKind.BYTE_TYPE_DESC) {
            StringSchema byteSchema = new StringSchema();
            byteSchema.setFormat("byte");
            media.setSchema(byteSchema);
        } else {
            Schema itemSchema = getOpenApiSchema(typeDescriptorNode.kind());
            arraySchema.setItems(itemSchema);
            media.setSchema(arraySchema);
        }

        if (requestBody.getContent() != null) {
            Content content = requestBody.getContent();
            if (content.containsKey(mimeType)) {
                ComposedSchema oneOfSchema = new ComposedSchema();
                oneOfSchema.addOneOfItem(content.get(mimeType).getSchema());
                oneOfSchema.addOneOfItem(arraySchema);
                media.setSchema(oneOfSchema);
            } else {
                content.addMediaType(mimeType, media);
            }
            content.addMediaType(mimeType, media);
            requestBody.setContent(content);
        } else {
            requestBody.setContent(new Content().addMediaType(mimeType, media));
        }
        //  Adding conditional check for http delete operation as it cannot have body
        //  parameter.
        if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
            operationAdaptor.getOperation().setRequestBody(requestBody);
        }
    }

    private void createRequestBody(RequestBody bodyParameter, RequiredParameterNode payloadNode,
                                   Map<String, Schema> schema, RequestBody requestBody, String mime) {

        String mimeType = mime.replaceAll("\"", "");
        if (payloadNode.typeName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode record = (SimpleNameReferenceNode) payloadNode.typeName();
            TypeSymbol typeSymbol = getReferenceTypeSymbol(semanticModel.symbol(record));
            String recordName = record.name().toString().trim();
            handleReferencePayload(typeSymbol, mimeType, recordName, schema, requestBody);
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
    private void handleReferencePayload(TypeSymbol typeSymbol, String mediaType, String recordName,
                                        Map<String, Schema> schema, RequestBody bodyParameter) {
        //handle record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, moduleMemberVisitor);
        componentMapper.createComponentSchema(schema, typeSymbol);
        diagnostics.addAll(componentMapper.getDiagnostics());
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        media.setSchema(new Schema().$ref(ConverterCommonUtils.unescapeIdentifier(recordName)));
        if (bodyParameter.getContent() != null) {
            Content content = bodyParameter.getContent();
            if (content.containsKey(mediaType)) {
                io.swagger.v3.oas.models.media.MediaType mediaSchema = content.get(mediaType);
                Schema currentSchema = mediaSchema.getSchema();
                ComposedSchema oneOf = new ComposedSchema();
                oneOf.addOneOfItem(currentSchema);
                oneOf.addOneOfItem(new Schema().$ref(ConverterCommonUtils.unescapeIdentifier(recordName)));
                mediaSchema.setSchema(oneOf);
                content.addMediaType(mediaType, mediaSchema);
            } else {
                content.addMediaType(mediaType, media);
            }
            bodyParameter.setContent(content);
        } else {
            bodyParameter.setContent(new Content().addMediaType(mediaType, media));
        }
        //  Adding conditional check for http delete operation as it cannot have body
        //  parameter.
        if (!operationAdaptor.getHttpOperation().equalsIgnoreCase(DELETE)) {
            operationAdaptor.getOperation().setRequestBody(bodyParameter);
        }
    }

    private void addConsumes(OperationAdaptor operationAdaptor, RequestBody bodyParameter, String applicationType,
                             SyntaxKind kindType) {
        String type = kindType.name().split("_")[0].toLowerCase(Locale.ENGLISH);
        Schema schema = ConverterCommonUtils.getOpenApiSchema(type);
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        if (bodyParameter.getContent() != null) {
            Content content = bodyParameter.getContent();
            if (content.containsKey(applicationType)) {
                io.swagger.v3.oas.models.media.MediaType mediaSchema = content.get(applicationType);
                Schema currentSchema = mediaSchema.getSchema();
                ComposedSchema oneOf = new ComposedSchema();
                oneOf.addOneOfItem(currentSchema);
                oneOf.addOneOfItem(schema);
                mediaSchema.setSchema(oneOf);
                content.addMediaType(applicationType, mediaSchema);
            } else {
                mediaType.setSchema(schema);
                content.addMediaType(applicationType, mediaType);
            }
            bodyParameter.setContent(content);
        } else {
            mediaType.setSchema(schema);
            bodyParameter.setContent(new Content().addMediaType(applicationType, mediaType));
            operationAdaptor.getOperation().setRequestBody(bodyParameter);
        }
    }
}
