/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.service.mapper.response;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.openapi.service.mapper.ServiceMapperFactory;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.response.model.CacheConfigAnnotation;
import io.ballerina.openapi.service.mapper.response.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.response.utils.CacheHeaderUtils;
import io.ballerina.openapi.service.mapper.response.utils.StatusCodeErrorUtils;
import io.ballerina.openapi.service.mapper.response.utils.StatusCodeResponseUtils;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.utils.MediaTypeUtils;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.DEFAULT;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_200;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_201;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_202;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_400;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_500;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_CODE_DESCRIPTIONS;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.service.mapper.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.service.mapper.Constants.POST;
import static io.ballerina.openapi.service.mapper.response.utils.StatusCodeErrorUtils.isSubTypeOfHttpStatusCodeError;
import static io.ballerina.openapi.service.mapper.response.utils.StatusCodeResponseUtils.isSubTypeOfHttpStatusCodeResponse;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.getMediaTypeFromType;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.isSameMediaType;

/**
 * This {@link DefaultResponseMapper} class is the implementation of the {@link ResponseMapper} interface.
 * This class provides functionalities for mapping the Ballerina return type to OpenAPI
 * response.
 *
 * @since 1.9.0
 */
public class DefaultResponseMapper implements ResponseMapper {
    private final TypeMapper typeMapper;
    protected final SemanticModel semanticModel;
    private List<String> allowedMediaTypes = new ArrayList<>();
    private final Map<String, Header> cacheHeaders = new HashMap<>();
    private final Map<String, Map<String, Header>> headersMap = new HashMap<>();
    private final String mediaTypeSubTypePrefix;
    private final ApiResponses apiResponses = new ApiResponses();
    private final OperationInventory operationInventory;
    private final FunctionDefinitionNode resourceNode;

    public DefaultResponseMapper(FunctionDefinitionNode resourceNode, OperationInventory operationInventory,
                                 AdditionalData additionalData, ServiceMapperFactory serviceMapperFactory) {
        this.typeMapper = serviceMapperFactory.getTypeMapper();
        this.mediaTypeSubTypePrefix = MediaTypeUtils.extractCustomMediaType(resourceNode).orElse("");
        this.semanticModel = additionalData.semanticModel();
        this.operationInventory = operationInventory;
        this.resourceNode = resourceNode;
    }

    private void generateApiResponses(FunctionDefinitionNode resourceNode, OperationInventory operationInventory) {
        extractAnnotationDetails(resourceNode);

        String defaultStatusCode = operationInventory.getHttpOperation().equalsIgnoreCase(POST) ? HTTP_201 : HTTP_200;
        TypeSymbol returnTypeSymbol = getReturnTypeSymbol(resourceNode);
        createResponseMapping(returnTypeSymbol, defaultStatusCode);
        if (operationInventory.hasDataBinding()) {
            addResponseMappingForDataBindingFailures();
        }
    }

    public void setApiResponses() {
        generateApiResponses(resourceNode, operationInventory);
        operationInventory.setApiResponses(apiResponses);
    }

    protected TypeSymbol getReturnTypeSymbol(FunctionDefinitionNode resourceNode) {
        Optional<Symbol> symbol = semanticModel.symbol(resourceNode);
        if (symbol.isEmpty() || !(symbol.get() instanceof ResourceMethodSymbol resourceMethodSymbol)) {
            return null;
        }
        Optional<TypeSymbol> returnTypeOpt = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
        return returnTypeOpt.orElse(null);
    }

    private void extractAnnotationDetails(FunctionDefinitionNode resource) {
        FunctionSignatureNode functionSignature = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptor.get();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            if (!annotations.isEmpty()) {
                for (AnnotationNode annotation : annotations) {
                    if (annotation.annotReference().toString().trim().equals("http:Cache")) {
                        CacheConfigAnnotation cacheConfigAnn = new CacheConfigAnnotation();
                        annotation.annotValue().ifPresentOrElse(
                                value -> CacheHeaderUtils.updateResponseHeaderWithCacheValues(cacheHeaders,
                                        CacheHeaderUtils.setCacheConfigValues(value.fields())),
                                () -> CacheHeaderUtils.updateResponseHeaderWithCacheValues(cacheHeaders, 
                                        cacheConfigAnn));
                    } else if (annotation.annotReference().toString().trim().equals(HTTP_PAYLOAD)) {
                        allowedMediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE,
                                annotation, semanticModel);
                    }
                }
            }
        }
    }

    protected void createResponseMapping(TypeSymbol returnType, String defaultStatusCode) {
        if (Objects.isNull(returnType)) {
            return;
        }
        Optional<UnionTypeSymbol> unionTypeOpt = getUnionType(returnType, semanticModel);
        if (unionTypeOpt.isPresent()) {
            addResponseMappingForUnion(defaultStatusCode, unionTypeOpt.get());
        } else {
            addResponseMappingForSimpleType(returnType, defaultStatusCode);
        }
    }

    private static Optional<UnionTypeSymbol> getUnionType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        if (Objects.isNull(typeSymbol)) {
            return Optional.empty();
        }
        return switch (typeSymbol.typeKind()) {
            case UNION -> Optional.of((UnionTypeSymbol) typeSymbol);
            case TYPE_REFERENCE -> isSameMediaType(typeSymbol, semanticModel) ? Optional.empty() :
                    getUnionType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), semanticModel);
            case INTERSECTION ->
                    getUnionType(((IntersectionTypeSymbol) typeSymbol).effectiveTypeDescriptor(), semanticModel);
            default -> Optional.empty();
        };
    }

    private void addResponseMappingForUnion(String defaultStatusCode, UnionTypeSymbol unionType) {
        Map<String, Map<String, TypeSymbol>> responseCodeMap = getResponseCodeMap(unionType, defaultStatusCode);
        for (Map.Entry<String, Map<String, TypeSymbol>> mediaTypeEntry : responseCodeMap.entrySet()) {
            Map<String, TypeSymbol> mediaTypes = mediaTypeEntry.getValue();
            String responseCode = mediaTypeEntry.getKey();
            for (Map.Entry<String, TypeSymbol> entry : mediaTypes.entrySet()) {
                TypeSymbol typeSymbol = entry.getValue();
                if (isSubTypeOfNil(typeSymbol, semanticModel)) {
                    addResponseMappingForNil();
                } else if (isSubTypeOfHttpResponse(typeSymbol, semanticModel)) {
                    addResponseMappingForHttpResponse();
                } else {
                    String mediaType = entry.getKey();
                    ApiResponse apiResponse = new ApiResponse();
                    addResponseContent(typeSymbol, apiResponse, mediaType);
                    addApiResponse(apiResponse, responseCode);
                }
            }
        }
    }

    private void addHeaders(ApiResponse apiResponse, String statusCode) {
        Map<String, Header> headers = new HashMap<>();
        if (!cacheHeaders.isEmpty() && isSuccessStatusCode(statusCode)) {
            headers.putAll(cacheHeaders);
        }
        if (headersMap.containsKey(statusCode)) {
            headers.putAll(headersMap.get(statusCode));
        }
        if (!headers.isEmpty()) {
            apiResponse.setHeaders(headers);
        }
    }

    private void addResponseContent(TypeSymbol returnType, ApiResponse apiResponse, String mediaType) {
        if (!isPlainAnyDataType(returnType)) {
            MediaType mediaTypeObj = new MediaType();
            mediaTypeObj.setSchema(typeMapper.getTypeSchema(returnType));
            updateApiResponseContentWithMediaType(apiResponse, mediaType, mediaTypeObj);
        }
    }

    public void addApiResponse(ApiResponse apiResponse, String statusCode) {
        addHeaders(apiResponse, statusCode);
        if (apiResponses.containsKey(statusCode)) {
            updateExistingApiResponse(apiResponse, statusCode);
        } else {
            if (Objects.isNull(apiResponse.getDescription())) {
                apiResponse.setDescription(HTTP_CODE_DESCRIPTIONS.get(statusCode));
            }
            apiResponses.put(statusCode, apiResponse);
        }
    }

    private void updateExistingApiResponse(ApiResponse apiResponse, String statusCode) {
        ApiResponse existingRes = apiResponses.get(statusCode);
        Content newContent = apiResponse.getContent();
        if (Objects.nonNull(newContent)) {
            Content content = existingRes.getContent();
            if (Objects.isNull(content)) {
                existingRes.setContent(newContent);
            } else {
                updateApiResponseContentWithMediaType(existingRes, newContent.keySet().iterator().next(),
                        newContent.values().iterator().next());
            }
        }
        apiResponses.put(statusCode, existingRes);
    }

    private void updateApiResponseContentWithMediaType(ApiResponse apiResponse, String mediaType,
                                                       MediaType mediaTypeObj) {
        Content content = apiResponse.getContent();
        if (Objects.isNull(content)) {
            content = new Content();
        }
        if (content.containsKey(mediaType) && Objects.nonNull(content.get(mediaType).getSchema())) {
            MediaType existingMediaType = content.get(mediaType);
            if (Objects.nonNull(mediaTypeObj.getSchema()) &&
                    !existingMediaType.getSchema().equals(mediaTypeObj.getSchema())) {
                updateContentWithSameMediaType(mediaTypeObj, existingMediaType);
            }
        } else {
            content.addMediaType(mediaType, mediaTypeObj);
        }
        apiResponse.setContent(content);
    }

    private static void updateContentWithSameMediaType(MediaType mediaTypeObj, MediaType existingMediaType) {
        if (existingMediaType.getSchema() instanceof ComposedSchema existingSchema) {
            List<Schema> oneOf = existingSchema.getOneOf();
            if (mediaTypeObj.getSchema() instanceof ComposedSchema mediaTypeSchema) {
                List<Schema> mediaTypeOneOf = mediaTypeSchema.getOneOf();
                for (Schema schema : mediaTypeOneOf) {
                    if (!oneOf.contains(schema)) {
                        oneOf.add(schema);
                    }
                }
                existingSchema.setOneOf(oneOf);
            } else if (!oneOf.contains(mediaTypeObj.getSchema())) {
                oneOf.add(mediaTypeObj.getSchema());
                existingSchema.setOneOf(oneOf);
            }
        } else {
            if (mediaTypeObj.getSchema() instanceof ComposedSchema mediaTypeSchema) {
                if (!mediaTypeSchema.getOneOf().contains(existingMediaType.getSchema())) {
                    mediaTypeSchema.getOneOf().add(existingMediaType.getSchema());
                }
                existingMediaType.setSchema(mediaTypeSchema);
            } else {
                Schema updatedSchema = new ComposedSchema().oneOf(List.of(existingMediaType.getSchema(),
                        mediaTypeObj.getSchema()));
                existingMediaType.setSchema(updatedSchema);
            }
        }
    }

    private void addResponseMappingForNil() {
        ApiResponse apiResponse = getApiResponse(HTTP_202);
        addApiResponse(apiResponse, HTTP_202);
    }

    private void addResponseMappingForDataBindingFailures() {
        ApiResponse apiResponse = getApiResponse(HTTP_400);
        TypeSymbol errorType = semanticModel.types().ERROR;
        addResponseContent(errorType, apiResponse, "application/json");
        addApiResponse(apiResponse, HTTP_400);
    }

    private void addResponseMappingForHttpResponse() {
        MediaType mediaTypeObj = new MediaType();
        mediaTypeObj.setSchema(new Schema().description("Any type of entity body"));
        ApiResponse apiResponse = getApiResponse(DEFAULT);
        apiResponse.setDescription("Any Response");
        apiResponse.setContent(new Content().addMediaType("*/*", mediaTypeObj));
        addApiResponse(apiResponse, DEFAULT);
    }

    private ApiResponse getApiResponse(String statusCode) {
        if (apiResponses.containsKey(statusCode)) {
            return apiResponses.get(statusCode);
        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription(HTTP_CODE_DESCRIPTIONS.get(statusCode));
        return apiResponse;
    }

    private void addResponseMappingForSimpleType(TypeSymbol returnType, String defaultStatusCode) {
        if (isSubTypeOfNil(returnType, semanticModel)) {
            addResponseMappingForNil();
        } else if (isSubTypeOfHttpResponse(returnType, semanticModel)) {
            addResponseMappingForHttpResponse();
        } else if (isSubTypeOfHttpStatusCodeResponse(returnType, semanticModel)) {
            ResponseInfo responseInfo = StatusCodeResponseUtils.extractResponseInfo(returnType, defaultStatusCode,
                    typeMapper, semanticModel);
            updateApiResponseWithResponseInfo(responseInfo);
        } else if (isSubTypeOfHttpStatusCodeError(returnType, semanticModel)) {
            ResponseInfo responseInfo = StatusCodeErrorUtils.extractResponseInfo(returnType, typeMapper, semanticModel);
            updateApiResponseWithResponseInfo(responseInfo);
        } else {
            ApiResponse apiResponse = new ApiResponse();
            String mediaType = getMediaTypeFromType(returnType, mediaTypeSubTypePrefix, allowedMediaTypes,
                    semanticModel);
            addResponseContent(returnType, apiResponse, mediaType);
            String statusCode = getResponseCode(returnType, defaultStatusCode, semanticModel);
            apiResponse.description(HTTP_CODE_DESCRIPTIONS.get(statusCode));
            addApiResponse(apiResponse, statusCode);
        }
    }

    private void updateApiResponseWithResponseInfo(ResponseInfo responseInfo) {
        updateHeaderMap(responseInfo.statusCode(), responseInfo.headers());
        createResponseMapping(responseInfo.bodyType(), responseInfo.statusCode());
    }

    public Map<String, Map<String, TypeSymbol>> getResponseCodeMap(UnionTypeSymbol typeSymbol, String defaultCode) {
        Map<String, Map<String, List<TypeSymbol>>> responsesCodeMap = new HashMap<>();
        extractBasicMembers(typeSymbol, defaultCode, responsesCodeMap);
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

    private void updateResponseCodeMap(Map<String, Map<String, List<TypeSymbol>>> responsesMap, TypeSymbol typesymbol,
                                       String responseCode, String mediaType) {
        if (responsesMap.containsKey(responseCode)) {
            if (responsesMap.get(responseCode).containsKey(mediaType)) {
                if (!responsesMap.get(responseCode).get(mediaType).contains(typesymbol)) {
                    responsesMap.get(responseCode).get(mediaType).add(typesymbol);
                }
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

    private void updateResponseCodeMap(Map<String, Map<String, TypeSymbol>> responseCodeMap, String code,
                                       String mediaType, TypeSymbol typeSymbol) {
        if (responseCodeMap.containsKey(code)) {
            responseCodeMap.get(code).put(mediaType, typeSymbol);
        } else {
            responseCodeMap.put(code, new HashMap<>(Map.of(mediaType, typeSymbol)));
        }
    }

    private void extractBasicMembers(UnionTypeSymbol unionTypeSymbol, String defaultCode,
                                     Map<String, Map<String, List<TypeSymbol>>> responses) {
        if (isSameMediaType(unionTypeSymbol, semanticModel)) {
            String mediaType = getMediaTypeFromType(unionTypeSymbol, mediaTypeSubTypePrefix, allowedMediaTypes,
                    semanticModel);
            String code = getResponseCode(unionTypeSymbol, defaultCode, semanticModel);
            updateResponseCodeMap(responses, unionTypeSymbol, code, mediaType);
            return;
        }
        List<TypeSymbol> directMemberTypes = unionTypeSymbol.userSpecifiedMemberTypes();
        for (TypeSymbol directMemberType : directMemberTypes) {
            String code = getResponseCode(directMemberType, defaultCode, semanticModel);
            ResponseInfo responseInfo = null;
            if (isSubTypeOfHttpStatusCodeResponse(directMemberType, semanticModel)) {
                responseInfo = StatusCodeResponseUtils.extractResponseInfo(directMemberType, code,
                        typeMapper, semanticModel);
            } else if (isSubTypeOfHttpStatusCodeError(directMemberType, semanticModel)) {
                responseInfo = StatusCodeErrorUtils.extractResponseInfo(directMemberType, typeMapper, semanticModel);
            }
            if (Objects.nonNull(responseInfo)) {
                code = responseInfo.statusCode();
                if (!responseInfo.headers().isEmpty()) {
                    updateHeaderMap(code, responseInfo.headers());
                }
                directMemberType = responseInfo.bodyType();
            }
            if (isSameMediaType(directMemberType, semanticModel)) {
                String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix, allowedMediaTypes,
                        semanticModel);
                updateResponseCodeMap(responses, directMemberType, code, mediaType);
            } else {
                Optional<UnionTypeSymbol> unionTypeOpt = getUnionType(directMemberType, semanticModel);
                if (unionTypeOpt.isEmpty()) {
                    String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix,
                            allowedMediaTypes, semanticModel);
                    updateResponseCodeMap(responses, directMemberType, code, mediaType);
                    continue;
                }
                extractBasicMembers(unionTypeOpt.get(), code, responses);
            }
        }
    }

    private void updateHeaderMap(String code, Map<String, Header> headers) {
        if (headersMap.containsKey(code)) {
            headersMap.get(code).putAll(headers);
        } else {
            headersMap.put(code, headers);
        }
    }

    private static String getResponseCode(TypeSymbol typeSymbol, String defaultCode, SemanticModel semanticModel) {
        if (isSubTypeOfNil(typeSymbol, semanticModel)) {
            return HTTP_202;
        } else if (isSubTypeOfError(typeSymbol, semanticModel)) {
            return HTTP_500;
        }
        return defaultCode;
    }

    private static boolean isSuccessStatusCode(String statusCode) {
        return statusCode.startsWith("2");
    }

    private static boolean isSubTypeOfNil(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return typeSymbol.subtypeOf(semanticModel.types().NIL);
    }

    private static boolean isPlainAnyDataType(TypeSymbol typeSymbol) {
        return typeSymbol.typeKind().equals(TypeDescKind.ANYDATA);
    }

    private static boolean isSubTypeOfError(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return typeSymbol.subtypeOf(semanticModel.types().ERROR);
    }
    
    public static boolean isSubTypeOfHttpResponse(TypeSymbol returnType, SemanticModel semanticModel) {
        Optional<Symbol> optionalRecordSymbol = semanticModel.types().getTypeByName("ballerina", "http",
                "", "Response");
        if (optionalRecordSymbol.isPresent() &&
                optionalRecordSymbol.get() instanceof ClassSymbol classSymbol) {
            return returnType.subtypeOf(classSymbol);
        }
        return false;
    }
}
