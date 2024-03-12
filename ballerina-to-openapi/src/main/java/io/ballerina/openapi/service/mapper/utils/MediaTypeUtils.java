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
package io.ballerina.openapi.service.mapper.utils;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.response.DefaultResponseMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * This {@link MediaTypeUtils} represents the util functions for populating media type.
 *
 * @since 1.9.0
 */
public class MediaTypeUtils {

    private static final String JSON_PATTERN = "^(application|text)\\/(.*[.+-]|)json$";
    private static final String XML_PATTERN = "^(application|text)\\/(.*[.+-]|)xml$";
    private static final String TEXT_PATTERN = "^(text)\\/(.*[.+-]|)plain$";
    private static final String OCTET_STREAM_PATTERN = "^(application)\\/(.*[.+-]|)octet-stream$";

    private final SemanticModel semanticModel;
    private final TypeSymbol structuredType;
    private final TypeSymbol byteArrayType;
    private final TypeSymbol basicTypesWithoutString;


    private static MediaTypeUtils mediaTypeUtils = null;

    private MediaTypeUtils(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        TypeSymbol mapOfAnydata = semanticModel.types().builder().MAP_TYPE.withTypeParam(
                semanticModel.types().ANYDATA).build();
        TypeSymbol tableOfAnydataMap = semanticModel.types().builder().TABLE_TYPE.withRowType(mapOfAnydata).build();
        TypeSymbol tupleOfAnydata = semanticModel.types().builder().TUPLE_TYPE.withRestType(
                semanticModel.types().ANYDATA).build();
        TypeSymbol structuredUnion = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                mapOfAnydata, tableOfAnydataMap, tupleOfAnydata).build();
        TypeSymbol structuredArray = semanticModel.types().builder().ARRAY_TYPE.withType(structuredUnion).build();
        structuredType = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                mapOfAnydata, tableOfAnydataMap, tupleOfAnydata, structuredArray).build();
        byteArrayType = semanticModel.types().builder().ARRAY_TYPE.withType(semanticModel.types().BYTE).build();
        basicTypesWithoutString = semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                semanticModel.types().BOOLEAN, semanticModel.types().INT, semanticModel.types().FLOAT,
                semanticModel.types().DECIMAL).build();
    }

    public static synchronized MediaTypeUtils getInstance(SemanticModel semanticModel) {
        if (Objects.isNull(mediaTypeUtils) || !mediaTypeUtils.semanticModel.equals(semanticModel)) {
            mediaTypeUtils = new MediaTypeUtils(semanticModel);
        }
        return mediaTypeUtils;
    }

    public String getMediaTypeFromType(TypeSymbol typeSymbol, String prefix, List<String> allowedMediaTypes) {
        String mediaType;
        if (typeSymbol.subtypeOf(semanticModel.types().STRING)) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, TEXT_PATTERN, TEXT_PLAIN);
        } else if (typeSymbol.subtypeOf(semanticModel.types().XML)) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, XML_PATTERN, APPLICATION_XML);
        } else if (typeSymbol.subtypeOf(byteArrayType)) {
            mediaType = getCompatibleMediaType(allowedMediaTypes, OCTET_STREAM_PATTERN, APPLICATION_OCTET_STREAM);
        } else if (DefaultResponseMapper.isSubTypeOfHttpResponse(typeSymbol, semanticModel)) {
            return "*/*";
        } else {
            mediaType = getCompatibleMediaType(allowedMediaTypes, JSON_PATTERN, APPLICATION_JSON);
        }
        return addMediaTypePrefix(mediaType, prefix);
    }

    private static String addMediaTypePrefix(String mediaType, String prefix) {
        if (prefix.trim().isEmpty() || mediaType.equals("*/*")) {
            return mediaType;
        }
        String[] mediaTypeParts = mediaType.split("/");
        if (mediaTypeParts.length == 2) {
            return mediaTypeParts[0] + "/" + prefix + "+" + mediaTypeParts[1];
        }
        return mediaType;
    }

    private static String getCompatibleMediaType(List<String> allowedMediaTypes, String pattern,
                                                 String defaultMediaType) {
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

    public boolean isSameMediaType(TypeSymbol typeSymbol) {
        if (typeSymbol.subtypeOf(semanticModel.types().STRING) || typeSymbol.subtypeOf(semanticModel.types().XML) ||
                typeSymbol.subtypeOf(byteArrayType) || typeSymbol.subtypeOf(basicTypesWithoutString)) {
            return true;
        }
        return isStructuredType(typeSymbol);
    }

    private boolean isStructuredType(TypeSymbol typeSymbol) {
        return typeSymbol.subtypeOf(structuredType);
    }

    public static Optional<String> extractCustomMediaType(FunctionDefinitionNode functionDefNode) {
        ServiceDeclarationNode serviceDefNode = (ServiceDeclarationNode) functionDefNode.parent();
        if (serviceDefNode.metadata().isPresent()) {
            MetadataNode metadataNode = serviceDefNode.metadata().get();
            NodeList<AnnotationNode> annotations = metadataNode.annotations();
            if (!annotations.isEmpty()) {
                return MapperCommonUtils.extractServiceAnnotationDetails(annotations,
                        "http:ServiceConfig", "mediaTypeSubtypePrefix");
            }
        }
        return Optional.empty();
    }
}
