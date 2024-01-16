/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationBuilder;
import io.ballerina.openapi.service.mapper.response.model.CacheConfigAnnotation;
import io.ballerina.openapi.service.mapper.response.utils.CacheHeaderUtils;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperInterface;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.ballerina.openapi.service.mapper.utils.MediaTypeUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.ACCEPTED;
import static io.ballerina.openapi.service.mapper.Constants.BAD_REQUEST;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_200;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_201;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_202;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_400;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_500;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_CODES;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_CODE_DESCRIPTIONS;
import static io.ballerina.openapi.service.mapper.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.service.mapper.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.service.mapper.Constants.POST;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.getMediaTypeFromType;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.isSameMediaType;

public class ResponseMapper implements ResponseMapperInterface {
    private final TypeMapperInterface typeMapper;
    private final SemanticModel semanticModel;
    private List<String> allowedMediaTypes = new ArrayList<>();
    private final Map<String, Header> cacheHeaders = new HashMap<>();
    private final Map<String, Map<String, Header>> headersMap = new HashMap<>();
    private final String mediaTypeSubTypePrefix;
    private final ApiResponses apiResponses = new ApiResponses();
    private final OperationBuilder operationBuilder;

    public ResponseMapper(FunctionDefinitionNode resourceNode, OperationBuilder operationBuilder, Components components,
                          AdditionalData additionalData) {
        this.typeMapper = new TypeMapper(components, additionalData);
        this.mediaTypeSubTypePrefix = MediaTypeUtils.extractCustomMediaType(resourceNode).orElse("");
        this.semanticModel = additionalData.semanticModel();
        this.operationBuilder = operationBuilder;
        extractAnnotationDetails(resourceNode);

        String defaultStatusCode = operationBuilder.getHttpOperation().equalsIgnoreCase(POST) ? HTTP_201 : HTTP_200;
        TypeSymbol returnTypeSymbol = getReturnTypeSymbol(resourceNode);
        createResponseMapping(returnTypeSymbol, defaultStatusCode);
        if (operationBuilder.hasDataBinding()) {
            addResponseMappingForDataBindingFailures();
        }
    }

    public void setApiResponses() {
        operationBuilder.setApiResponses(apiResponses);
    }

    private TypeSymbol getReturnTypeSymbol(FunctionDefinitionNode resourceNode) {
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

    private void createResponseMapping(TypeSymbol returnType, String defaultStatusCode) {
        UnionTypeSymbol unionType = getUnionType(returnType, semanticModel);
        if (Objects.nonNull(unionType)) {
            addResponseMappingForUnion(defaultStatusCode, unionType);
        } else {
            addResponseMappingForSimpleType(returnType, defaultStatusCode);
        }
    }

    private static UnionTypeSymbol getUnionType(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        if (Objects.isNull(typeSymbol)) {
            return null;
        }
        return switch (typeSymbol.typeKind()) {
            case UNION -> (UnionTypeSymbol) typeSymbol;
            case TYPE_REFERENCE -> isSameMediaType(typeSymbol, semanticModel) ? null :
                    getUnionType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor(), semanticModel);
            case INTERSECTION ->
                    getUnionType(((IntersectionTypeSymbol) typeSymbol).effectiveTypeDescriptor(), semanticModel);
            default -> null;
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
        ApiResponse res = apiResponses.get(statusCode);
        if (apiResponse.getContent() != null) {
            Content content = res.getContent();
            if (content == null) {
                content = new Content();
            }

            updateApiResponseContentWithMediaType(apiResponse, content);
            res.setContent(content);
        }
        apiResponses.put(statusCode, res);
    }

    private static void updateApiResponseContentWithMediaType(ApiResponse apiResponse, Content content) {
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

    private void updateApiResponseContentWithMediaType(ApiResponse apiResponse, String mediaType,
                                                       MediaType mediaTypeObj) {
        Content content = apiResponse.getContent();
        if (Objects.isNull(content)) {
            content = new Content();
        }
        content.addMediaType(mediaType, mediaTypeObj);
        apiResponse.setContent(content);
    }

    private void addResponseMappingForNil() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description(ACCEPTED);
        addApiResponse(apiResponse, HTTP_202);
    }

    private void addResponseMappingForDataBindingFailures() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.description(BAD_REQUEST);
        TypeSymbol errorType = semanticModel.types().ERROR;
        addResponseContent(errorType, apiResponse, "application/json");
        addApiResponse(apiResponse, HTTP_400);
    }

    private void addResponseMappingForHttpResponse() {
        MediaType mediaTypeObj = new MediaType();
        mediaTypeObj.setSchema(new Schema().description("Any type of entity body"));
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setDescription("Any Response");
        apiResponse.setContent(new Content().addMediaType("*/*", mediaTypeObj));
        addApiResponse(apiResponse, "default");
    }

    private void addResponseMappingForSimpleType(TypeSymbol returnType, String defaultStatusCode) {
        if (isSubTypeOfNil(returnType, semanticModel)) {
            addResponseMappingForNil();
        } else if (isSubTypeOfHttpResponse(returnType, semanticModel)) {
            addResponseMappingForHttpResponse();
        } else if (isSubTypeOfHttpStatusCodeResponse(returnType, semanticModel)) {
            TypeSymbol bodyType = getBodyTypeFromStatusCodeResponse(returnType, semanticModel);
            Map<String, Header> headersFromStatusCodeResponse = getHeadersFromStatusCodeResponse(returnType);
            String statusCode = getResponseCode(returnType, defaultStatusCode, semanticModel);
            headersMap.put(statusCode, headersFromStatusCodeResponse);
            createResponseMapping(bodyType, getResponseCode(returnType, defaultStatusCode, semanticModel));
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
            if (isHttpStatusCodeResponseType(semanticModel, directMemberType)) {
                Map<String, Header> headersFromStatusCodeResponse = getHeadersFromStatusCodeResponse(directMemberType);
                if (!headersFromStatusCodeResponse.isEmpty()) {
                    headersMap.put(code, headersFromStatusCodeResponse);
                }
                directMemberType = getBodyTypeFromStatusCodeResponse(directMemberType, semanticModel);
            }
            if (isSameMediaType(directMemberType, semanticModel)) {
                String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix, allowedMediaTypes,
                        semanticModel);
                updateResponseCodeMap(responses, directMemberType, code, mediaType);
            } else {
                UnionTypeSymbol unionType = getUnionType(directMemberType, semanticModel);
                if (Objects.isNull(unionType)) {
                    String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix,
                            allowedMediaTypes, semanticModel);
                    updateResponseCodeMap(responses, directMemberType, code, mediaType);
                    continue;
                }
                extractBasicMembers(unionType, code, responses);
            }
        }
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

    public TypeSymbol getBodyTypeFromStatusCodeResponse(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        RecordTypeSymbol statusCodeRecordType = getStatusCodeRecordTypeSymbol(typeSymbol);
        if (Objects.nonNull(statusCodeRecordType) && statusCodeRecordType.fieldDescriptors().containsKey("body")) {
            return statusCodeRecordType.fieldDescriptors().get("body").typeDescriptor();
        }
        return semanticModel.types().ANYDATA;
    }

    public Map<String, Header> getHeadersFromStatusCodeResponse(TypeSymbol typeSymbol) {
        RecordTypeSymbol statusCodeRecordType = getStatusCodeRecordTypeSymbol(typeSymbol);
        if (Objects.isNull(statusCodeRecordType)) {
            return new HashMap<>();
        }

        HeadersInfo headersInfo = getHeadersInfo(statusCodeRecordType);
        if (Objects.isNull(headersInfo)) {
            return new HashMap<>();
        }

        Map<String, RecordFieldSymbol> recordFieldMap = new HashMap<>(headersInfo.headerRecordType().
                fieldDescriptors());
        Map<String, Schema> recordFieldsMapping = typeMapper.mapRecordFields(recordFieldMap, new HashSet<>(),
                headersInfo.recordName(), false);
        return mapRecordFieldToHeaders(recordFieldsMapping);
    }

    private HeadersInfo getHeadersInfo(RecordTypeSymbol statusCodeRecordType) {
        if (statusCodeRecordType.fieldDescriptors().containsKey("headers")) {
            TypeSymbol headersType = typeMapper.getReferredType(
                    statusCodeRecordType.fieldDescriptors().get("headers").typeDescriptor());
            if (Objects.nonNull(headersType) && headersType instanceof TypeReferenceTypeSymbol headersRefType &&
                    headersRefType.typeDescriptor() instanceof RecordTypeSymbol recordType) {
                return new HeadersInfo(recordType, MapperCommonUtils.getTypeName(headersType));
            } else if (Objects.nonNull(headersType) && headersType instanceof RecordTypeSymbol recordType) {
                return new HeadersInfo(recordType, MapperCommonUtils.getTypeName(recordType));
            }
        }
        return null;
    }

    private record HeadersInfo(RecordTypeSymbol headerRecordType, String recordName) {
    }

    private RecordTypeSymbol getStatusCodeRecordTypeSymbol(TypeSymbol typeSymbol) {
        TypeSymbol statusCodeResType = typeMapper.getReferredType(typeSymbol);
        RecordTypeSymbol statusCodeRecordType = null;
        if (statusCodeResType instanceof TypeReferenceTypeSymbol statusCodeResRefType &&
                statusCodeResRefType.typeDescriptor() instanceof RecordTypeSymbol recordTypeSymbol) {
            statusCodeRecordType = recordTypeSymbol;
        } else if (statusCodeResType instanceof RecordTypeSymbol recordTypeSymbol) {
            statusCodeRecordType = recordTypeSymbol;
        }
        return statusCodeRecordType;
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

    private static String getResponseCode(TypeSymbol typeSymbol, String defaultCode, SemanticModel semanticModel) {
        if (isSubTypeOfNil(typeSymbol, semanticModel)) {
            return "202";
        } else if (isSubTypeOfError(typeSymbol, semanticModel)) {
            return HTTP_500;
        }
        for (Map.Entry<String, String> entry : HTTP_CODES.entrySet()) {
            if (isSubTypeOfHttpRecordType(entry.getKey(), typeSymbol, semanticModel)) {
                return entry.getValue();
            }
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
    
    private static boolean isSubTypeOfHttpStatusCodeResponse(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return isSubTypeOfHttpRecordType("StatusCodeResponse", typeSymbol, semanticModel);
    }

    private static boolean isSubTypeOfHttpRecordType(String type, TypeSymbol typeSymbol, SemanticModel semanticModel) {
        Optional<Symbol> optionalRecordSymbol = semanticModel.types().getTypeByName("ballerina", "http",
                "", type);
        if (optionalRecordSymbol.isPresent() &&
                optionalRecordSymbol.get() instanceof TypeDefinitionSymbol recordSymbol) {
            return typeSymbol.subtypeOf(recordSymbol.typeDescriptor());
        }
        return false;
    }
}
