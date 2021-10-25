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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.client.mime.DefaultType;
import io.ballerina.openapi.generators.client.mime.JsonType;
import io.ballerina.openapi.generators.client.mime.MimeType;
import io.ballerina.openapi.generators.client.mime.OctedStreamType;
import io.ballerina.openapi.generators.client.mime.UrlEncodedType;
import io.ballerina.openapi.generators.client.mime.XmlType;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;


import static io.ballerina.openapi.generators.GeneratorConstants.JSON;
import static io.ballerina.openapi.generators.GeneratorConstants.PDF;
import static io.ballerina.openapi.generators.GeneratorConstants.TEXT_PREFIX;
import static io.ballerina.openapi.generators.GeneratorConstants.UNSUPPORTED_MEDIA_ERROR;
import static io.ballerina.openapi.generators.GeneratorConstants.XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;


/**
 *  Factory class for mime type creation.
 */
public class MimeFactory {

    /**
     * Get the relevant mime object.
     *
     * @param mediaTypeEntry            - Media type entry
     * @param ballerinaUtilGenerator    - Ballerina util generator
     * @param imports                   - Ballerina imports
     * @return mimetype
     * @throws BallerinaOpenApiException throws an exception on unsupported mime types.
     */
    public MimeType getMimeType(Map.Entry<String, MediaType> mediaTypeEntry,
                                BallerinaUtilGenerator ballerinaUtilGenerator, List<ImportDeclarationNode> imports)
            throws BallerinaOpenApiException {
        Schema requestBodySchema = mediaTypeEntry.getValue().getSchema();
        if (requestBodySchema.get$ref() != null || requestBodySchema.getType() != null
                || requestBodySchema.getProperties() != null) {
            String mediaType = mediaTypeEntry.getKey();
            if (mediaType.contains(JSON)) {
                return new JsonType();
            } else if (mediaType.startsWith(TEXT_PREFIX) || mediaType.contains(PDF)) {
                return new DefaultType();
            } else if (mediaType.contains(XML)) {
                return new XmlType(imports);
            } else if (mediaType.equals(APPLICATION_FORM_URLENCODED)) {
                return new UrlEncodedType(ballerinaUtilGenerator);
            } else if (mediaType.equals(APPLICATION_OCTET_STREAM)) {
                return new OctedStreamType();
            } else {
                throw new BallerinaOpenApiException(String.format(UNSUPPORTED_MEDIA_ERROR, mediaType));
            }
        } else {
            return new DefaultType();
        }
    }
}
