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

package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.type.ComponentMapper;
import io.ballerina.openapi.service.mapper.type.RecordTypeMapper;
import io.ballerina.openapi.service.mapper.type.ReferenceTypeMapper;
import io.ballerina.openapi.service.model.CacheConfigAnnotation;
import io.ballerina.openapi.service.model.OperationAdaptor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static io.ballerina.openapi.service.Constants.CACHE_CONTROL;
import static io.ballerina.openapi.service.Constants.ETAG;
import static io.ballerina.openapi.service.Constants.FALSE;
import static io.ballerina.openapi.service.Constants.HTTP_200;
import static io.ballerina.openapi.service.Constants.HTTP_201;
import static io.ballerina.openapi.service.Constants.HTTP_500;
import static io.ballerina.openapi.service.Constants.HTTP_CODES;
import static io.ballerina.openapi.service.Constants.HTTP_CODE_DESCRIPTIONS;
import static io.ballerina.openapi.service.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.service.Constants.LAST_MODIFIED;
import static io.ballerina.openapi.service.Constants.MAX_AGE;
import static io.ballerina.openapi.service.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.service.Constants.MUST_REVALIDATE;
import static io.ballerina.openapi.service.Constants.NO_CACHE;
import static io.ballerina.openapi.service.Constants.NO_STORE;
import static io.ballerina.openapi.service.Constants.NO_TRANSFORM;
import static io.ballerina.openapi.service.Constants.POST;
import static io.ballerina.openapi.service.Constants.PRIVATE;
import static io.ballerina.openapi.service.Constants.PROXY_REVALIDATE;
import static io.ballerina.openapi.service.Constants.PUBLIC;
import static io.ballerina.openapi.service.Constants.S_MAX_AGE;
import static io.ballerina.openapi.service.Constants.TRUE;
import static io.ballerina.openapi.service.utils.MapperCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.service.utils.MapperCommonUtils.extractCustomMediaType;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * This class uses to map the Ballerina return details to the OAS response.
 *
 * @since 2.0.0
 */
public class OpenAPIResponseMapper {

    private final SemanticModel semanticModel;
    private final Components components;
    private final List<OpenAPIMapperDiagnostic> errors = new ArrayList<>();

    public List<OpenAPIMapperDiagnostic> getErrors() {
        return errors;
    }

    private static final String JSON_PATTERN = "^(application|text)\\/(.*[.+-]|)json$";
    private static final String XML_PATTERN = "^(application|text)\\/(.*[.+-]|)xml$";
    private static final String TEXT_PATTERN = "^(text)\\/(.*[.+-]|)plain$";
    private static final String OCTET_STREAM_PATTERN = "^(application)\\/(.*[.+-]|)octet-stream$";

    public OpenAPIResponseMapper(SemanticModel semanticModel, Components components) {
        this.semanticModel = semanticModel;
        this.components = components;
    }

    /**
     * This function for mapping the function return type details with OAS response schema.
     *
     * @param resource         FunctionDefinitionNode for relevant resource function.
     * @param operationAdaptor OperationAdaptor model for holding operation details specific to HTTP operation.
     */
    public void getResourceOutput(FunctionDefinitionNode resource, OperationAdaptor operationAdaptor) {

        String httpMethod = operationAdaptor.getHttpOperation().toUpperCase(Locale.ENGLISH);
        ResponseAnnotation responseAnnotation = extractAnnotationDetails(resource);
        String defaultStatusCode = httpMethod.equals(POST) ? HTTP_201 : HTTP_200;
        Optional<Symbol> symbol = semanticModel.symbol(resource);
        if (symbol.isEmpty() || !(symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol)) {
            return;
        }
        Optional<TypeSymbol> returnTypeOpt = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
        Operation operation = operationAdaptor.getOperation();
        Optional<String> optionalMediaTypePrefix = extractCustomMediaType(resource);
        String mediaTypePrefix = optionalMediaTypePrefix.orElse("");
        if (returnTypeOpt.isPresent()) {
            ApiResponses apiResponses = new ApiResponses();
            addResponseMapping(returnTypeOpt.get(), defaultStatusCode, apiResponses,
                    responseAnnotation, mediaTypePrefix);
            operation.setResponses(apiResponses);
        }
    }

    private void addResponseMapping(TypeSymbol returnType, String defaultStatusCode,
                                    ApiResponses apiResponses, ResponseAnnotation responseAnnotation,
                                    String mediaTypePrefix) {
        UnionTypeSymbol unionType = getUnionType(returnType);
        if (Objects.nonNull(unionType)) {
            getResponseMappingForUnion(defaultStatusCode, unionType, apiResponses, responseAnnotation, mediaTypePrefix);
        } else {
            getResponseMappingForSimpleType(returnType, defaultStatusCode, apiResponses,
                    responseAnnotation, mediaTypePrefix);
        }
    }

    private void getResponseMappingForSimpleType(TypeSymbol returnType, String defaultStatusCode,
                                                 ApiResponses apiResponses, ResponseAnnotation responseAnnotation,
                                                 String mediaTypePrefix) {
        if (isSubTypeOfNil(returnType)) {
            getResponseMappingForNil(apiResponses);
        } else if (isSubTypeOfHttpResponse(returnType)) {
            getResponseMappingForHttpResponse(apiResponses, responseAnnotation);
        } else if (isSubTypeOfHttpStatusCodeResponse(returnType)) {
            TypeSymbol bodyType = getBodyTypeFromStatusCodeResponse(returnType, semanticModel);
            Map<String, Header> headers = getHeadersFromStatusCodeResponse(returnType);
            headers.putAll(responseAnnotation.headers());
            ResponseAnnotation newResponseAnnotation = new ResponseAnnotation(responseAnnotation.allowedMediaTypes(),
                    headers);
            addResponseMapping(bodyType, getResponseCode(returnType, defaultStatusCode),
                    apiResponses, newResponseAnnotation, mediaTypePrefix);
        } else {
            ApiResponse apiResponse = new ApiResponse();
            String mediaType = getMediaTypeFromType(returnType, mediaTypePrefix,
                    responseAnnotation.allowedMediaTypes());
            addResponseContent(returnType, apiResponse, mediaType);
            String statusCode = getResponseCode(returnType, defaultStatusCode);
            apiResponse.description(HTTP_CODE_DESCRIPTIONS.get(statusCode));
            if (isSuccessStatusCode(statusCode)) {
                addHeaders(responseAnnotation, apiResponse);
            }
            apiResponses.put(statusCode, apiResponse);
        }
    }

    private void addResponseContent(TypeSymbol returnType, ApiResponse apiResponse, String mediaType) {
        if (!isPlainAnydataType(returnType)) {
            MediaType mediaTypeObj = new MediaType();
            Map<String, Schema> componentSchemas = components.getSchemas();
            if (componentSchemas == null) {
                componentSchemas = new HashMap<>();
            }
            mediaTypeObj.setSchema(ComponentMapper.getTypeSchema(returnType, componentSchemas, semanticModel));
            if (!componentSchemas.isEmpty()) {
                components.setSchemas(componentSchemas);
            }
            updateApiResponseContent(apiResponse, mediaType, mediaTypeObj);
        }
    }

    private boolean isSuccessStatusCode(String statusCode) {
        return statusCode.startsWith("2");
    }

    private boolean isSubTypeOfNil(TypeSymbol typeSymbol) {
        return typeSymbol.subtypeOf(semanticModel.types().NIL);
    }

    private boolean isPlainAnydataType(TypeSymbol typeSymbol) {
        return typeSymbol.typeKind().equals(TypeDescKind.ANYDATA);
    }

    private void getResponseMappingForUnion(String defaultStatusCode, UnionTypeSymbol unionType,
                                            ApiResponses apiResponses, ResponseAnnotation responseAnnotation,
                                            String mediaTypePrefix) {
        Map<String, Map<String, TypeSymbol>> responseCodeMap = getResponseCodeMap(unionType, defaultStatusCode,
                mediaTypePrefix, responseAnnotation.allowedMediaTypes());
        for (Map.Entry<String, Map<String, TypeSymbol>> mediaTypeEntry : responseCodeMap.entrySet()) {
            Map<String, TypeSymbol> mediaTypes = mediaTypeEntry.getValue();
            String responseCode = mediaTypeEntry.getKey();
            for (Map.Entry<String, TypeSymbol> entry : mediaTypes.entrySet()) {
                TypeSymbol typeSymbol = entry.getValue();
                if (isSubTypeOfNil(typeSymbol)) {
                    getResponseMappingForNil(apiResponses);
                } else if (isSubTypeOfHttpResponse(typeSymbol)) {
                    getResponseMappingForHttpResponse(apiResponses, responseAnnotation);
                } else {
                    String mediaType = entry.getKey();
                    ApiResponse apiResponse = new ApiResponse();
                    addResponseContent(typeSymbol, apiResponse, mediaType);
                    if (isSuccessStatusCode(responseCode)) {
                        addHeaders(responseAnnotation, apiResponse);
                    }
                    addApiResponse(apiResponses, apiResponse, responseCode);
                }
            }
        }
    }

    private void addHeaders(ResponseAnnotation responseAnnotation, ApiResponse apiResponse) {
        Map<String, Header> headers = responseAnnotation.headers();
        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            apiResponse.setHeaders(headers);
        }
    }

    private void getResponseMappingForHttpResponse(ApiResponses apiResponses, ResponseAnnotation responseAnnotation) {
        MediaType mediaTypeObj = new MediaType();
        mediaTypeObj.setSchema(new Schema().description("Any type of entity body"));
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Any Response");
        apiResponse.setContent(new Content().addMediaType("*/*", mediaTypeObj));
        addHeaders(responseAnnotation, apiResponse);
        addApiResponse(apiResponses, apiResponse, "default");
    }

    private boolean isSubTypeOfHttpResponse(TypeSymbol returnType) {
        Optional<Symbol> optionalRecordSymbol = semanticModel.types().getTypeByName("ballerina", "http",
                "", "Response");
        if (optionalRecordSymbol.isPresent() &&
                optionalRecordSymbol.get() instanceof ClassSymbol classSymbol) {
            return returnType.subtypeOf(classSymbol);
        }
        return false;
    }

    private void getResponseMappingForNil(ApiResponses apiResponses) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description("Accepted");
        addApiResponse(apiResponses, apiResponse, "202");
    }

    private UnionTypeSymbol getUnionType(TypeSymbol typeSymbol) {
        if (Objects.isNull(typeSymbol)) {
            return null;
        }
        return switch (typeSymbol.typeKind()) {
            case UNION -> (UnionTypeSymbol) typeSymbol;
            case TYPE_REFERENCE -> getUnionType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
            case INTERSECTION -> getUnionType(((IntersectionTypeSymbol) typeSymbol).effectiveTypeDescriptor());
            default -> null;
        };
    }

    private ResponseAnnotation extractAnnotationDetails(FunctionDefinitionNode resource) {
        FunctionSignatureNode functionSignature = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Map<String, Header> headers = new LinkedHashMap<>();
            List<String> allowedMediaTypes = new ArrayList<>();
            if (!annotations.isEmpty()) {
                for (AnnotationNode annotation : annotations) {
                    if (annotation.annotReference().toString().trim().equals("http:Cache")) {
                        CacheConfigAnnotation cacheConfigAnn = new CacheConfigAnnotation();
                        annotation.annotValue().ifPresentOrElse(
                                value -> updateResponseHeaderWithCacheValues(headers, setCacheConfigValues(
                                        value.fields())),
                                () -> updateResponseHeaderWithCacheValues(headers, cacheConfigAnn));
                    } else if (annotation.annotReference().toString().trim().equals(HTTP_PAYLOAD)) {
                        allowedMediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE,
                                annotation, semanticModel);
                    }
                }
            }
            return new ResponseAnnotation(allowedMediaTypes, headers);
        }
        return null;
    }

    record ResponseAnnotation(List<String> allowedMediaTypes, Map<String, Header> headers) {
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

    public Map<String, Map<String, TypeSymbol>> getResponseCodeMap(UnionTypeSymbol typeSymbol, String defaultCode,
                                                                   String mediaTypePrefix,
                                                                   List<String> allowedMediaTypes) {
        List<TypeSymbol> memberTypeSymbols = getBasicMemberTypes(typeSymbol);
        Map<String, Map<String, List<TypeSymbol>>> responsesCodeMap = new HashMap<>();
        for (TypeSymbol memberTypeSymbol : memberTypeSymbols) {
            String code = getResponseCode(memberTypeSymbol, defaultCode);
            if (isHttpStatusCodeResponseType(semanticModel, memberTypeSymbol)) {
                memberTypeSymbol = getBodyTypeFromStatusCodeResponse(memberTypeSymbol, semanticModel);
            }
            String mediaType = getMediaTypeFromType(memberTypeSymbol, mediaTypePrefix, allowedMediaTypes);
            updateResponseCodeMap(responsesCodeMap, memberTypeSymbol, code, mediaType);
        }
        Map<String, Map<String, TypeSymbol>> responseCodeMap = new HashMap<>();
        for (Map.Entry<String, Map<String, List<TypeSymbol>>> mediaTypeEntry : responsesCodeMap.entrySet()) {
            Map<String, List<TypeSymbol>> mediaTypes = mediaTypeEntry.getValue();
            String code = mediaTypeEntry.getKey();
            for (Map.Entry<String, List<TypeSymbol>> entry : mediaTypes.entrySet()) {
                List<TypeSymbol> typeSymbols = entry.getValue();
                String mediaType = entry.getKey();
                if (typeSymbols.size() == 1) {
                    updateResponseCodeMap(responseCodeMap, code, mediaType, typeSymbols.get(0));
                } else {
                    TypeSymbol[] typeSymbolArray = typeSymbols.toArray(TypeSymbol[]::new);
                    TypeSymbol unionTypeSymbol = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                            typeSymbolArray).build();
                    updateResponseCodeMap(responseCodeMap, code, mediaType, unionTypeSymbol);
                }
            }
        }
        return responseCodeMap;
    }

    public static boolean isHttpStatusCodeResponseType(SemanticModel semanticModel, TypeSymbol typeSymbol) {
        Optional<Symbol> optionalStatusCodeRecordSymbol = semanticModel.types().getTypeByName("ballerina", "http",
                "", "StatusCodeResponse");
        if (optionalStatusCodeRecordSymbol.isPresent() &&
                optionalStatusCodeRecordSymbol.get() instanceof TypeDefinitionSymbol statusCodeRecordSymbol) {
            return typeSymbol.subtypeOf(statusCodeRecordSymbol.typeDescriptor());
        }
        return false;
    }

    public static TypeSymbol getBodyTypeFromStatusCodeResponse(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        TypeSymbol recordTypeSymbol = ReferenceTypeMapper.getReferredType(typeSymbol);
        if (Objects.nonNull(recordTypeSymbol) &&
                ((RecordTypeSymbol) recordTypeSymbol).fieldDescriptors().containsKey("body")) {
            return ((RecordTypeSymbol) recordTypeSymbol).fieldDescriptors().get("body").typeDescriptor();
        }
        return semanticModel.types().ANYDATA;
    }

    public Map<String, Header> getHeadersFromStatusCodeResponse(TypeSymbol typeSymbol) {
        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) ReferenceTypeMapper.getReferredType(typeSymbol);
        if (Objects.nonNull(recordTypeSymbol) && recordTypeSymbol.fieldDescriptors().containsKey("headers")) {
            TypeSymbol headersType = ReferenceTypeMapper.getReferredType(
                    recordTypeSymbol.fieldDescriptors().get("headers").typeDescriptor());
            if (Objects.nonNull(headersType) && headersType.typeKind().equals(TypeDescKind.RECORD)) {
                RecordTypeSymbol recordType = (RecordTypeSymbol) headersType;
                Map<String, RecordFieldSymbol> recordFieldMap = new LinkedHashMap<>(recordType.fieldDescriptors());
                Map<String, Schema> schemas = components.getSchemas();
                if (Objects.isNull(schemas)) {
                    schemas = new HashMap<>();
                }
                Map<String, Schema> recordFieldsMapping = RecordTypeMapper.getRecordFieldsMapping(recordFieldMap,
                        schemas, new HashSet<>(), semanticModel);
                if (!schemas.isEmpty()) {
                    components.setSchemas(schemas);
                }
                return mapRecordFieldToHeaders(recordFieldsMapping);
            }
        }
        return new HashMap<>();
    }

    public Map<String, Header> mapRecordFieldToHeaders(Map<String, Schema> recordFields) {
        Map<String, Header> headers = new HashMap<>();
        for (Map.Entry<String, Schema> entry : recordFields.entrySet()) {
            Header header = new Header();
            header.setSchema(entry.getValue());
            headers.put(entry.getKey(), header);
        }
        return headers;
    }

    private List<TypeSymbol> getBasicMemberTypes(UnionTypeSymbol unionTypeSymbol) {
        List<TypeSymbol> directMemberTypes = unionTypeSymbol.userSpecifiedMemberTypes();
        List<TypeSymbol> memberTypes = new ArrayList<>();
        for (TypeSymbol directMemberType : directMemberTypes) {
            if (isHttpStatusCodeResponseType(semanticModel, directMemberType)) {
                directMemberType = getBodyTypeFromStatusCodeResponse(directMemberType, semanticModel);
            }
            if (isSameContentType(directMemberType)) {
                memberTypes.add(directMemberType);
            } else {
                UnionTypeSymbol unionType = getUnionType(directMemberType);
                if (Objects.isNull(unionType)) {
                    memberTypes.add(directMemberType);
                    continue;
                }
                List<TypeSymbol> internalMemberTypes = getBasicMemberTypes(unionType);
                memberTypes.addAll(internalMemberTypes);
            }
        }
        return memberTypes;
    }

    private boolean isSameContentType(TypeSymbol typeSymbol) {
        if (typeSymbol.subtypeOf(semanticModel.types().STRING) || typeSymbol.subtypeOf(semanticModel.types().XML) ||
                typeSymbol.subtypeOf(semanticModel.types().builder().ARRAY_TYPE.withType(
                        semanticModel.types().BYTE).build())) {
            return true;
        }
        return typeSymbol.subtypeOf(semanticModel.types().JSON) && !semanticModel.types().STRING.subtypeOf(typeSymbol);
    }

    private void updateResponseCodeMap(Map<String, Map<String, TypeSymbol>> responseCodeMap, String code,
                                       String mediaType, TypeSymbol typeSymbol) {
        if (responseCodeMap.containsKey(code)) {
            responseCodeMap.get(code).put(mediaType, typeSymbol);
        } else {
            responseCodeMap.put(code, new HashMap<>(Map.of(mediaType, typeSymbol)));
        }
    }

    private void updateApiResponseContent(ApiResponse apiResponse, String mediaType, MediaType mediaTypeObj) {
        Content content = apiResponse.getContent();
        if (Objects.isNull(content)) {
            content = new Content();
        }
        content.addMediaType(mediaType, mediaTypeObj);
        apiResponse.setContent(content);
    }

    private void updateResponseCodeMap(Map<String, Map<String, List<TypeSymbol>>> responsesMap, TypeSymbol typesymbol,
                                       String responseCode, String mediaType) {
        if (responsesMap.containsKey(responseCode)) {
            if (responsesMap.get(responseCode).containsKey(mediaType)) {
                responsesMap.get(responseCode).get(mediaType).add(typesymbol);
            } else {
                List<TypeSymbol> typeSymbols = new ArrayList<>();
                typeSymbols.add(typesymbol);
                responsesMap.get(responseCode).put(mediaType, typeSymbols);
            }
        } else {
            List<TypeSymbol> typeSymbols = new ArrayList<>();
            typeSymbols.add(typesymbol);
            Map<String, List<TypeSymbol>> mediaTypeMap = new HashMap<>();
            mediaTypeMap.put(mediaType, typeSymbols);
            responsesMap.put(responseCode, mediaTypeMap);
        }
    }

    private String getResponseCode(TypeSymbol typeSymbol, String defaultCode) {
        if (isSubTypeOfNil(typeSymbol)) {
            return "202";
        } else if (isSubTypeOfError(typeSymbol)) {
            return HTTP_500;
        }
        for (Map.Entry<String, String> entry : HTTP_CODES.entrySet()) {
            if (isSubTypeOfHttpRecordType(entry.getKey(), typeSymbol)) {
                return entry.getValue();
            }
        }
        return defaultCode;
    }

    private boolean isSubTypeOfError(TypeSymbol typeSymbol) {
        return typeSymbol.subtypeOf(semanticModel.types().ERROR);
    }

    private boolean isSubTypeOfHttpStatusCodeResponse(TypeSymbol typeSymbol) {
        return isSubTypeOfHttpRecordType("StatusCodeResponse", typeSymbol);
    }

    private boolean isSubTypeOfHttpRecordType(String type, TypeSymbol typeSymbol) {
        Optional<Symbol> optionalRecordSymbol = semanticModel.types().getTypeByName("ballerina", "http",
                "", type);
        if (optionalRecordSymbol.isPresent() &&
                optionalRecordSymbol.get() instanceof TypeDefinitionSymbol recordSymbol) {
            return typeSymbol.subtypeOf(recordSymbol.typeDescriptor());
        }
        return false;
    }

    public String getMediaTypeFromType(TypeSymbol typeSymbol, String prefix, List<String> allowedMediaTypes) {
        String mediaType;
        if (typeSymbol.subtypeOf(semanticModel.types().STRING)) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, TEXT_PATTERN, TEXT_PLAIN);
        } else if (typeSymbol.subtypeOf(semanticModel.types().XML)) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, XML_PATTERN, APPLICATION_XML);
        } else if (typeSymbol.subtypeOf(
                semanticModel.types().builder().ARRAY_TYPE.withType(semanticModel.types().BYTE).build())) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, OCTET_STREAM_PATTERN, APPLICATION_OCTET_STREAM);
        } else if (isSubTypeOfHttpResponse(typeSymbol)) {
            return "*/*";
        } else {
            mediaType = getCompatibleMediaType(allowedMediaTypes, JSON_PATTERN, APPLICATION_JSON);
        }
        return addMediaTypePrefix(mediaType, prefix);
    }

    private String addMediaTypePrefix(String mediaType, String prefix) {
        if (prefix.trim().isEmpty() || mediaType.equals("*/*")) {
            return mediaType;
        }
        String[] mediaTypeParts = mediaType.split("/");
        if (mediaTypeParts.length == 2) {
            return mediaTypeParts[0] + "/" + prefix + "+" + mediaTypeParts[1];
        }
        return mediaType;
    }

    private String getCompatibleMediaType(List<String> allowedMediaTypes, String pattern, String defaultMediaType) {
        if (Objects.isNull(allowedMediaTypes) || allowedMediaTypes.isEmpty()) {
            return defaultMediaType;
        }
        if (allowedMediaTypes.size() == 1) {
            return allowedMediaTypes.get(0);
        }
        for (String mediaType : allowedMediaTypes) {
            if (mediaType.matches(pattern)) {
                return mediaType;
            }
        }
        return defaultMediaType;
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

    public void addApiResponse(ApiResponses apiResponses, ApiResponse apiResponse, String statusCode) {
        if (apiResponses.containsKey(statusCode)) {
            updateExistingApiResponse(apiResponses, apiResponse, statusCode);
        } else {
            if (Objects.isNull(apiResponse.getDescription())) {
                apiResponse.setDescription(HTTP_CODE_DESCRIPTIONS.get(statusCode));
            }
            apiResponses.put(statusCode, apiResponse);
        }
    }

    private static void updateExistingApiResponse(ApiResponses apiResponses, ApiResponse apiResponse,
                                                  String statusCode) {
        ApiResponse res = apiResponses.get(statusCode);
        Content content = res.getContent();
        if (content == null) {
            content = new Content();
        }
        if (apiResponse.getContent() != null) {
            updateApiResponseContent(apiResponse, content);
        }
        res.setContent(content);
        apiResponses.put(statusCode, res);
    }

    private static void updateApiResponseContent(ApiResponse apiResponse, Content content) {
        String mediaType = apiResponse.getContent().keySet().iterator().next();
        Schema newSchema = apiResponse.getContent().values().iterator().next().getSchema();
        if (content.containsKey(mediaType)) {
            Schema<?> schema = content.get(mediaType).getSchema();
            if (schema instanceof ComposedSchema && schema.getOneOf() != null) {
                schema.getOneOf().add(newSchema);
                content.put(mediaType, new MediaType().schema(schema));
            } else {
                ComposedSchema composedSchema = new ComposedSchema();
                composedSchema.addOneOfItem(schema);
                composedSchema.addOneOfItem(newSchema);
                MediaType updatedMediaContent =
                        new MediaType().schema(composedSchema);
                content.put(mediaType, updatedMediaContent);
            }
        } else {
            content.put(mediaType, apiResponse.getContent().values().iterator().next());
        }
    }
}
