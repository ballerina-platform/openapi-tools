/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ballerina To OpenApi Service Constants.
 */
public class Constants {
    public static final String ATTR_HOST = "host";
    public static final String ATTR_DEF_HOST = "localhost";
    public static final String INT = "int";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL = "decimal";
    public static final String ARRAY = "array";
    public static final String RECORD = "record";
    public static final String OBJECT = "object";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String TYPE_REFERENCE = "type_reference";
    public static final String PATH = "path";
    public static final String QUERY = "query";
    public static final String BODY = "body";
    public static final String HEADER = "header";
    public static final String COOKIE = "cookie";
    public static final String FORM = "form";
    public static final String HTTP_PAYLOAD = "http:Payload";
    public static final String HTTP = "http";
    public static final String PAYLOAD = "payload";
    public static final String TYPEREFERENCE = "typeReference";
    public static final String HTTP_HEADER = "http:Header";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String OCTET_STREAM = "octet-stream";
    public static final String XML = "xml";
    public static final String JSON = "json";
    public static final String PLAIN = "plain";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PATCH = "PATCH";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";
    public static final String OPTIONS = "OPTIONS";
    public static final String HEAD = "HEAD";
    public static final String OPENAPI_SUFFIX = "_openapi";
    public static final String SERVER = "server";
    public static final String PORT = "port";
    public static final String APPLICATION_PREFIX = "application/";
    public static final String JSON_POSTFIX = "+json";
    public static final String XML_POSTFIX = "+xml";
    public static final String TEXT_POSTFIX = "+plain";
    public static final String OCTECT_STREAM_POSTFIX = "+octet-stream";
    public static final String TEXT_PREFIX = "text/";
    public static final String MAP_JSON = "map<json>";
    public static final String HTTP_REQUEST = "http:Request";
    public static final String DEFAULT = "default";
    public static final String HTTP_RESPONSE = "http:Response";

    /**
     * Enum to select the Ballerina Type.
     * Ballerina service, mock and client generation is available
     */
    public enum BallerinaType {
        INT("int"),
        STRING("string"),
        DECIMAL("decimal"),
        BOOLEAN("boolean"),
        RECORD("record"),
        ARRAY("array");

        private String name;

        BallerinaType(String name) {
            this.name = name;
        }

        public String typeName() {
            return name;
        }

    }

    /**
     * Enum to select the Ballerina Type.
     * Ballerina service, mock and client generation is available
     */
    public enum OpenAPIType {
        INTEGER("integer"),
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        RECORD("object"),
        ARRAY("array");

        private String name;

        OpenAPIType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Http response status code.
     */
    public static final List<String> STATUS_CODE_RESPONSE = Collections.unmodifiableList(
            Arrays.asList(
            "Continue", "SwitchingProtocols", "Ok", "Created", "Accepted", "NonAuthoritativeInformation", "NoContent",
    "ResetContent", "PartialContent", "MultipleChoices", "MovedPermanently", "Found", "SeeOther", "NotModified",
                    "UseProxy", "TemporaryRedirect", "PermanentRedirect", "BadRequest", "Unauthorized",
                    "PaymentRequired", "Forbidden", "NotFound", "MethodNotAllowed", "NotAcceptable",
                    "ProxyAuthenticationRequired", "RequestTimeout", "Conflict", "Gone", "LengthRequired",
                    "PreconditionFailed", "PayloadTooLarge", "UriTooLong", "UnsupportedMediaType",
                    "RangeNotSatisfiable", "ExpectationFailed", "UpgradeRequired", "RequestHeaderFieldsTooLarge",
                    "InternalServerError", "NotImplemented", "BadGateway", "ServiceUnavailable", "GatewayTimeout",
                    "HttpVersionNotSupported"));

    public static final Map<String, String> HTTP_CODES;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("Continue", "100");
        aMap.put("SwitchingProtocols", "101");
        aMap.put("Ok", "200");
        aMap.put("Created", "201");
        aMap.put("Accepted", "202");
        aMap.put("NonAuthoritativeInformation", "203");
        aMap.put("NoContent", "204");
        aMap.put("RestContent", "205");
        aMap.put("PartialContent", "206");
        aMap.put("MultipleChoices", "300");
        aMap.put("MovedPermanently", "301");
        aMap.put("Found", "302");
        aMap.put("SeeOther", "303");
        aMap.put("NotModified", "304");
        aMap.put("UseProxy", "305");
        aMap.put("TemporaryRedirect", "308");
        aMap.put("BadRequest", "400");
        aMap.put("Unauthorized", "401");
        aMap.put("PaymentRequired", "402");
        aMap.put("Forbidden", "403");
        aMap.put("NotFound", "404");
        aMap.put("MethodNotAllowed", "405");
        aMap.put("NotAcceptable", "406");
        aMap.put("ProxyAuthenticationRequires", "407");
        aMap.put("RequestTimeOut", "408");
        aMap.put("Conflict", "409");
        aMap.put("Gone", "410");
        aMap.put("LengthRequired", "411");
        aMap.put("PreconditionFailed", "412");
        aMap.put("UriTooLong", "413");
        aMap.put("UnsupportedMediaType", "414");
        aMap.put("RangeNotSatisfied", "415");
        aMap.put("ExpectationFailed", "416");
        aMap.put("UpgradeRequired", "426");
        aMap.put("RequestHeaderFieldsTooLarge", "431");
        aMap.put("InternalServerError", "500");
        aMap.put("NotImplemented", "501");
        aMap.put("BadGateway", "502");
        aMap.put("ServiceUnavailable", "503");
        aMap.put("GatewayTimeOut", "504");
        aMap.put("HttpVersionNotSupported", "505");
        HTTP_CODES = Collections.unmodifiableMap(aMap);
    }
    public static final String HTTP_200 = "200";
    public static final String HTTP_200_DESCRIPTION = "Ok";
    public static final String SPECIAL_CHAR_REGEX = "([\\[\\]\\\\?!<>@#&~`*\\-=^+();:\\/\\_{}\\s|.$])";

    //Cache config constant
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String ETAG = "ETag";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String MUST_REVALIDATE = "must-revalidate";
    public static final String NO_CACHE = "no-cache";
    public static final String NO_STORE = "no-store";
    public static final String NO_TRANSFORM = "no-transform";
    public static final String PRIVATE = "private";
    public static final String PUBLIC = "public";
    public static final String PROXY_REVALIDATE = "proxy-revalidate";
    public static final String MAX_AGE = "max-age";
    public static final String S_MAX_AGE = "s-maxage";

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String SLASH = "/";
    public static final String HYPHEN = "-";
    public static final String CONTRACT = "contract";
    public static final String VERSION = "'version";
    public static final String TITLE = "title";
    public static final String OPENAPI_ANNOTATION = "openapi:ServiceInfo";

    //File extensions
    public static final String YAML_EXTENSION = ".yaml";
    public static final String JSON_EXTENSION = ".json";
    public static final String YML_EXTENSION = ".yml";

    public static final String BALLERINA = "ballerina";
}
