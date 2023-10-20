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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ReadonlyTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MapTypeDescriptorNode;
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
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUALIFIED_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_FIELD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_REFERENCE;
import static io.ballerina.openapi.converter.Constants.ACCEPTED;
import static io.ballerina.openapi.converter.Constants.APPLICATION_PREFIX;
import static io.ballerina.openapi.converter.Constants.BAD_REQUEST;
import static io.ballerina.openapi.converter.Constants.BODY;
import static io.ballerina.openapi.converter.Constants.BYTE;
import static io.ballerina.openapi.converter.Constants.BYTE_ARRAY;
import static io.ballerina.openapi.converter.Constants.CACHE_CONTROL;
import static io.ballerina.openapi.converter.Constants.ETAG;
import static io.ballerina.openapi.converter.Constants.FALSE;
import static io.ballerina.openapi.converter.Constants.HTML_POSTFIX;
import static io.ballerina.openapi.converter.Constants.HTTP;
import static io.ballerina.openapi.converter.Constants.HTTP_200;
import static io.ballerina.openapi.converter.Constants.HTTP_200_DESCRIPTION;
import static io.ballerina.openapi.converter.Constants.HTTP_201;
import static io.ballerina.openapi.converter.Constants.HTTP_201_DESCRIPTION;
import static io.ballerina.openapi.converter.Constants.HTTP_202;
import static io.ballerina.openapi.converter.Constants.HTTP_204;
import static io.ballerina.openapi.converter.Constants.HTTP_400;
import static io.ballerina.openapi.converter.Constants.HTTP_500;
import static io.ballerina.openapi.converter.Constants.HTTP_500_DESCRIPTION;
import static io.ballerina.openapi.converter.Constants.HTTP_CODES;
import static io.ballerina.openapi.converter.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.converter.Constants.HTTP_RESPONSE;
import static io.ballerina.openapi.converter.Constants.JSON_POSTFIX;
import static io.ballerina.openapi.converter.Constants.LAST_MODIFIED;
import static io.ballerina.openapi.converter.Constants.MAX_AGE;
import static io.ballerina.openapi.converter.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.converter.Constants.MUST_REVALIDATE;
import static io.ballerina.openapi.converter.Constants.NO_CACHE;
import static io.ballerina.openapi.converter.Constants.NO_STORE;
import static io.ballerina.openapi.converter.Constants.NO_TRANSFORM;
import static io.ballerina.openapi.converter.Constants.OCTECT_STREAM_POSTFIX;
import static io.ballerina.openapi.converter.Constants.PLUS;
import static io.ballerina.openapi.converter.Constants.POST;
import static io.ballerina.openapi.converter.Constants.PRIVATE;
import static io.ballerina.openapi.converter.Constants.PROXY_REVALIDATE;
import static io.ballerina.openapi.converter.Constants.PUBLIC;
import static io.ballerina.openapi.converter.Constants.RESPONSE_HEADERS;
import static io.ballerina.openapi.converter.Constants.SLASH;
import static io.ballerina.openapi.converter.Constants.S_MAX_AGE;
import static io.ballerina.openapi.converter.Constants.TEXT_POSTFIX;
import static io.ballerina.openapi.converter.Constants.TEXT_PREFIX;
import static io.ballerina.openapi.converter.Constants.TRUE;
import static io.ballerina.openapi.converter.Constants.WILD_CARD_CONTENT_KEY;
import static io.ballerina.openapi.converter.Constants.WILD_CARD_SUMMARY;
import static io.ballerina.openapi.converter.Constants.XML_POSTFIX;
import static io.ballerina.openapi.converter.Constants.X_WWW_FORM_URLENCODED_POSTFIX;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.extractCustomMediaType;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.getOpenApiSchema;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This class uses to map the Ballerina return details to the OAS response.
 *
 * @since 2.0.0
 */
public class OpenAPIResponseMapper {

    private final SemanticModel semanticModel;
    private final Components components;
    private final List<OpenAPIConverterDiagnostic> errors = new ArrayList<>();
    private final Location location;
    private String httpMethod;
    private ModuleMemberVisitor moduleMemberVisitor;

    public List<OpenAPIConverterDiagnostic> getErrors() {
        return errors;
    }

    public OpenAPIResponseMapper(SemanticModel semanticModel, Components components, Location location,
                                 ModuleMemberVisitor moduleMemberVisitor) {
        this.semanticModel = semanticModel;
        this.components = components;
        this.location = location;
        this.moduleMemberVisitor = moduleMemberVisitor;
    }

    /**
     * This function for mapping the function return type details with OAS response schema.
     *
     * @param resource         FunctionDefinitionNode for relevant resource function.
     * @param operationAdaptor OperationAdaptor model for holding operation details specific to HTTP operation.
     */
    public void getResourceOutput(FunctionDefinitionNode resource, OperationAdaptor operationAdaptor) {

        httpMethod = operationAdaptor.getHttpOperation().toUpperCase(Locale.ENGLISH);
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
                    ApiResponses responses = extractReturnAnnotationDetails(operationAdaptor, customMediaType,
                            returnType, headers, annotation);
                    addResponse(apiResponses, Optional.of(responses));
                }
            } else {
                Optional<ApiResponses> responses = getAPIResponses(operationAdaptor, returnType,
                        customMediaType, headers);
                addResponse(apiResponses, responses);
            }
        } else {
            // When the return type is not mention in the resource function.
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.description(ACCEPTED);
            apiResponses.put(HTTP_202, apiResponse);
            if (operation.getRequestBody() != null || operation.getParameters() != null) {
                ApiResponse badRequestResponse = new ApiResponse();
                badRequestResponse.description(BAD_REQUEST);
                apiResponses.put(HTTP_400, badRequestResponse);
            }
        }
        operation.setResponses(apiResponses);
    }

    /**
     * Extract annotation details for generate headers.
     *
     * @param operationAdaptor - Operation model
     * @param customMediaType  - Prefix media type for response
     * @param typeNode         - Return node
     * @param headers          - OAS header
     * @param annotation       - Annotation node
     */
    private ApiResponses extractReturnAnnotationDetails(OperationAdaptor operationAdaptor,
                                                        Optional<String> customMediaType, Node typeNode,
                                                        Map<String, Header> headers, AnnotationNode annotation) {
        List<String> overrideMediaType = new ArrayList<>();
        ApiResponses apiResponses = new ApiResponses();
        if (annotation.annotReference().toString().trim().equals("http:Cache")) {
            CacheConfigAnnotation cacheConfigAnn = new CacheConfigAnnotation();
            annotation.annotValue().ifPresentOrElse(
                    value -> updateResponseHeaderWithCacheValues(headers, setCacheConfigValues(value.fields())),
                    () -> updateResponseHeaderWithCacheValues(headers, cacheConfigAnn));
        } else if (annotation.annotReference().toString().trim().equals(HTTP_PAYLOAD)) {
            overrideMediaType = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE, annotation, semanticModel);
        }
        Optional<ApiResponses> responses = getAPIResponses(operationAdaptor, typeNode,
                customMediaType, headers);
        // Override the mediaType with given type
        if (responses.isPresent() && !overrideMediaType.isEmpty()) {
            ApiResponses updatedResponse = overrideMediaType(customMediaType, overrideMediaType, responses.get());
            addResponse(apiResponses, Optional.of(updatedResponse));
        } else {
            addResponse(apiResponses, responses);
        }
        return apiResponses;
    }

    /**
     * This util function is used to override the MediaType with given mediaType inside the payload annotation.
     * ex: <pre>
     *     http:Payload {mediaType: ["application/json", "application/xml"]}
     * </pre>
     *
     * @param customMediaType   This customMediaType comes from service configure annotation.
     * @param overrideMediaType MediaTypes inside payload annotation.
     * @param responses         Generated response.
     */
    private ApiResponses overrideMediaType(Optional<String> customMediaType, List<String> overrideMediaType,
                                           ApiResponses responses) {
        ApiResponses updatedResponse = new ApiResponses();
        for (Map.Entry<String, ApiResponse> next : responses.entrySet()) {
            ApiResponse response = next.getValue();
            Content content = response.getContent();
            if (content == null) {
                continue;
            }
            Set<Map.Entry<String, io.swagger.v3.oas.models.media.MediaType>> entries = content.entrySet();
            Iterator<Map.Entry<String, io.swagger.v3.oas.models.media.MediaType>> iterMediaType = entries.iterator();
            Content updatedContent = new Content();
            while (iterMediaType.hasNext()) {
                Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> currentMedia = iterMediaType.next();
                for (String mediaType : overrideMediaType) {
                    if (customMediaType.isPresent()) {
                        Optional<String> media = convertBallerinaMIMEToOASMIMETypes(mediaType, customMediaType);
                        if (media.isPresent()) {
                            mediaType = media.get();
                        } else {
                            /* if both the return type annotation and the HTTP serviceConfig annotation contains media
                               type prefixes, need to combine both when generating OAS media type. (e.g. for the below
                               scenario, the media type will be 'application/snowflake+fake+xml' )
                              @http:ServiceConfig{
                             *     mediaTypeSubtypePrefix: "snowflake"
                             * }
                             * service /payloadV on helloEp {
                             *  resource function get pet02() returns @http:Payload {mediaType: "application/fake+xml"}
                             *  User {
                             *         return {};
                             *     }
                             *  }
                             */
                            StringBuilder mediaTypeBuilder = new StringBuilder();
                            String[] splits = mediaType.split(SLASH);
                            mediaTypeBuilder.append(splits[0]).append(SLASH).append(customMediaType.get())
                                    .append(PLUS).append(splits[1]);
                            mediaType = mediaTypeBuilder.toString();
                        }
                    }
                    updatedContent.addMediaType(mediaType, currentMedia.getValue());
                }
            }
            response.setContent(updatedContent);
            updatedResponse.addApiResponse(next.getKey(), response);
        }
        return updatedResponse;
    }

    /**
     * Update all the {@link CacheConfigAnnotation} details with annotation fields.
     */
    private CacheConfigAnnotation setCacheConfigValues(SeparatedNodeList<MappingFieldNode> fields) {
        CacheConfigAnnotation cacheConfig = new CacheConfigAnnotation();
        // when field values has -- create a function for have the default values.
        Spliterator<MappingFieldNode> spliterator = fields.spliterator();
        spliterator.forEachRemaining(field -> {
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
    private Optional<ApiResponses> getAPIResponses(OperationAdaptor operationAdaptor,
                                                   Node typeNode, Optional<String> customMediaPrefix,
                                                   Map<String, Header> headers) {
        ApiResponse apiResponse = new ApiResponse();
        ApiResponses apiResponses = new ApiResponses();
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;
        if (typeNode.parent().kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
            ApiResponse acceptedRequestResponse = new ApiResponse();
            acceptedRequestResponse.description(ACCEPTED);
            apiResponses.put(HTTP_202, acceptedRequestResponse);
            Operation operation = operationAdaptor.getOperation();
            if (operation.getRequestBody() != null || operation.getParameters() != null) {
                ApiResponse badRequestResponse = new ApiResponse();
                badRequestResponse.description(BAD_REQUEST);
                apiResponses.put(HTTP_400, badRequestResponse);
            }
        }
        String mediaTypeString;
        switch (typeNode.kind()) {
            case QUALIFIED_NAME_REFERENCE:
                QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                return handleQualifiedNameType(apiResponses, customMediaPrefix, headers, apiResponse, qNode);
            case FLOAT_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case INT_TYPE_DESC:
            case STRING_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
                apiResponse = setCacheHeader(headers, apiResponse, statusCode);
                String type = typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
                Schema<?> schema = ConverterCommonUtils.getOpenApiSchema(type);
                Optional<String> media = convertBallerinaMIMEToOASMIMETypes(type, customMediaPrefix);
                if (media.isPresent()) {
                    mediaType.setSchema(schema);
                    apiResponse.description(description);
                    apiResponse.content(new Content().addMediaType(media.get(), mediaType));
                    apiResponses.put(statusCode, apiResponse);
                    return Optional.of(apiResponses);
                } else {
                    return Optional.empty();
                }
            case JSON_TYPE_DESC:
                setCacheHeader(headers, apiResponse, statusCode);
                mediaType.setSchema(new ObjectSchema());
                mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + JSON_POSTFIX)
                        .orElse(APPLICATION_JSON);
                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
                apiResponse.description(description);
                apiResponses.put(statusCode, apiResponse);
                return Optional.of(apiResponses);
            case XML_TYPE_DESC:
                setCacheHeader(headers, apiResponse, statusCode);
                mediaType.setSchema(new ObjectSchema());
                mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + XML_POSTFIX)
                        .orElse(MediaType.APPLICATION_XML);
                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
                apiResponse.description(description);
                apiResponses.put(statusCode, apiResponse);
                return Optional.of(apiResponses);
            case SIMPLE_NAME_REFERENCE:
                SimpleNameReferenceNode recordNode = (SimpleNameReferenceNode) typeNode;
                Map<String, Schema> schemas = components.getSchemas();
                handleReferenceResponse(operationAdaptor, recordNode, schemas, apiResponses, customMediaPrefix,
                        headers);
                return Optional.of(apiResponses);
            case UNION_TYPE_DESC:
                return mapUnionReturns(operationAdaptor,
                        (UnionTypeDescriptorNode) typeNode, customMediaPrefix, headers);
            case RECORD_TYPE_DESC:
                return mapInlineRecordInReturn(operationAdaptor, apiResponses,
                        (RecordTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
            case ARRAY_TYPE_DESC:
                return getApiResponsesForArrayTypes(operationAdaptor, apiResponses,
                        (ArrayTypeDescriptorNode) typeNode, apiResponse, mediaType, customMediaPrefix, headers);
            case ERROR_TYPE_DESC:
                // Return type is given as error or error? in the ballerina it will generate 500 response.
                apiResponse.description(HTTP_500_DESCRIPTION);
                ObjectSchema errorSchema = new ObjectSchema();
                // Create ErrorPayload object properties.
                Map<String, Schema> properties = new HashMap<>();
                properties.put("timestamp", new StringSchema().description("Timestamp of the error"));
                properties.put("status", new IntegerSchema().description("Relevant HTTP status code"));
                properties.put("reason", new StringSchema().description("Reason phrase"));
                properties.put("message", new StringSchema().description("Error message"));
                properties.put("path", new StringSchema().description("Request path"));
                properties.put("method", new StringSchema().description("Method type of the request"));
                errorSchema.setProperties(properties);
                if (components.getSchemas() == null) {
                    components.setSchemas(new HashMap<>());
                }
                components.getSchemas().put("ErrorPayload", errorSchema);
                mediaType.setSchema(new Schema<>().$ref("ErrorPayload"));
                apiResponse.content(new Content().addMediaType(APPLICATION_JSON, mediaType));
                apiResponses.put(HTTP_500, apiResponse);
                return Optional.of(apiResponses);
            case OPTIONAL_TYPE_DESC:
                return getAPIResponses(operationAdaptor,
                        ((OptionalTypeDescriptorNode) typeNode).typeDescriptor(), customMediaPrefix, headers);
            case MAP_TYPE_DESC:
                setCacheHeader(headers, apiResponse, statusCode);
                MapTypeDescriptorNode mapNode = (MapTypeDescriptorNode) typeNode;
                ObjectSchema objectSchema = new ObjectSchema();
                Schema<?> apiSchema = getOpenApiSchema(mapNode.mapTypeParamsNode().typeNode().kind());
                objectSchema.additionalProperties(apiSchema);
                mediaType.setSchema(objectSchema);
                mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + JSON_POSTFIX)
                        .orElse(APPLICATION_JSON);
                apiResponse.content(new Content().addMediaType(mediaTypeString, mediaType));
                apiResponse.description(description);
                apiResponses.put(statusCode, apiResponse);
                return Optional.of(apiResponses);
            default:
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_101;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage, this.location,
                        typeNode.kind().toString());
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
            if (typeName.equals(HTTP_RESPONSE)) {
                apiResponse = new ApiResponse();
                apiResponse.description("Any Response");
                Content content = new Content();
                io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
                content.put(WILD_CARD_CONTENT_KEY, mediaType);
                apiResponse.setContent(content);
                apiResponses.put(Constants.DEFAULT, apiResponse);
                return Optional.of(apiResponses);
            } else {
                Optional<String> code = generateApiResponseCode(qNode.identifier().toString().trim());
                if (code.isPresent()) {
                    apiResponse.description(qNode.identifier().toString().trim());
                    setCacheHeader(headers, apiResponse, code.get());
                    apiResponses.put(code.get(), apiResponse);
                    return Optional.of(apiResponses);
                } else {
                    return Optional.empty();
                }
            }
        } else {
            Symbol symbol = semanticModel.symbol(qNode).get();
            if (symbol instanceof TypeReferenceTypeSymbol) {
                TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) symbol;
                TypeSymbol typeSymbol = typeRef.typeDescriptor();
                if (typeSymbol.typeKind() == TypeDescKind.INTERSECTION) {
                    List<TypeSymbol> memberTypes = ((IntersectionTypeSymbol) typeSymbol).memberTypeDescriptors();
                    for (TypeSymbol memberType : memberTypes) {
                        if (!(memberType instanceof ReadonlyTypeSymbol)) {
                            //TODO : address the rest of the simple name reference types
                            typeSymbol = memberType;
                            break;
                        }
                    }
                }
                if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
                    ApiResponses responses = handleRecordTypeSymbol(qNode.identifier().text().trim(),
                            components.getSchemas(), customMediaPrefix, typeRef,
                            new OpenAPIComponentMapper(components, moduleMemberVisitor),
                            headers);
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
                                                                Optional<String> customMediaPrefix,
                                                                Map<String, Header> headers) {
        String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;

        Map<String, Schema> schemas = components.getSchemas();
        if (array.memberTypeDesc().kind() == SIMPLE_NAME_REFERENCE) {
            handleReferenceResponse(operationAdaptor, (SimpleNameReferenceNode) array.memberTypeDesc(),
                    schemas, apiResponses, customMediaPrefix, headers);
        } else if (array.memberTypeDesc().kind() == QUALIFIED_NAME_REFERENCE) {
            Optional<ApiResponses> optionalAPIResponses =
                    handleQualifiedNameType(new ApiResponses(), customMediaPrefix, headers, apiResponse,
                            (QualifiedNameReferenceNode) array.memberTypeDesc());
            if (optionalAPIResponses.isPresent()) {
                ApiResponses responses = optionalAPIResponses.get();
                updateResponseWithArraySchema(responses);
                apiResponses.putAll(responses);
            }
        } else {
            ArraySchema arraySchema = new ArraySchema();
            String type = array.memberTypeDesc().kind().toString().trim().split("_")[0].
                    toLowerCase(Locale.ENGLISH);
            type = type.equals(BYTE) ? BYTE_ARRAY : type;
            Schema<?> openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
            Optional<String> mimeType = convertBallerinaMIMEToOASMIMETypes(type, customMediaPrefix);
            if (mimeType.isEmpty()) {
                return Optional.empty();
            }
            if (type.equals(BYTE_ARRAY)) {
                mediaType.setSchema(openApiSchema);
            } else {
                arraySchema.setItems(openApiSchema);
                mediaType.setSchema(arraySchema);
            }
            apiResponse.description(description);
            apiResponse.content(new Content().addMediaType(mimeType.get(), mediaType));
            apiResponses.put(statusCode, apiResponse);
        }
        return Optional.of(apiResponses);
    }

    /**
     * Update given response schema type with array schema.
     *
     * @param responses api responses related to return type.
     */
    private void updateResponseWithArraySchema(ApiResponses responses) {

        for (Map.Entry<String, ApiResponse> responseEntry : responses.entrySet()) {
            Content content = responseEntry.getValue().getContent();
            for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> mediaTypeEntry :
                    content.entrySet()) {
                io.swagger.v3.oas.models.media.MediaType value = mediaTypeEntry.getValue();
                Schema<?> schema = value.getSchema();
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setItems(schema);
                value.setSchema(arraySchema);
                content.remove(mediaTypeEntry.getKey());
                content.put(mediaTypeEntry.getKey(), value);
            }
            responses.remove(responseEntry.getKey());
            responses.addApiResponse(responseEntry.getKey(),
                    new ApiResponse().content(content).description(responseEntry.getValue().getDescription()));
        }
    }

    /**
     * Handle response has inline record as return type.
     */
    private Optional<ApiResponses> mapInlineRecordInReturn(OperationAdaptor operationAdaptor, ApiResponses apiResponses,
                                                           RecordTypeDescriptorNode typeNode, ApiResponse apiResponse,
                                                           io.swagger.v3.oas.models.media.MediaType mediaType,
                                                           Optional<String> customMediaPrefix,
                                                           Map<String, Header> headers) {
        String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;

        NodeList<Node> fields = typeNode.fields();
        Optional<String> httpCode = Optional.of(statusCode);
        Schema<?> inlineSchema = new Schema<>();
        Optional<String> mediaTypeResponse = Optional.of(APPLICATION_JSON);
        boolean ishttpTypeInclusion = false;
        Map<String, Schema> properties = new HashMap<>();
        if (fields.stream().anyMatch(module -> module.kind() == TYPE_REFERENCE)) {
            ishttpTypeInclusion = true;
        }
        // TODO: 1- scenarios returns record {| *http:Ok; Person body;|}
        // TODO: 2- scenarios returns record {| *http:Ok; string body;|}
        // 3- scenarios returns record {| int id; string body;|} - done
        // 4- scenarios returns record {| int id; Person body;|} - done

        for (Node field : fields) {
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
                    SimpleNameReferenceNode nameRefNode = (SimpleNameReferenceNode) type01;
                    handleReferenceResponse(operationAdaptor, nameRefNode, componentsSchemas, apiResponses,
                            customMediaPrefix, headers);
                    Schema<?> referenceSchema = new Schema<>();
                    referenceSchema.set$ref(ConverterCommonUtils.unescapeIdentifier(
                            recordField.typeName().toString().trim()));
                    properties.put(recordField.fieldName().text(), referenceSchema);
                } else {
                    //TODO array fields handling
                    mediaTypeResponse = convertBallerinaMIMEToOASMIMETypes(recordField.typeName().toString().trim(),
                            customMediaPrefix);
                    Schema<?> propertySchema = ConverterCommonUtils.getOpenApiSchema(
                            recordField.typeName().toString().trim());
                    properties.put(recordField.fieldName().text(), propertySchema);
                }
            }
        }
        if (!ishttpTypeInclusion) {
            inlineSchema = new ObjectSchema();
            inlineSchema.setProperties(properties);
            mediaTypeResponse = Optional.of(APPLICATION_JSON);
            if (customMediaPrefix.isPresent()) {
                mediaTypeResponse = Optional.of(APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX);
            }
        }
        mediaType.setSchema(inlineSchema);
        apiResponse.description(description);
        if (mediaTypeResponse.isPresent() && httpCode.isPresent()) {
            apiResponse.content(new Content().addMediaType(mediaTypeResponse.get(), mediaType));
            setCacheHeader(headers, apiResponse, httpCode.get());
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
    private Optional<ApiResponses> mapUnionReturns(OperationAdaptor operationAdaptor,
                                                   UnionTypeDescriptorNode typeNode, Optional<String> customMediaPrefix
            , Map<String, Header> headers) {

        TypeDescriptorNode rightNode = typeNode.rightTypeDesc();
        TypeDescriptorNode leftNode = typeNode.leftTypeDesc();
        ApiResponses apiResponses = new ApiResponses();
        // Handle leftNode because it is main node
        Optional<ApiResponses> apiResponse = getAPIResponses(operationAdaptor, leftNode,
                customMediaPrefix, headers);
        addResponse(apiResponses, apiResponse);
        // Handle rest of the union type
        if (rightNode instanceof UnionTypeDescriptorNode) {
            UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
            while (traversRightNode.rightTypeDesc() != null) {
                if (leftNode.kind() == QUALIFIED_NAME_REFERENCE) {
                    leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
                    Optional<ApiResponses> apiResponsesLeft = getAPIResponses(operationAdaptor, leftNode,
                            customMediaPrefix, headers);
                    addResponse(apiResponses, apiResponsesLeft);
                }
            }
        } else {
            Optional<ApiResponses> apiResponsesRight = getAPIResponses(operationAdaptor, rightNode,
                    customMediaPrefix, headers);
            addResponse(apiResponses, apiResponsesRight);
        }
        return Optional.of(apiResponses);
    }

    private static void addResponse(ApiResponses apiResponses, Optional<ApiResponses> apiResponse) {
        apiResponse.ifPresent(responses -> responses.forEach((key, value) -> {
            if (apiResponses.containsKey(key)) {
                ApiResponse res = apiResponses.get(key);
                if (value.getContent() != null) {
                    Content content = res.getContent();
                    if (content == null) {
                        content = new Content();
                    }
                    String mediaType = value.getContent().keySet().iterator().next();
                    Schema newSchema = value.getContent().values().iterator().next().getSchema();
                    if (content.containsKey(mediaType)) {
                        Schema<?> schema = content.get(mediaType).getSchema();
                        if (schema instanceof ComposedSchema && ((ComposedSchema) schema).getOneOf() != null) {
                            schema.getOneOf().add(newSchema);
                            content.put(mediaType, new io.swagger.v3.oas.models.media.MediaType().schema(schema));
                        } else {
                            ComposedSchema composedSchema = new ComposedSchema();
                            composedSchema.addOneOfItem(schema);
                            composedSchema.addOneOfItem(newSchema);
                            io.swagger.v3.oas.models.media.MediaType updatedMediaContent =
                                    new io.swagger.v3.oas.models.media.MediaType().schema(composedSchema);
                            content.put(mediaType, updatedMediaContent);
                        }
                    } else {
                        content.put(mediaType, value.getContent().values().iterator().next());
                    }
                    res.content(content);
                }
                apiResponses.put(key, res);
            } else {
                apiResponses.put(key, value);
            }
        }));
    }

    /**
     * Convert ballerina MIME types to OAS MIME types.
     */
    private Optional<String> convertBallerinaMIMEToOASMIMETypes(String type, Optional<String> customMediaPrefix) {
        switch (type) {
            case APPLICATION_JSON:
            case Constants.JSON:
            case Constants.INT:
            case Constants.FLOAT:
            case Constants.DECIMAL:
            case Constants.BOOLEAN:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + JSON_POSTFIX)
                        .orElse(APPLICATION_JSON));
            case MediaType.APPLICATION_XML:
            case Constants.XML:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + XML_POSTFIX).
                        orElse(MediaType.APPLICATION_XML));
            case Constants.BYTE_ARRAY:
                return Optional.of(customMediaPrefix.map(s -> APPLICATION_PREFIX + s + OCTECT_STREAM_POSTFIX)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM));
            case MediaType.TEXT_PLAIN:
            case Constants.STRING:
                return Optional.of(customMediaPrefix.map(s -> TEXT_PREFIX + s +
                        TEXT_POSTFIX).orElse(MediaType.TEXT_PLAIN));
            case MediaType.TEXT_HTML:
                return Optional.of(customMediaPrefix.map(s -> TEXT_PREFIX + s +
                        HTML_POSTFIX).orElse(MediaType.TEXT_HTML));
            case MediaType.TEXT_XML:
                return Optional.of(customMediaPrefix.map(s -> TEXT_PREFIX + s +
                        XML_POSTFIX).orElse(MediaType.TEXT_XML));
            default:
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_102;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage, this.location,
                        type);
                if (customMediaPrefix.isEmpty()) {
                    errors.add(error);
                }
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
            return Optional.empty();
        }
    }

    private void handleReferenceResponse(OperationAdaptor operationAdaptor, SimpleNameReferenceNode referenceNode,
                                         Map<String, Schema> schema, ApiResponses apiResponses,
                                         Optional<String> customMediaPrefix, Map<String, Header> headers) {
        ApiResponse apiResponse = new ApiResponse();
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handle record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, moduleMemberVisitor);
        String mediaTypeString;
        // Check typeInclusion is related to the http status code
        if (referenceNode.parent().kind().equals(ARRAY_TYPE_DESC)) {
            String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
            String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;
            setCacheHeader(headers, apiResponse, statusCode);
            io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
            ArraySchema arraySchema = new ArraySchema();
            componentMapper.createComponentSchema(schema, typeSymbol);
            errors.addAll(componentMapper.getDiagnostics());
            arraySchema.setItems(new Schema<>().$ref(ConverterCommonUtils.unescapeIdentifier(
                    referenceNode.name().toString().trim())));
            media.setSchema(arraySchema);
            apiResponse.description(description);
            mediaTypeString = customMediaPrefix.isPresent() ? APPLICATION_PREFIX + customMediaPrefix + JSON_POSTFIX :
                    APPLICATION_JSON;
            apiResponse.content(new Content().addMediaType(mediaTypeString, media));
            apiResponses.put(statusCode, apiResponse);

        } else if (typeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            TypeSymbol referredTypeSymbol = typeReferenceTypeSymbol.typeDescriptor();
            if (referredTypeSymbol.typeKind() == TypeDescKind.INTERSECTION) {
                referredTypeSymbol = componentMapper.excludeReadonlyIfPresent(referredTypeSymbol);
            }
            String referenceName = referenceNode.name().toString().trim();
            String referredTypeName = referredTypeSymbol.getName().isPresent() ?
                    referredTypeSymbol.getName().get() : "";

            if (referredTypeSymbol.typeKind() == TypeDescKind.RECORD) {
                ApiResponses responses = handleRecordTypeSymbol(referenceName, schema, customMediaPrefix,
                        typeReferenceTypeSymbol, componentMapper, headers);
                apiResponses.putAll(responses);
            } else if (referredTypeSymbol.typeKind() == TypeDescKind.ERROR) {
                io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                apiResponse.description(HTTP_500_DESCRIPTION);
                mediaType.setSchema(new StringSchema());
                apiResponse.content(new Content().addMediaType(MediaType.TEXT_PLAIN, mediaType));
                apiResponses.put(HTTP_500, apiResponse);
            } else if (referredTypeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE &&
                    generateApiResponseCode(referredTypeName).isPresent()) {
                Optional<String> code = generateApiResponseCode(referredTypeName);
                apiResponse.description(referredTypeName);
                setCacheHeader(headers, apiResponse, code.get());
                apiResponses.put(code.get(), apiResponse);
            } else {
                ApiResponses responses = createResponseForTypeReferenceTypeReturns(referenceName,
                        typeReferenceTypeSymbol, schema, customMediaPrefix,
                        componentMapper, headers);
                apiResponses.putAll(responses);
            }
        }
        //Check content and status code if it is in 200 range then add the header
        operationAdaptor.getOperation().setResponses(apiResponses);
    }

    /**
     * Create responses when http errors are present in the union type.
     *
     * @param unionTypeSymbol union type symbol
     * @param headers         headers
     * @return api responses
     */
    public Optional<ApiResponses> createResponsesForErrorsInUnion(UnionTypeSymbol unionTypeSymbol,
                                                                  Map<String, Header> headers) {
        ApiResponses apiResponses = new ApiResponses();

        for (TypeSymbol memberTypeDescriptor : unionTypeSymbol.memberTypeDescriptors()) {
            memberTypeDescriptor.getName().ifPresent(name -> {
                if (HTTP_CODES.containsKey(name)) {
                    ApiResponse apiResponse = new ApiResponse();
                    Optional<String> code = generateApiResponseCode(name);
                    apiResponse.description(name);
                    setCacheHeader(headers, apiResponse, code.get());
                    apiResponses.put(code.get(), apiResponse);
                }
            });
        }
        return apiResponses.isEmpty() ? Optional.empty() : Optional.of(apiResponses);
    }

    private ApiResponses handleRecordTypeSymbol(String referenceName, Map<String, Schema> schema,
                                                Optional<String> customMediaPrefix, TypeReferenceTypeSymbol typeSymbol,
                                                OpenAPIComponentMapper componentMapper,
                                                Map<String, Header> headers) {
        //Handle both intersection and record
        ApiResponses apiResponses = new ApiResponses();
        RecordTypeSymbol returnRecord = null;
        if (typeSymbol.typeDescriptor().typeKind() == TypeDescKind.RECORD) {
            returnRecord = (RecordTypeSymbol) typeSymbol.typeDescriptor();
        } else if (typeSymbol.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
            IntersectionTypeSymbol typeIntersection = (IntersectionTypeSymbol) typeSymbol.typeDescriptor();
            for (TypeSymbol memberTypeDescriptor : typeIntersection.memberTypeDescriptors()) {
                if (memberTypeDescriptor.typeKind() == TypeDescKind.RECORD) {
                    returnRecord = (RecordTypeSymbol) memberTypeDescriptor;
                }
            }
        }
        if (returnRecord == null) {
            return apiResponses;
        }
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

    /**
     * Create API responses when return type is type reference.
     */
    private ApiResponses createResponseForTypeReferenceTypeReturns(String referenceName,
                                                                   TypeReferenceTypeSymbol typeReferenceTypeSymbol,
                                                                   Map<String, Schema> schema,
                                                                   Optional<String> customMediaPrefix,

                                                                   OpenAPIComponentMapper componentMapper,
                                                                   Map<String, Header> headers) {

        TypeSymbol referredTypeSymbol = typeReferenceTypeSymbol.typeDescriptor();
        TypeDescKind typeDescKind = referredTypeSymbol.typeKind();
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();

        String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;

        ApiResponses apiResponses = new ApiResponses();
        if (referredTypeSymbol.typeKind() == TypeDescKind.INTERSECTION) {
            referredTypeSymbol = componentMapper.excludeReadonlyIfPresent(referredTypeSymbol);
            typeDescKind = referredTypeSymbol.typeKind();
        }

        if (typeDescKind == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) referredTypeSymbol;
            createResponsesForErrorsInUnion(unionTypeSymbol, headers).ifPresent(apiResponses::putAll);
        }

        componentMapper.createComponentSchema(schema, typeReferenceTypeSymbol);
        errors.addAll(componentMapper.getDiagnostics());
        media.setSchema(new Schema<>().$ref(ConverterCommonUtils.unescapeIdentifier(referenceName)));

        ImmutablePair<String, String> mediaTypePair = getMediaTypeForTypeReferenceTypeReturns(referredTypeSymbol,
                typeDescKind, customMediaPrefix);

        String mediaTypeString = mediaTypePair.getLeft();
        if (customMediaPrefix.isPresent()) {
            mediaTypeString = APPLICATION_PREFIX + customMediaPrefix.get() + mediaTypePair.getRight();
        }

        ApiResponse apiResponse = new ApiResponse();
        setCacheHeader(headers, apiResponse, statusCode);
        apiResponse.content(new Content().addMediaType(mediaTypeString, media));
        apiResponse.description(description);
        apiResponses.put(statusCode, apiResponse);

        return apiResponses;
    }


    private static ImmutablePair<String, String> getMediaTypeForTypeReferenceTypeReturns(TypeSymbol referredTypeSymbol,
                                                                                         TypeDescKind typeDescKind,
                                                                                         Optional<String>
                                                                                                 customMediaPrefix) {
        ImmutablePair<String, String> mediaType = null;
        switch (typeDescKind) {
            case XML:
                mediaType = new ImmutablePair<>(MediaType.APPLICATION_XML, XML_POSTFIX);
                break;
            case STRING:
                mediaType = new ImmutablePair<>(MediaType.TEXT_PLAIN, TEXT_POSTFIX);
                break;
            case BYTE:
                mediaType = new ImmutablePair<>(MediaType.APPLICATION_OCTET_STREAM, OCTECT_STREAM_POSTFIX);
                break;
            case ARRAY:
                ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) referredTypeSymbol;
                TypeSymbol memberTypeDesc = arrayTypeSymbol.memberTypeDescriptor();
                mediaType = getMediaTypeForTypeReferenceTypeReturns(memberTypeDesc, memberTypeDesc.typeKind(),
                        customMediaPrefix);
                break;
            case UNION:
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) referredTypeSymbol;
                boolean isNil = unionTypeSymbol.memberTypeDescriptors().stream().anyMatch(
                        memberTypeDescriptor -> memberTypeDescriptor.typeKind() == TypeDescKind.NIL);
                if (unionTypeSymbol.memberTypeDescriptors().size() == 2 && isNil) {
                    for (TypeSymbol memberTypeDescriptor : unionTypeSymbol.memberTypeDescriptors()) {
                        if (memberTypeDescriptor.typeKind() != TypeDescKind.NIL) {
                            mediaType = getMediaTypeForTypeReferenceTypeReturns(memberTypeDescriptor,
                                    memberTypeDescriptor.typeKind(), customMediaPrefix);
                            break;
                        }
                    }
                } else {
                    mediaType = new ImmutablePair<>(MediaType.APPLICATION_JSON, JSON_POSTFIX);
                }
                break;
            default:
                mediaType = new ImmutablePair<>(MediaType.APPLICATION_JSON, JSON_POSTFIX);
                break;
        }

        return mediaType;
    }

    private ApiResponses createResponseForRecord(String referenceName, Map<String, Schema> schema,
                                                 Optional<String> customMediaPrefix, TypeSymbol typeSymbol,
                                                 OpenAPIComponentMapper componentMapper,
                                                 io.swagger.v3.oas.models.media.MediaType media,
                                                 Map<String, Header> headers) {
        String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;
        ApiResponses apiResponses = new ApiResponses();
        String mediaTypeString;
        componentMapper.createComponentSchema(schema, typeSymbol);
        errors.addAll(componentMapper.getDiagnostics());
        media.setSchema(new Schema<>().$ref(ConverterCommonUtils.unescapeIdentifier(referenceName)));
        mediaTypeString = APPLICATION_JSON;
        if (customMediaPrefix.isPresent()) {
            mediaTypeString = APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX;
        }
        ApiResponse apiResponse = new ApiResponse();
        setCacheHeader(headers, apiResponse, statusCode);
        apiResponse.content(new Content().addMediaType(mediaTypeString, media));
        apiResponse.description(description);
        apiResponses.put(statusCode, apiResponse);
        return apiResponses;
    }

    private Optional<ApiResponses> handleRecordHasHttpTypeInclusionField(Map<String, Schema> schema,
                                                                         TypeSymbol typeSymbol,
                                                                         OpenAPIComponentMapper componentMapper,
                                                                         RecordTypeSymbol returnRecord,
                                                                         List<TypeSymbol> typeInclusions,
                                                                         Optional<String> customMediaPrefix,
                                                                         Map<String, Header> headers) {
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
                Map<String, RecordFieldSymbol> fieldsOfRecord = returnRecord.fieldDescriptors();
                // Handle the response headers
                RecordFieldSymbol header = fieldsOfRecord.get(RESPONSE_HEADERS);
                extractResponseHeaders(headers, header);
                setCacheHeader(headers, apiResponse, code.get());

                // since `NoContent` status code doesn't have body for return
                // payload, we need to leave it with only description instead of mapping in content type to
                // OAS.
                if (code.get().equals(HTTP_204)) {
                    apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                    apiResponses.put(code.get(), apiResponse);
                    return Optional.of(apiResponses);
                }

                // Handle the content of the response
                RecordFieldSymbol body = fieldsOfRecord.get(BODY);
                TypeDescKind typeDescKind = body.typeDescriptor().typeKind();
                if (typeDescKind == TypeDescKind.TYPE_REFERENCE) {
                    componentMapper.createComponentSchema(schema, body.typeDescriptor());
                    errors.addAll(componentMapper.getDiagnostics());
                    media.setSchema(new Schema<>().$ref(ConverterCommonUtils.unescapeIdentifier(
                            body.typeDescriptor().getName().orElseThrow().trim())));
                    mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + JSON_POSTFIX)
                            .orElse(APPLICATION_JSON);
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                } else if (typeDescKind == TypeDescKind.STRING) {
                    media.setSchema(new StringSchema());
                    mediaTypeString = customMediaPrefix.map(s -> TEXT_PREFIX + s +
                            TEXT_POSTFIX).orElse(MediaType.TEXT_PLAIN);
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                } else if (typeDescKind == TypeDescKind.XML) {
                    media.setSchema(new ObjectSchema());
                    mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + XML_POSTFIX)
                            .orElse(MediaType.APPLICATION_XML);
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                } else if (typeDescKind == TypeDescKind.MAP &&
                        (((MapTypeSymbol) body.typeDescriptor()).typeParam().typeKind() == TypeDescKind.STRING)) {
                    mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + X_WWW_FORM_URLENCODED_POSTFIX)
                            .orElse(MediaType.APPLICATION_FORM_URLENCODED);
                    Schema<?> objectSchema = new ObjectSchema();
                    objectSchema.additionalProperties(new StringSchema());
                    media.setSchema(objectSchema);
                    apiResponse.content(new Content().addMediaType(mediaTypeString, media));
                } else if (typeDescKind == TypeDescKind.UNION) {
                    UnionTypeSymbol unionType = (UnionTypeSymbol) body.typeDescriptor();
                    List<TypeSymbol> typeSymbols = unionType.memberTypeDescriptors();
                    //store media type based on the members of the union type
                    Map<String, Schema> contentDetails = new LinkedHashMap<>();
                    for (TypeSymbol type : typeSymbols) {
                        ImmutablePair<String, String> mediaTypes =
                                getMediaTypeForTypeReferenceTypeReturns(type, type.typeKind(), customMediaPrefix);
                        mediaTypeString = customMediaPrefix.map(s -> APPLICATION_PREFIX + s + mediaTypes.getRight())
                                .orElseGet(mediaTypes::getLeft);
                        Schema<?> mediaSchema = getOpenApiSchema(type.typeKind().getName());
                        if (type.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                            componentMapper.createComponentSchema(schema, type);
                            errors.addAll(componentMapper.getDiagnostics());
                            String recordName = type.getName().orElseThrow().trim();
                            mediaSchema.set$ref(ConverterCommonUtils.unescapeIdentifier(recordName));
                        }
                        if (contentDetails.containsKey(mediaTypeString)) {
                            Schema<?> oldSchema = contentDetails.get(mediaTypeString);
                            if (oldSchema instanceof ComposedSchema && oldSchema.getOneOf() != null) {
                                oldSchema.getOneOf().add(mediaSchema);
                                contentDetails.put(mediaTypeString, oldSchema);
                            } else {
                                ComposedSchema composedSchema = new ComposedSchema();
                                composedSchema.addOneOfItem(oldSchema);
                                composedSchema.addOneOfItem(mediaSchema);
                                contentDetails.put(mediaTypeString, composedSchema);
                            }
                        } else {
                            contentDetails.put(mediaTypeString, mediaSchema);
                        }
                    }
                    Content content = new Content();
                    for (Map.Entry<String, Schema> entry : contentDetails.entrySet()) {
                        io.swagger.v3.oas.models.media.MediaType mediaType =
                                new io.swagger.v3.oas.models.media.MediaType();

                        mediaTypeString = entry.getKey();
                        mediaType.setSchema(entry.getValue());
                        content.addMediaType(mediaTypeString, mediaType);
                    }
                    apiResponse.content(content);
                }
                apiResponse.description(typeInSymbol.getName().orElseThrow().trim());
                apiResponses.put(code.get(), apiResponse);
            }
        }
        if (!isHttpModule) {
            componentMapper.createComponentSchema(schema, typeSymbol);
            errors.addAll(componentMapper.getDiagnostics());
            media.setSchema(new Schema<>().$ref(ConverterCommonUtils.unescapeIdentifier(typeSymbol.getName().get())));
            mediaTypeString = APPLICATION_JSON;
            if (customMediaPrefix.isPresent()) {
                mediaTypeString = APPLICATION_PREFIX + customMediaPrefix.get() + JSON_POSTFIX;
            }
            String statusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
            String description = httpMethod.equals(POST) ? HTTP_201_DESCRIPTION : HTTP_200_DESCRIPTION;

            setCacheHeader(headers, apiResponse, statusCode);
            apiResponse.content(new Content().addMediaType(mediaTypeString, media));
            apiResponse.description(description);
            apiResponses.put(statusCode, apiResponse);
        }
        return Optional.of(apiResponses);
    }

    /**
     * This function is to handle headers which come with response.
     *
     * @param headers Headers already generated for cache config
     * @param header  Response header field
     */
    private void extractResponseHeaders(Map<String, Header> headers, RecordFieldSymbol header) {

        if (header.typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol headerType = (TypeReferenceTypeSymbol) header.typeDescriptor();
            if (headerType.typeDescriptor() instanceof RecordTypeSymbol) {
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) headerType.typeDescriptor();
                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> field : rfields.entrySet()) {
                    Header headerSchema = new Header();
                    TypeSymbol fieldType = field.getValue().typeDescriptor();
                    String type = fieldType.typeKind().toString().trim().toLowerCase(Locale.ENGLISH);
                    Schema<?> openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
                    if (fieldType instanceof ArrayTypeSymbol) {
                        ArrayTypeSymbol array = (ArrayTypeSymbol) fieldType;
                        TypeSymbol itemType = array.memberTypeDescriptor();
                        String item = itemType.typeKind().toString().trim().toLowerCase(Locale.ENGLISH);
                        Schema<?> itemSchema = ConverterCommonUtils.getOpenApiSchema(item);
                        if (openApiSchema instanceof ArraySchema) {
                            ((ArraySchema) openApiSchema).setItems(itemSchema);
                        }
                    }
                    headerSchema.setSchema(openApiSchema);
                    Optional<Documentation> fieldDoc = ((Documentable) field.getValue()).documentation();
                    if (fieldDoc.isPresent() && fieldDoc.get().description().isPresent()) {
                        headerSchema.setDescription(fieldDoc.get().description().get().trim());
                    }
                    // Normalized header key values
                    String headerKey = getValidHeaderKey(field);
                    headers.put(headerKey, headerSchema);
                }
            }
        }
    }

    /**
     * This function to normalized header key value to OAS key value.
     * ex: ballerina header key x\-rate\-limit\-id -> x-rate-limit-id
     *
     * @param field header flied
     */
    private String getValidHeaderKey(Map.Entry<String, RecordFieldSymbol> field) {
        return field.getKey().replaceAll("\\\\", "");
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
