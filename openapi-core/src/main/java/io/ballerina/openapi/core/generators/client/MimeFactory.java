/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.mime.CustomType;
import io.ballerina.openapi.core.generators.client.mime.JsonType;
import io.ballerina.openapi.core.generators.client.mime.MimeType;
import io.ballerina.openapi.core.generators.client.mime.MultipartFormData;
import io.ballerina.openapi.core.generators.client.mime.OctedStreamType;
import io.ballerina.openapi.core.generators.client.mime.UrlEncodedType;
import io.ballerina.openapi.core.generators.client.mime.XmlType;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.core.GeneratorConstants.IMAGE;
import static io.ballerina.openapi.core.GeneratorConstants.JSON;
import static io.ballerina.openapi.core.GeneratorConstants.PDF;
import static io.ballerina.openapi.core.GeneratorConstants.TEXT_PREFIX;
import static io.ballerina.openapi.core.GeneratorConstants.UNSUPPORTED_MEDIA_ERROR;
import static io.ballerina.openapi.core.GeneratorConstants.VENDOR_SPECIFIC_TYPE;
import static io.ballerina.openapi.core.GeneratorConstants.XML;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

/**
 * Factory class for mime type creation.
 *
 * @since 1.3.0
 */
public class MimeFactory {

    /**
     * Get the relevant mime object.
     *
     * @param mediaTypeEntry         - Media type entry
     * @param ballerinaUtilGenerator - Ballerina util generator
     * @param imports                - Ballerina imports
     * @return mimetype
     * @throws BallerinaOpenApiException throws an exception on unsupported mime types.
     */
    public MimeType getMimeType(Map.Entry<String, MediaType> mediaTypeEntry,
                                BallerinaUtilGenerator ballerinaUtilGenerator, List<ImportDeclarationNode> imports)
            throws BallerinaOpenApiException {

        Schema requestBodySchema = mediaTypeEntry.getValue().getSchema();
        if (requestBodySchema != null && (requestBodySchema.get$ref() != null || requestBodySchema.getType() != null
                || requestBodySchema.getProperties() != null)) {
            String mediaType = mediaTypeEntry.getKey();
            if (mediaType.contains(VENDOR_SPECIFIC_TYPE)) {
                return new CustomType();
            } else if (mediaType.contains(JSON)) {
                return new JsonType();
            } else if (mediaType.startsWith(TEXT_PREFIX) || mediaType.contains(PDF) || mediaType.startsWith(IMAGE)) {
                return new CustomType();
            } else if (mediaType.contains(XML)) {
                return new XmlType(imports);
            } else if (mediaType.equals(APPLICATION_FORM_URLENCODED)) {
                return new UrlEncodedType(ballerinaUtilGenerator);
            } else if (mediaType.equals(APPLICATION_OCTET_STREAM)) {
                return new OctedStreamType();
            } else if (mediaType.equals(MULTIPART_FORM_DATA)) {
                return new MultipartFormData(imports, ballerinaUtilGenerator);
            } else {
                throw new BallerinaOpenApiException(String.format(UNSUPPORTED_MEDIA_ERROR, mediaType));
            }
        } else {
            return new CustomType();
        }
    }
}
