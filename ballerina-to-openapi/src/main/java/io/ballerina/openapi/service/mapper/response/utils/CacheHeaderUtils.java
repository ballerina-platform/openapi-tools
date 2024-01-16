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
package io.ballerina.openapi.service.mapper.response.utils;

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.response.model.CacheConfigAnnotation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static io.ballerina.openapi.service.mapper.Constants.CACHE_CONTROL;
import static io.ballerina.openapi.service.mapper.Constants.ETAG;
import static io.ballerina.openapi.service.mapper.Constants.FALSE;
import static io.ballerina.openapi.service.mapper.Constants.LAST_MODIFIED;
import static io.ballerina.openapi.service.mapper.Constants.MAX_AGE;
import static io.ballerina.openapi.service.mapper.Constants.MUST_REVALIDATE;
import static io.ballerina.openapi.service.mapper.Constants.NO_CACHE;
import static io.ballerina.openapi.service.mapper.Constants.NO_STORE;
import static io.ballerina.openapi.service.mapper.Constants.NO_TRANSFORM;
import static io.ballerina.openapi.service.mapper.Constants.PRIVATE;
import static io.ballerina.openapi.service.mapper.Constants.PROXY_REVALIDATE;
import static io.ballerina.openapi.service.mapper.Constants.PUBLIC;
import static io.ballerina.openapi.service.mapper.Constants.S_MAX_AGE;
import static io.ballerina.openapi.service.mapper.Constants.TRUE;

public final class CacheHeaderUtils {

    private CacheHeaderUtils() {

    }

    public static CacheConfigAnnotation setCacheConfigValues(SeparatedNodeList<MappingFieldNode> fields) {
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

    private static void setCacheConfigField(CacheConfigAnnotation cacheConfig, String fieldName,
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
            String sMaxAge = expressionNode.toString();
            try {
                int maxA = Integer.parseInt(sMaxAge);
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

    public static void updateResponseHeaderWithCacheValues(Map<String, Header> headers, CacheConfigAnnotation cache) {
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

    private static void cacheHeaderForEmptyValues(Map<String, Header> headers, Header cacheHeader,
                                                  StringSchema stringSchema, String value) {
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

    private static String generateCacheControlString(CacheConfigAnnotation cacheConfig) {
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

    private static String appendFields(List<String> fields) {
        return ("=\"" + buildCommaSeparatedString(fields) + "\"");
    }

    private static String buildCommaSeparatedString(List<String> fields) {
        String genString = fields.stream().map(field -> field.replaceAll("\"", "") + ",").
                collect(Collectors.joining());
        return genString.substring(0, genString.length() - 1);
    }
}
