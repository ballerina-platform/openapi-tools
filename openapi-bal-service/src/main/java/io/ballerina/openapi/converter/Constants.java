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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ballerina To OpenApi Service Constants.
 */
public class Constants {
    public static final String ATTR_HOST = "host";
    public static final String INT = "int";
    public static final String INTEGER = "integer";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL = "decimal";
    public static final String ARRAY = "array";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String TYPE_REFERENCE = "type_reference";
    public static final String PATH = "path";
    public static final String BODY = "body";
    public static final String HTTP_PAYLOAD = "http:Payload";
    public static final String HTTP_QUERY = "http:Query";
    public static final String HTTP = "http";
    public static final String BALLERINA = "ballerina";
    public static final String TYPEREFERENCE = "typeReference";
    public static final String HTTP_HEADER = "http:Header";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String BYTE = "byte";
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
    public static final String HTML_POSTFIX = "+plain";
    public static final String OCTECT_STREAM_POSTFIX = "+octet-stream";
    public static final String X_WWW_FORM_URLENCODED_POSTFIX = "+x-www-form-urlencoded";
    public static final String X_WWW_FORM_URLENCODED = "x-www-form-urlencoded";

    public static final String TEXT_PREFIX = "text/";
    public static final String MAP_JSON = "map<json>";
    public static final String MAP_STRING = "map<string>";
    public static final String MAP = "map";
    public static final String HTTP_REQUEST = "http:Request";
    public static final String DEFAULT = "default";
    public static final String HTTP_RESPONSE = "http:Response";
    public static final String RESPONSE_HEADERS = "headers";
    public static final String WILD_CARD_CONTENT_KEY = "*/*";
    public static final String WILD_CARD_SUMMARY = "Any type of entity body";
    public static final String MEDIA_TYPE = "mediaType";
    public static final String TUPLE = "tuple";
    public static final String REGEX_INTERPOLATION_PATTERN = "^(?!.*\\$\\{).+$";
    public static final String DATE_CONSTRAINT_ANNOTATION = "constraint:Date";

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


    public static final Map<String, String> HTTP_CODES;
    static {
        Map<String, String> httpCodeMap = new HashMap<>();
        httpCodeMap.put("Continue", "100");
        httpCodeMap.put("SwitchingProtocols", "101");
        httpCodeMap.put("Processing", "102");
        httpCodeMap.put("EarlyHints", "103");
        httpCodeMap.put("Ok", "200");
        httpCodeMap.put("Created", "201");
        httpCodeMap.put("Accepted", "202");
        httpCodeMap.put("NonAuthoritativeInformation", "203");
        httpCodeMap.put("NoContent", "204");
        httpCodeMap.put("RestContent", "205");
        httpCodeMap.put("PartialContent", "206");
        httpCodeMap.put("MultiStatus", "207");
        httpCodeMap.put("AlreadyReported", "208");
        httpCodeMap.put("IMUsed", "226");
        httpCodeMap.put("MultipleChoices", "300");
        httpCodeMap.put("MovedPermanently", "301");
        httpCodeMap.put("Found", "302");
        httpCodeMap.put("SeeOther", "303");
        httpCodeMap.put("NotModified", "304");
        httpCodeMap.put("UseProxy", "305");
        httpCodeMap.put("TemporaryRedirect", "307");
        httpCodeMap.put("PermanentRedirect", "308");
        httpCodeMap.put("BadRequest", "400");
        httpCodeMap.put("Unauthorized", "401");
        httpCodeMap.put("PaymentRequired", "402");
        httpCodeMap.put("Forbidden", "403");
        httpCodeMap.put("NotFound", "404");
        httpCodeMap.put("MethodNotAllowed", "405");
        httpCodeMap.put("NotAcceptable", "406");
        httpCodeMap.put("ProxyAuthenticationRequired", "407");
        httpCodeMap.put("RequestTimeOut", "408");
        httpCodeMap.put("Conflict", "409");
        httpCodeMap.put("Gone", "410");
        httpCodeMap.put("LengthRequired", "411");
        httpCodeMap.put("PreconditionFailed", "412");
        httpCodeMap.put("PayloadTooLarge", "413");
        httpCodeMap.put("UriTooLong", "414");
        httpCodeMap.put("UnsupportedMediaType", "415");
        httpCodeMap.put("RangeNotSatisfiable", "416");
        httpCodeMap.put("ExpectationFailed", "417");
        httpCodeMap.put("MisdirectedRequest", "421");
        httpCodeMap.put("UnprocessableEntity", "422");
        httpCodeMap.put("Locked", "423");
        httpCodeMap.put("FailedDependency", "424");
        httpCodeMap.put("TooEarly", "425");
        httpCodeMap.put("UpgradeRequired", "426");
        httpCodeMap.put("PreconditionRequired", "428");
        httpCodeMap.put("TooManyRequests", "429");
        httpCodeMap.put("RequestHeaderFieldsTooLarge", "431");
        httpCodeMap.put("UnavailableDueToLegalReasons", "451");
        httpCodeMap.put("InternalServerError", "500");
        httpCodeMap.put("NotImplemented", "501");
        httpCodeMap.put("BadGateway", "502");
        httpCodeMap.put("ServiceUnavailable", "503");
        httpCodeMap.put("GatewayTimeout", "504");
        httpCodeMap.put("HttpVersionNotSupported", "505");
        httpCodeMap.put("VariantAlsoNegotiates", "506");
        httpCodeMap.put("InsufficientStorage", "507");
        httpCodeMap.put("LoopDetected", "508");
        httpCodeMap.put("NotExtended", "510");
        httpCodeMap.put("NetworkAuthenticationRequired", "511");
        httpCodeMap.put("NetworkAuthorizationRequired", "511"); //This status code was added since it is deprecated.
        // TODO: remove this after fixing https://github.com/ballerina-platform/ballerina-standard-library/issues/4245
        HTTP_CODES = Collections.unmodifiableMap(httpCodeMap);
    }
    public static final String HTTP_200 = "200";
    public static final String HTTP_201 = "201";
    public static final String HTTP_500 = "500";
    public static final String HTTP_200_DESCRIPTION = "Ok";
    public static final String HTTP_201_DESCRIPTION = "Created";
    public static final String HTTP_500_DESCRIPTION = "Internal server error";
    public static final String HTTP_204 = "204";
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
    public static final String PLUS = "+";
    public static final String UNDERSCORE = "_";
    public static final String HTTP_202 = "202";
    public static final String HTTP_400 = "400";
    public static final String ACCEPTED = "Accepted";
    public static final String BAD_REQUEST = "BadRequest";
}
