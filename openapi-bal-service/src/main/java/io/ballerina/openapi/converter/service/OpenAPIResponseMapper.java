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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.error.ErrorMessages;
import io.ballerina.openapi.converter.error.IncompatibleResourceError;
import io.ballerina.openapi.converter.error.OpenAPIConverterError;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_FIELD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_REFERENCE;
import static io.ballerina.openapi.converter.Constants.APPLICATION_PREFIX;
import static io.ballerina.openapi.converter.Constants.BODY;
import static io.ballerina.openapi.converter.Constants.CACHE_CONTROL;
import static io.ballerina.openapi.converter.Constants.ETAG;
import static io.ballerina.openapi.converter.Constants.FALSE;
import static io.ballerina.openapi.converter.Constants.HTTP;
import static io.ballerina.openapi.converter.Constants.HTTP_200;
import static io.ballerina.openapi.converter.Constants.HTTP_200_DESCRIPTION;
import static io.ballerina.openapi.converter.Constants.HTTP_CODES;
import static io.ballerina.openapi.converter.Constants.JSON_POSTFIX;
import static io.ballerina.openapi.converter.Constants.LAST_MODIFIED;
import static io.ballerina.openapi.converter.Constants.MAX_AGE;
import static io.ballerina.openapi.converter.Constants.MUST_REVALIDATE;
import static io.ballerina.openapi.converter.Constants.NO_CACHE;
import static io.ballerina.openapi.converter.Constants.NO_STORE;
import static io.ballerina.openapi.converter.Constants.NO_TRANSFORM;
import static io.ballerina.openapi.converter.Constants.OCTECT_STREAM_POSTFIX;
import static io.ballerina.openapi.converter.Constants.PRIVATE;
import static io.ballerina.openapi.converter.Constants.PROXY_REVALIDATE;
import static io.ballerina.openapi.converter.Constants.PUBLIC;
import static io.ballerina.openapi.converter.Constants.S_MAX_AGE;
import static io.ballerina.openapi.converter.Constants.TEXT_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_PREFIX;
import static io.ballerina.openapi.converter.Constants.TRUE;
import static io.ballerina.openapi.converter.Constants.XML_POSTFIX;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.extractCustomMediaType;

/**
 * This class uses to map the Ballerina return details to the OAS response.
 *
 * @since 2.0.0
 */
public class OpenAPIResponseMapper {
    private final SemanticModel semanticModel;
    private final Components components;
    private final List<OpenAPIConverterError> errors = new ArrayList<>();
    private final Location location;

    public List<OpenAPIConverterError> getErrors() {
        return errors;
    }

    public OpenAPIResponseMapper(SemanticModel semanticModel, Components components, Location location) {
        this.semanticModel = semanticModel;
        this.components = components;
        this.location = location;
    }

    /**
     * This function for mapping the function return type details with OAS response schema.
     *
     * @param resource          FunctionDefinitionNode for relevant resource function.
     * @param operationAdaptor  OperationAdaptor model for holding operation details specific to HTTP operation.
     */
    public void getResourceOutput(FunctionDefinitionNode resource, OperationAdaptor operationAdaptor) {

        FunctionSignatureNode functionSignature = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        io.swagger.v3.oas.models.Operation operation = operationAdaptor.getOperation();
        // Handle response with custom prefix subtype
        Optional<String> customMediaType = extractCustomMediaType(resource);
        ApiResponses apiResponses = new ApiResponses();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Node returnType = returnNode.type();
            Map<String, Header> headers = new LinkedHashMap<>();
            if (!annotations.isEmpty()) {
                for (AnnotationNode annotation : annotations) {
                    apiResponses.putAll(extractReturnAnnotationDetails(operationAdaptor, customMediaType,
                            returnType, headers, annotation));
                }
            } else {
                Optional<ApiResponses> responses = getAPIResponses(operationAdaptor, apiResponses, returnType,
                        customMediaType, headers);
                responses.ifPresent(apiResponses::putAll);
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
     * Extract annotation details for generate headers.
     *
     * @param operationAdaptor  - Operation model
     * @param customMediaType   - Prefix media type for response
     * @param typeNode          - Return node
     * @param headers           - OAS header
     * @param annotation        - Annotation node
     */
    private ApiResponses extractReturnAnnotationDetails(OperationAdaptor operationAdaptor,
                                                        Optional<String> customMediaType, Node typeNode,
                                                        Map<String, Header> headers, AnnotationNode annotation) {
        ApiResponses apiResponses = new ApiResponses();
        if (annotation.annotReference().toString().trim().equals("http:Cache")) {
            CacheConfigAnnotation cacheConfigAnn = new CacheConfigAnnotation();
            annotation.annotValue().ifPresentOrElse(
                    (value) -> {
                        updateResponseHeaderWithCacheValues(headers, setCacheConfigValues(value.fields()));
                        },
                    ()-> {
                        updateResponseHeaderWithCacheValues(headers, cacheConfigAnn);
                    }
            );

            // Update headers with cache annotation details.
            Optional<ApiResponses> responses = getAPIResponses(operationAdaptor, apiResponses, typeNode,
                    customMediaType, headers);
            responses.ifPresent(apiResponses::putAll);
        } else {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.description("Accepted");
            apiResponses.put("202", apiResponse);
        }
        return apiResponses;
    }

    /**
     * Update all the {@CacheConfigAnnotation} details with annotation fields.
     */
    private CacheConfigAnnotation setCacheConfigValues(SeparatedNodeList<MappingFieldNode> fields) {
        CacheConfigAnnotation cacheConfig = new CacheConfigAnnotation();
        // when field values has -- create a function for have the default values.
        Spliterator<MappingFieldNode> spliterator = fields.spliterator();
        spliterator.forEachRemaining(field-> {
            if (field.kind() == SyntaxKind.SPECIFIC_FIELD) {
                SpecificFieldNode specificFieldNode = (SpecificFieldNode) field;
                // generate string
                String name = specificFieldNode.fieldName().toString().trim();
                ExpressionNode expressionNode = specificFieldNode.valueExpr().get();
                setCacheConfigField(cacheConfig, name, expressionNode);
            }
        });
        return cacheConfig;
    }

    private void updateResponseHeaderWithCacheValues(Map<String, Header> headers, CacheConfigAnnotation cache) {

        Header cacheHeader = new Header();
        StringSchema stringSchema = new StringSchema();
        if (!cache.isSetETag() && !cache.isSetLastModified()) {
            stringSchema._default(generateCacheControlString(cache));
            cacheHeader.setSchema(stringSchema);
            headers.put(CACHE_CONTROL, cacheHeader);
        } else if (!cache.isSetETag() && cache.isSetLastModified()) {
            stringSchema._default(generateCacheControlString(cache));
            cacheHeader.setSchema(stringSchema);
            headers.put(CACHE_CONTROL, cacheHeader);
            Header lmHeader = new Header();
            lmHeader.setSchema(new StringSchema());
            headers.put(LAST_MODIFIED, lmHeader);

        } else if (!cache.isSetLastModified() && cache.isSetETag()) {
            stringSchema._default(generateCacheControlString(cache));
            cacheHeader.setSchema(stringSchema);
            headers.put(CACHE_CONTROL, cacheHeader);
            Header eTag = new Header();
            eTag.setSchema(new StringSchema());
            headers.put(ETAG, eTag);
        } else {
            cacheHeaderForEmptyValues(headers, cacheHeader, stringSchema, generateCacheControlString(cache));
        }
    }

    private void cacheHeaderForEmptyValues(Map<String, Header> headers, Header cacheHeader, StringSchema stringSchema,
                                           String value) {

        stringSchema._default(value);
        cacheHeader.setSchema(stringSchema);
        headers.put(CACHE_CONTROL, cacheHeader);
        Header eTag = new Header();
        eTag.setSchema(new StringSchema());
        headers.put(ETAG, eTag);
        Header lmHeader = new Header();
        lmHeader.setSchema(new StringSchema());
        headers.put(LAST_MODIFIED, lmHeader);
    }

    private void setCacheConfigField(CacheConfigAnnotation cacheConfig, String fieldName,
                                     ExpressionNode expressionNode) {
        if (expressionNode.toString().equals(TRUE)) {
            switch (fieldName) {
                case "noCache":
                    cacheConfig.setNoCache(true);
                    break;
                case "noStore":
                    cacheConfig.setNoStore(true);
                    break;
                case "noTransform":
                    cacheConfig.setNoTransform(true);
                    break;
                case "isPrivate":
                    cacheConfig.setPrivate(true);
                    break;
                case "proxyRevalidate":
                    cacheConfig.setProxyRevalidate(true);
                    break;
                default:
                    break;
            }
        } else if ("maxAge".equals(fieldName)) {
            String maxAge = expressionNode.toString();
            try {
                int maxA = Integer.parseInt(maxAge);
                cacheConfig.setMaxAge(maxA);
            } catch (NumberFormatException ignored) {
            }
        } else if ("sMaxAge".equals(fieldName)) {
            String smaxAge = expressionNode.toString();
            try {
                int maxA = Integer.parseInt(smaxAge);
                cacheConfig.setsMaxAge(maxA);
            } catch (NumberFormatException ignored) {
            }
        } else if ((expressionNode.toString().equals(FALSE))) {
            switch (fieldName) {
                case "setETag":
                    cacheConfig.setSetETag(false);
                    break;
                case "setLastModified":
                    cacheConfig.setSetLastModified(false);
                    break;
                case "mustRevalidate":
                    cacheConfig.setMustRevalidate(false);
                    break;
                default:
                    break;
            }
        } else if ("privateFields".equals(fieldName)) {
            List<String> privateFields = new ArrayList<>();
            SeparatedNodeList<Node> fields = ((ListConstructorExpressionNode) expressionNode).expressions();
            fields.stream().forEach(field -> privateFields.add(field.toString().trim()));
            cacheConfig.setPrivateFields(privateFields);
        } else if ("noCacheFields".equals(fieldName)) {
            List<String> noCacheFields = new ArrayList<>();
            SeparatedNodeList<Node> fields = ((ListConstructorExpressionNode) expressionNode).expressions();
            fields.stream().forEach(field -> noCacheFields.add(field.toString().trim()));
            cacheConfig.setNoCacheFields(noCacheFields);
        }
    }

    /**
     * This method for handle the return type has non annotations.
     *
     * @return {@link io.swagger.v3.oas.models.responses.ApiResponses} for operation.
     */
    private Optional<ApiResponses> getAPIResponses(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                         Node typeNode, Optional<String> customMediaPrefix,
                                         Map<String, Header> headers) {

        ApiResponse apiResponse = new ApiResponse();
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        String mediaTypeString;
        switch (typeNode.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                return  handleQualifiedNameType(apiResponses, customMediaPrefix, headers, apiResponse, qNode);
            case INT_TYPE_DESC:
            case STRING_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
                apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
                String type =  typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
                Schema schema = ConverterCommonUtils.getOpenApiSchema(type);
                Optional<String> media = convertBallerinaMIMEToOASMIMETypes(type, customMediaPrefix);
                if (media.isPresent()) {
                    mediaType.setSchema(schema);
                    apiResponse.description(HTTP_200_DESCRIPTION);
                    apiResponse.content(new Content().addMediaType(media.get(), mediaType));
                    apiResponses.put(HTTP_200, apiResponse);
                    return Optional.of(apiResponses);
                } else {
                    return Optional.empty();
                }
            case JSON_TYPE_DESC:
                apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
                mediaType.setSchema(new ObjectSchema());
                mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix.get() +
                        JSON_POSTFIX : MediaType.APPLICATION_JSON;
                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
                apiResponse.description(HTTP_200_DESCRIPTION);
                apiResponses.put(HTTP_200, apiResponse);
                return Optional.of(apiResponses);
            case XML_TYPE_DESC:
                apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
                mediaType.setSchema(new ObjectSchema());
                mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix.get() +
                       XML_POSTFIX  : MediaType.APPLICATION_XML;
                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
                apiResponse.description(HTTP_200_DESCRIPTION);
                apiResponses.put(HTTP_200, apiResponse);
                return Optional.of(apiResponses);
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode recordNode  = (SimpleNameReferenceNode) typeNode;
                Map<String, Schema> schemas = components.getSchemas();
                handleReferenceResponse(operationAdaptor, recordNode, schemas, apiResponses, customMediaPrefix,
                        headers);
                return Optional.of(apiResponses);
            case UNION_TYPE_DESC:
                return mapUnionReturns(operationAdaptor, apiResponses,
                        (UnionTypeDescriptorNode) typeNode, customMediaPrefix, headers);
            case RECORD_TYPE_DESC:
                return mapInlineRecordInReturn(operationAdaptor, apiResponses,
                        (RecordTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
            case ARRAY_TYPE_DESC:
                return getApiResponsesForArrayTypes(operationAdaptor, apiResponses,
                        (ArrayTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
            case ERROR_TYPE_DESC:
                // Return type is given as error or error? in the ballerina it will generate 500 response.
                apiResponse.description("Found unexpected output");
                mediaType.setSchema(new StringSchema());
                apiResponse.content(new Content().addMediaType(MediaType.TEXT_PLAIN, mediaType));
                apiResponses.put("500", apiResponse);
                return Optional.of(apiResponses);
            case OPTIONAL_TYPE_DESC:
                return getAPIResponses(operationAdaptor, apiResponses,
                        ((OptionalTypeDescriptorNode) typeNode).typeDescriptor(), customMediaPrefix, headers);
            default:
                ErrorMessages errorMessage = ErrorMessages.OAS_CONVERTOR_101;
                IncompatibleResourceError error = new IncompatibleResourceError(errorMessage.getCode(),
                        errorMessage.getDescription() + typeNode.kind(), this.location,
                        errorMessage.getSeverity());
                errors.add(error);
                return Optional.empty();
        }
    }

    /**
     * This function is for handling {@code QualifiedNameReferenceNode} type returns.
     */
    private Optional<ApiResponses> handleQualifiedNameType(ApiResponses apiResponses,
                                                           Optional<String> customMediaPrefix,
                                                           Map<String, Header> headers, ApiResponse apiResponse,
                                                           QualifiedNameReferenceNode qNode) {

        if (qNode.modulePrefix().text().equals(HTTP)) {
            String typeName = qNode.modulePrefix().text() + ":" + qNode.identifier().text();
            if (typeName.equals("http:Response")) {
                ErrorMessages errorMessage = ErrorMessages.OAS_CONVERTOR_105;
                IncompatibleResourceError error = new IncompatibleResourceError(errorMessage.getCode(),
                        errorMessage.getDescription(), location, errorMessage.getSeverity());
                errors.add(error);
            } else {
                Optional<String> code = generateApiResponseCode(qNode.identifier().toString().trim());
                if (code.isPresent()) {
                    apiResponse.description(qNode.identifier().toString().trim());
                    apiResponse = setCacheHeader(headers, apiResponse, code.get());
                    apiResponses.put(code.get(), apiResponse);
                    return Optional.of(apiResponses);
                } else {
                    return Optional.empty();
                }
            }
        } else {
            Symbol symbol = semanticModel.symbol(qNode).get();
            if (symbol instanceof  TypeReferenceTypeSymbol) {
                TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol;
                TypeSymbol typeSymbol = typeRef.typeDescriptor();
                if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
                    RecordTypeSymbol recordType = (RecordTypeSymbol) typeSymbol;
                    ApiResponses responses = handleRecordTypeSymbol(qNode.identifier().text().trim(),
                            components.getSchemas(), customMediaPrefix, typeRef,
                            new OpenAPIComponentMapper(components), recordType, headers);
                    apiResponses.putAll(responses);
                    return Optional.of(apiResponses);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Handle return has array types.
     */
    private Optional<ApiResponses> getApiResponsesForArrayTypes(OperationAdaptor operationAdaptor,
                                                                ApiResponses apiResponses,
                                                                ArrayTypeDescriptorNode array, ApiResponse apiResponse,
                                                                io.swagger.v3.oas.models.media.MediaType mediaType,
                                                      Optional<String> customMediaPrefix, Map<String, Header> headers) {
        Map<String, Schema> schemas02 = components.getSchemas();
        if (array.memberTypeDesc().kind() == SIMPLE_NAME_REFERENCE) {
            handleReferenceResponse(operationAdaptor, (SimpleNameReferenceNode) array.memberTypeDesc(),
                    schemas02, apiResponses, customMediaPrefix, headers);
        } else {
            ArraySchema arraySchema = new ArraySchema();
            String type02 = array.memberTypeDesc().kind().toString().trim().split("_")[0].
                            toLowerCase(Locale.ENGLISH);
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(type02);
            Optional<String> mimeType = convertBallerinaMIMEToOASMIMETypes(type02, customMediaPrefix);
            if (mimeType.isEmpty()) {
                return Optional.empty();
            }
            arraySchema.setItems(openApiSchema);
            mediaType.setSchema(arraySchema);
            apiResponse.description(HTTP_200_DESCRIPTION);
            apiResponse.content(new Content().addMediaType(mimeType.get(), mediaType));
            apiResponses.put(HTTP_200, apiResponse);
        }
        return Optional.of(apiResponses);
    }

    /**
     * Handle response has inline record as return type.
     */
    private Optional<ApiResponses> mapInlineRecordInReturn(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                 RecordTypeDescriptorNode typeNode, ApiResponse apiResponse,
                                                 io.swagger.v3.oas.models.media.MediaType mediaType,
                                                 Optional<String> customMediaPrefix, Map<String, Header> headers) {

        NodeList<Node> fields = typeNode.fields();
        Optional<String> httpCode = Optional.of(HTTP_200);
        Schema inlineSchema = new Schema();
        String description = HTTP_200_DESCRIPTION;
        Optional<String> mediaTypeResponse = Optional.of(MediaType.APPLICATION_JSON);
        boolean ishttpTypeInclusion = false;
        Map<String , Schema> properties = new HashMap<>();
        if (fields.stream().anyMatch(module -> module.kind() == TYPE_REFERENCE)) {
            ishttpTypeInclusion = true;
        }
        // TODO: 1- scenarios returns record {| *http:Ok; Person body;|}
        // TODO: 2- scenarios returns record {| *http:Ok; string body;|}
        // 3- scenarios returns record {| int id; string body;|} - done
        // 4- scenarios returns record {| int id; Person body;|} - done

        for (Node field: fields) {
            // TODO Handle http typeInclusion, check it comes from http module
            if (field.kind() == TYPE_REFERENCE) {
                TypeReferenceNode typeFieldNode = (TypeReferenceNode) field;
                if (typeFieldNode.typeName().kind() == QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode identifierNode = (QualifiedNameReferenceNode) typeFieldNode.typeName();
                    httpCode = generateApiResponseCode(identifierNode.identifier().text().trim());
                    description = identifierNode.identifier().text().trim();
                    apiResponse.description(identifierNode.identifier().text().trim());
                }
            }
            if (field.kind() == RECORD_FIELD) {
                RecordFieldNode recordField = (RecordFieldNode) field;
                Node type01 = recordField.typeName();
                if (recordField.typeName().kind() == SIMPLE_NAME_REFERENCE) {
                    Map<String, Schema> componentsSchemas = components.getSchemas();
                    SimpleNameReferenceNode nameRefNode =  (SimpleNameReferenceNode) type01;
                    handleReferenceResponse(operationAdaptor, nameRefNode, componentsSchemas, apiResponses,
                            customMediaPrefix, headers);
                    Schema referenceSchema = new Schema();
                    referenceSchema.set$ref(recordField.typeName().toString().trim());
                    properties.put(recordField.fieldName().text(), referenceSchema);
                } else {
                    //TODO array fields handling
                    mediaTypeResponse = convertBallerinaMIMEToOASMIMETypes(recordField.typeName().toString().trim(),
                            customMediaPrefix);
                    Schema propertySchema = ConverterCommonUtils.getOpenApiSchema(
                            recordField.typeName().toString().trim());
                    properties.put(recordField.fieldName().text(), propertySchema);
                }
            }
        }
        if (!ishttpTypeInclusion) {
            inlineSchema = new ObjectSchema();
            inlineSchema.setProperties(properties);
            mediaTypeResponse = Optional.of(MediaType.APPLICATION_JSON);
            if (customMediaPrefix.isPresent()) {
                mediaTypeResponse = Optional.of(APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX);
            }
        }
        mediaType.setSchema(inlineSchema);
        apiResponse.description(description);
        if (mediaTypeResponse.isPresent() && httpCode.isPresent()) {
            apiResponse.content(new Content().addMediaType(mediaTypeResponse.get(), mediaType));
            apiResponse = setCacheHeader(headers, apiResponse, httpCode.get());
            apiResponses.put(httpCode.get(), apiResponse);
            return Optional.of(apiResponses);
        } else {
            return Optional.empty();
        }
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
    private Optional<ApiResponses> mapUnionReturns(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                         UnionTypeDescriptorNode typeNode, Optional<String> customMediaPrefix
            , Map<String, Header> headers) {

        TypeDescriptorNode rightNode = typeNode.rightTypeDesc();
        TypeDescriptorNode leftNode = typeNode.leftTypeDesc();
        // Handle leftNode because it is main node
        Optional<ApiResponses> apiResponse = getAPIResponses(operationAdaptor, apiResponses, leftNode,
                customMediaPrefix, headers);
        apiResponse.ifPresent(apiResponses::putAll);
        // Handle rest of the union type
        if (rightNode instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
            while (traversRightNode.rightTypeDesc() != null) {
                if (leftNode.kind() == QUALIFIED_NAME_REFERENCE) {
                    leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
                    Optional<ApiResponses> apiResponsesLeft = getAPIResponses(operationAdaptor, apiResponses, leftNode,
                            customMediaPrefix, headers);
                    apiResponsesLeft.ifPresent(apiResponses::putAll);
                }
            }
        } else {
            Optional<ApiResponses> apiResponsesRight = getAPIResponses(operationAdaptor, apiResponses, rightNode,
                    customMediaPrefix, headers);
            apiResponsesRight.ifPresent(apiResponses::putAll);
        }
        return Optional.of(apiResponses);
    }

    /**
     * Convert ballerina MIME types to OAS MIME types.
     */
    private Optional<String> convertBallerinaMIMEToOASMIMETypes(String type, Optional<String> customMediaPrefix) {
        switch (type) {
            case Constants.JSON:
            case Constants.INT:
            case Constants.FLOAT:
            case Constants.DECIMAL:
            case Constants.BOOLEAN:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + JSON_POSTFIX)
                        .orElse(MediaType.APPLICATION_JSON));
            case Constants.XML:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + XML_POSTFIX).
                        orElse(MediaType.APPLICATION_XML));
            case Constants.BYTE_ARRAY:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + OCTECT_STREAM_POSTFIX)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM));
            case Constants.STRING:
                return Optional.of(customMediaPrefix.map(s -> TEXT_PREFIX + s +
                        TEXT_POSTFIX).orElse(MediaType.TEXT_PLAIN));
            default:
                ErrorMessages errorMessage = ErrorMessages.OAS_CONVERTOR_102;
                IncompatibleResourceError error = new IncompatibleResourceError(errorMessage.getCode(),
                        errorMessage.getDescription() + type, this.location, errorMessage.getSeverity());
                errors.add(error);
                return Optional.empty();
        }
    }

    /**
     * Get related http status code.
     */
    private Optional<String> generateApiResponseCode(String identifier) {
        if (HTTP_CODES.containsKey(identifier)) {
            return Optional.of(HTTP_CODES.get(identifier));
        } else {
            ErrorMessages errorMessage = ErrorMessages.OAS_CONVERTOR_103;
            IncompatibleResourceError error = new IncompatibleResourceError(errorMessage.getCode(),
                    errorMessage.getDescription()  + identifier, this.location, errorMessage.getSeverity());
            errors.add(error);
            return Optional.empty();
        }
    }

    private void handleReferenceResponse(OperationAdaptor operationAdaptor, SimpleNameReferenceNode referenceNode,
                                         Map<String, Schema> schema, ApiResponses apiResponses,
                                         Optional<String> customMediaPrefix, Map<String, Header> headers) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handle record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
        String mediaTypeString;
        // Check typeInclusion is related to the http status code
        if (referenceNode.parent().kind().equals(ARRAY_TYPE_DESC)) {
            io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
            ArraySchema arraySchema = new ArraySchema();
            componentMapper.createComponentSchema(schema, typeSymbol);
            arraySchema.setItems(new Schema().$ref(referenceNode.name().toString().trim()));
            media.setSchema(arraySchema);
            apiResponse.description(HTTP_200_DESCRIPTION);
            mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX :
                    MediaType.APPLICATION_JSON;
            apiResponse.content(new Content().addMediaType(mediaTypeString, media));
            apiResponses.put(HTTP_200, apiResponse);

        } else if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            if (typeReferenceTypeSymbol.typeDescriptor().typeKind() == TypeDescKind.RECORD) {
                RecordTypeSymbol returnRecord = (RecordTypeSymbol) typeReferenceTypeSymbol.typeDescriptor();
                String referenceName = referenceNode.name().toString().trim();
                ApiResponses responses = handleRecordTypeSymbol(referenceName, schema, customMediaPrefix,
                        typeSymbol, componentMapper, returnRecord, headers);
                apiResponses.putAll(responses);
            }
        }
        //Check content and status code if it is in 200 range then add the header
        operationAdaptor.getOperation().setResponses(apiResponses);
    }

    private ApiResponses handleRecordTypeSymbol(String referenceName, Map<String, Schema> schema,
                                                Optional<String> customMediaPrefix, TypeSymbol typeSymbol,
                                                OpenAPIComponentMapper componentMapper,
                                                RecordTypeSymbol returnRecord,
                                                Map<String, Header> headers) {
        ApiResponses apiResponses = new ApiResponses();

        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        List<TypeSymbol> typeInclusions = returnRecord.typeInclusions();
        if (!typeInclusions.isEmpty()) {
            Optional<ApiResponses> typeInclusionResponses = handleRecordHasHttpTypeInclusionField(schema, typeSymbol,
                    componentMapper, returnRecord, typeInclusions, customMediaPrefix, headers);
            if (typeInclusionResponses.isPresent()) {
                apiResponses.putAll(typeInclusionResponses.get());
            } else {
                apiResponses.putAll(createResponseForRecord(referenceName, schema, customMediaPrefix, typeSymbol,
                        componentMapper, media, headers));
            }
        } else {
            apiResponses.putAll(createResponseForRecord(referenceName, schema, customMediaPrefix, typeSymbol,
                    componentMapper, media, headers));
        }
        return apiResponses;
    }

    private ApiResponses createResponseForRecord(String referenceName, Map<String, Schema> schema,
                                                 Optional<String> customMediaPrefix, TypeSymbol typeSymbol,
                                                 OpenAPIComponentMapper componentMapper,
                                                 io.swagger.v3.oas.models.media.MediaType media,
                                                 Map<String, Header> headers) {
        ApiResponses apiResponses = new ApiResponses();
        String mediaTypeString;
        componentMapper.createComponentSchema(schema, typeSymbol);
        media.setSchema(new Schema().$ref(referenceName));
        mediaTypeString = MediaType.APPLICATION_JSON;
        if (customMediaPrefix.isPresent()) {
            mediaTypeString = APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX;
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
        apiResponse.content(new Content().addMediaType(mediaTypeString, media));
        apiResponse.description(HTTP_200_DESCRIPTION);
        apiResponses.put(HTTP_200, apiResponse);
        return apiResponses;
    }

    private Optional<ApiResponses> handleRecordHasHttpTypeInclusionField(Map<String, Schema> schema,
                                                                         TypeSymbol typeSymbol,
                                                       OpenAPIComponentMapper componentMapper,
                                                       RecordTypeSymbol returnRecord, List<TypeSymbol> typeInclusions,
                                                                         Optional<String> customMediaPrefix, Map<String,
            Header> headers) {
        ApiResponses apiResponses = new ApiResponses();
        ApiResponse apiResponse = new ApiResponse();
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
        boolean isHttpModule = false;
        String mediaTypeString;
        for (TypeSymbol typeInSymbol : typeInclusions) {
            if (HTTP.equals(typeInSymbol.getModule().orElseThrow().getName().orElseThrow())) {
                isHttpModule = true;
                Optional<String> code = generateApiResponseCode(typeInSymbol.getName().orElseThrow().trim());
                if (code.isEmpty()) {
                    return Optional.empty();
                }
                apiResponse = setCacheHeader(headers, apiResponse, code.get());
                Map<String, RecordFieldSymbol> fieldsOfRecord = returnRecord.fieldDescriptors();
                // Handle the content of the response
                RecordFieldSymbol body = fieldsOfRecord.get(BODY);
                if (body.typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                    componentMapper.createComponentSchema(schema, body.typeDescriptor());
                    media.setSchema(new Schema().$ref(body.typeDescriptor().getName().orElseThrow().trim()));
                    mediaTypeString = customMediaPrefix.isPresent() ?
                            APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX : MediaType.APPLICATION_JSON;
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                    apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                    apiResponses.put(code.get(), apiResponse);
                } else if (body.typeDescriptor().typeKind() == TypeDescKind.STRING) {
                    media.setSchema(new StringSchema());
                    mediaTypeString = customMediaPrefix.isPresent() ? "text/" + customMediaPrefix.get() + "+plain" :
                            MediaType.TEXT_PLAIN;
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                    apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                    apiResponses.put(code.get(), apiResponse);
                } else {
                    apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                    apiResponses.put(code.get(), apiResponse);
                }
            }
        }
        if (!isHttpModule) {
            componentMapper.createComponentSchema(schema, typeSymbol);
            media.setSchema(new Schema().$ref(typeSymbol.getName().get()));
            mediaTypeString = MediaType.APPLICATION_JSON;
            if (customMediaPrefix.isPresent()) {
                mediaTypeString = APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX;
            }
            apiResponse = setCacheHeader(headers, apiResponse, HTTP_200);
            apiResponse.content(new Content().addMediaType(mediaTypeString, media));
            apiResponse.description(HTTP_200_DESCRIPTION);
            apiResponses.put(HTTP_200, apiResponse);
        }
        return Optional.of(apiResponses);
    }

    /**
     * Cache configuration utils.
     */
    public String generateCacheControlString(CacheConfigAnnotation cacheConfig) {
        List<String> directives = new ArrayList<>();
        if (cacheConfig.isMustRevalidate()) {
            directives.add(MUST_REVALIDATE);
        }
        if (cacheConfig.isNoCache()) {
            directives.add(NO_CACHE + appendFields(cacheConfig.getNoCacheFields()));
        }
        if (cacheConfig.isNoStore()) {
            directives.add(NO_STORE);
        }
        if (cacheConfig.isNoTransform()) {
            directives.add(NO_TRANSFORM);
        }
        if (cacheConfig.isPrivate()) {
            directives.add(PRIVATE + appendFields(cacheConfig.getPrivateFields()));
        } else {
            directives.add(PUBLIC);
        }
        if (cacheConfig.isProxyRevalidate()) {
            directives.add(PROXY_REVALIDATE);
        }

        if (cacheConfig.getMaxAge() > -1) {
            directives.add(MAX_AGE + "=" + cacheConfig.getMaxAge());
        }
        if (cacheConfig.getsMaxAge() > -1) {
            directives.add(S_MAX_AGE + "=" + cacheConfig.getsMaxAge());
        }
        return String.join(",", directives);
    }

    private String appendFields(List<String> fields) {
        return ("=\"" + buildCommaSeparatedString(fields) + "\"");
    }

    private String buildCommaSeparatedString(List<String> fields) {
        String genString = fields.stream().map(field -> field.replaceAll("\"", "") + ",").collect(Collectors.joining());
        return genString.substring(0, genString.length() - 1);
    }

    /**
     * This util function is for setting header parameters in the OAS response section which has 200 range status code.
     */
    private ApiResponse setCacheHeader(Map<String, Header> headers, ApiResponse apiResponse, String code) {
        if (code.startsWith("2") && !headers.isEmpty()) {
            apiResponse.setHeaders(headers);
        }
        return apiResponse;
    }
}
