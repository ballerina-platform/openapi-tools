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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.openapi.service.mapper.type.TypeMapperImpl;
import io.ballerina.openapi.service.mapper.utils.MediaTypeUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.mapper.Constants.HTTP_PAYLOAD;
import static io.ballerina.openapi.service.mapper.Constants.MEDIA_TYPE;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.getMediaTypeFromType;
import static io.ballerina.openapi.service.mapper.utils.MediaTypeUtils.isSameMediaType;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.extractAnnotationFieldDetails;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.removeStartingSingleQuote;

public class RequestBodyMapper {
    private List<String> allowedMediaTypes = new ArrayList<>();
    private final RequestBody requestBody = new RequestBody().required(true);
    private final TypeMapper typeMapper;
    private final SemanticModel semanticModel;
    private final OperationInventory operationInventory;
    private final String mediaTypeSubTypePrefix;

    public RequestBodyMapper(ParameterSymbol reqParameter, AnnotationNode annotation,
                             OperationInventory operationInventory, FunctionDefinitionNode resourceNode,
                             Components components, Map<String, String> apiDocs, AdditionalData additionalData) {
        this.typeMapper = new TypeMapperImpl(components, additionalData);
        this.semanticModel = additionalData.semanticModel();
        this.operationInventory = operationInventory;
        this.mediaTypeSubTypePrefix = MediaTypeUtils.extractCustomMediaType(resourceNode).orElse("");
        requestBody.description(apiDocs.get(removeStartingSingleQuote(reqParameter.getName().get())));
        extractAnnotationDetails(annotation);
        createReqBodyMapping(reqParameter.typeDescriptor());
    }

    public void setRequestBody() {
        operationInventory.overrideRequestBody(requestBody);
    }

    private void extractAnnotationDetails(AnnotationNode annotation) {
        if (annotation.annotReference().toString().trim().equals(HTTP_PAYLOAD)) {
            allowedMediaTypes = extractAnnotationFieldDetails(HTTP_PAYLOAD, MEDIA_TYPE, annotation, semanticModel);
        }
    }

    private void createReqBodyMapping(TypeSymbol reqBodyType) {
        UnionTypeSymbol unionType = getUnionType(reqBodyType, semanticModel);
        if (Objects.nonNull(unionType)) {
            addReqBodyMappingForUnion(unionType);
        } else {
            addReqBodyMappingForSimpleType(reqBodyType);
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

    private void addReqBodyMappingForUnion(UnionTypeSymbol unionType) {
        Map<String, TypeSymbol> reqContentMap = getReqContentMap(unionType);
        for (Map.Entry<String, TypeSymbol> entry : reqContentMap.entrySet()) {
            TypeSymbol typeSymbol = entry.getValue();
            String mediaType = entry.getKey();
            addRequestContent(typeSymbol, mediaType);
        }
    }

    private void addRequestContent(TypeSymbol reqBodyType, String mediaType) {
        MediaType mediaTypeObj = new MediaType();
        mediaTypeObj.setSchema(typeMapper.getTypeSchema(reqBodyType));
        updateReqBodyContentWithMediaType(mediaType, mediaTypeObj);
    }

    private void updateReqBodyContentWithMediaType(String mediaType, MediaType mediaTypeObj) {
        Content content = requestBody.getContent();
        if (Objects.isNull(content)) {
            content = new Content();
        }
        content.addMediaType(mediaType, mediaTypeObj);
        requestBody.setContent(content);
    }

    private void addReqBodyMappingForSimpleType(TypeSymbol returnType) {
        String mediaType = getMediaTypeFromType(returnType, mediaTypeSubTypePrefix, allowedMediaTypes, semanticModel);
        addRequestContent(returnType, mediaType);
    }

    public Map<String, TypeSymbol> getReqContentMap(UnionTypeSymbol typeSymbol) {
        Map<String, List<TypeSymbol>> reqContentsList = new HashMap<>();
        extractBasicMembers(typeSymbol, reqContentsList);
        Map<String, TypeSymbol> reqContents = new HashMap<>();
        for (Map.Entry<String, List<TypeSymbol>> entry : reqContentsList.entrySet()) {
            List<TypeSymbol> typeSymbols = entry.getValue();
            String mediaType = entry.getKey();
            if (typeSymbols.size() == 1) {
                if (isSubTypeOfNil(typeSymbols.get(0), semanticModel)) {
                    requestBody.required(null);
                    continue;
                }
                reqContents.put(mediaType, typeSymbols.get(0));
            } else {
                if (typeSymbols.removeIf(typeSymbol1 -> isSubTypeOfNil(typeSymbol1, semanticModel))) {
                    requestBody.required(null);
                }
                TypeSymbol[] typeSymbolArray = typeSymbols.toArray(TypeSymbol[]::new);
                TypeSymbol unionTypeSymbol = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                        typeSymbolArray).build();
                reqContents.put(mediaType, unionTypeSymbol);
            }
        }
        return reqContents;
    }

    private void updateReqContentMap(Map<String, List<TypeSymbol>> reqContents, TypeSymbol typesymbol,
                                     String mediaType) {
        if (reqContents.containsKey(mediaType)) {
            reqContents.get(mediaType).add(typesymbol);
        } else {
            List<TypeSymbol> typeSymbols = new ArrayList<>();
            typeSymbols.add(typesymbol);
            reqContents.put(mediaType, typeSymbols);
        }
    }

    private void extractBasicMembers(UnionTypeSymbol unionTypeSymbol, Map<String, List<TypeSymbol>> reqContents) {
        if (isSameMediaType(unionTypeSymbol, semanticModel)) {
            String mediaType = getMediaTypeFromType(unionTypeSymbol, mediaTypeSubTypePrefix,
                    allowedMediaTypes, semanticModel);
            updateReqContentMap(reqContents, unionTypeSymbol, mediaType);
            return;
        }
        List<TypeSymbol> directMemberTypes = unionTypeSymbol.userSpecifiedMemberTypes();
        for (TypeSymbol directMemberType : directMemberTypes) {
            if (isSameMediaType(directMemberType, semanticModel)) {
                String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix,
                        allowedMediaTypes, semanticModel);
                updateReqContentMap(reqContents, directMemberType, mediaType);
            } else {
                UnionTypeSymbol unionType = getUnionType(directMemberType, semanticModel);
                if (Objects.isNull(unionType)) {
                    String mediaType = getMediaTypeFromType(directMemberType, mediaTypeSubTypePrefix,
                            allowedMediaTypes, semanticModel);
                    updateReqContentMap(reqContents, directMemberType, mediaType);
                    continue;
                }
                extractBasicMembers(unionType, reqContents);
            }
        }
    }

    private static boolean isSubTypeOfNil(TypeSymbol typeSymbol, SemanticModel semanticModel) {
        return typeSymbol.subtypeOf(semanticModel.types().NIL);
    }
}
