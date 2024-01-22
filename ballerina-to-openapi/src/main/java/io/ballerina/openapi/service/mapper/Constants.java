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
package io.ballerina.openapi.service.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ballerina To OpenApi Service Constants.
 */
public final class Constants {
    public static final String ATTR_HOST = "host";
    public static final String INT = "int";
    public static final String NUMBER = "number";
    public static final String STRING = "string";
    public static final String INT64 = "int64";
    public static final String INT32 = "int32";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String HTTP_PAYLOAD = "http:Payload";
    public static final String HTTP_QUERY = "http:Query";
    public static final String HTTP = "http";
    public static final String BALLERINA = "ballerina";
    public static final String HTTP_HEADER = "http:Header";
    public static final String BYTE = "byte";
    public static final String XML = "xml";
    public static final String JSON = "json";
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
    public static final String HTTP_REQUEST = "http:Request";
    public static final String DEFAULT = "default";
    public static final String WILD_CARD_CONTENT_KEY = "*/*";
    public static final String WILD_CARD_SUMMARY = "Any type of entity body";
    public static final String MEDIA_TYPE = "mediaType";
    public static final String REGEX_INTERPOLATION_PATTERN = "^(?!.*\\$\\{).+$";
    public static final String DATE_CONSTRAINT_ANNOTATION = "constraint:Date";

    private Constants() {

    }

    /**
     * Enum to select the Ballerina Type.
     * Ballerina mapper, mock and client generation is available
     */
    public enum BallerinaType {
        INT("int"),
        STRING("string"),
        DECIMAL("decimal"),
        BOOLEAN("boolean"),
        RECORD("record"),
        ARRAY("array");

        private final String name;

        BallerinaType(String name) {
            this.name = name;
        }

        public String typeName() {
            return name;
        }

    }

    /**
     * Enum to select the Ballerina Type.
     * Ballerina mapper, mock and client generation is available
     */
    public enum OpenAPIType {
        INTEGER("integer"),
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        RECORD("object"),
        ARRAY("array");

        private final String name;

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
    public static final Map<String, String> HTTP_CODE_DESCRIPTIONS;
    static {
        Map<String, String> httpCodeDescriptionMap = new HashMap<>();
        httpCodeDescriptionMap.put("100", "Continue");
        httpCodeDescriptionMap.put("101", "SwitchingProtocols");
        httpCodeDescriptionMap.put("102", "Processing");
        httpCodeDescriptionMap.put("103", "EarlyHints");
        httpCodeDescriptionMap.put("200", "Ok");
        httpCodeDescriptionMap.put("201", "Created");
        httpCodeDescriptionMap.put("202", "Accepted");
        httpCodeDescriptionMap.put("203", "NonAuthoritativeInformation");
        httpCodeDescriptionMap.put("204", "NoContent");
        httpCodeDescriptionMap.put("205", "RestContent");
        httpCodeDescriptionMap.put("206", "PartialContent");
        httpCodeDescriptionMap.put("207", "MultiStatus");
        httpCodeDescriptionMap.put("208", "AlreadyReported");
        httpCodeDescriptionMap.put("226", "IMUsed");
        httpCodeDescriptionMap.put("300", "MultipleChoices");
        httpCodeDescriptionMap.put("301", "MovedPermanently");
        httpCodeDescriptionMap.put("302", "Found");
        httpCodeDescriptionMap.put("303", "SeeOther");
        httpCodeDescriptionMap.put("304", "NotModified");
        httpCodeDescriptionMap.put("305", "UseProxy");
        httpCodeDescriptionMap.put("307", "TemporaryRedirect");
        httpCodeDescriptionMap.put("308", "PermanentRedirect");
        httpCodeDescriptionMap.put("400", "BadRequest");
        httpCodeDescriptionMap.put("401", "Unauthorized");
        httpCodeDescriptionMap.put("402", "PaymentRequired");
        httpCodeDescriptionMap.put("403", "Forbidden");
        httpCodeDescriptionMap.put("404", "NotFound");
        httpCodeDescriptionMap.put("405", "MethodNotAllowed");
        httpCodeDescriptionMap.put("406", "NotAcceptable");
        httpCodeDescriptionMap.put("407", "ProxyAuthenticationRequired");
        httpCodeDescriptionMap.put("408", "RequestTimeOut");
        httpCodeDescriptionMap.put("409", "Conflict");
        httpCodeDescriptionMap.put("410", "Gone");
        httpCodeDescriptionMap.put("411", "LengthRequired");
        httpCodeDescriptionMap.put("412", "PreconditionFailed");
        httpCodeDescriptionMap.put("413", "PayloadTooLarge");
        httpCodeDescriptionMap.put("414", "UriTooLong");
        httpCodeDescriptionMap.put("415", "UnsupportedMediaType");
        httpCodeDescriptionMap.put("416", "RangeNotSatisfiable");
        httpCodeDescriptionMap.put("417", "ExpectationFailed");
        httpCodeDescriptionMap.put("421", "MisdirectedRequest");
        httpCodeDescriptionMap.put("422", "UnprocessableEntity");
        httpCodeDescriptionMap.put("423", "Locked");
        httpCodeDescriptionMap.put("424", "FailedDependency");
        httpCodeDescriptionMap.put("425", "TooEarly");
        httpCodeDescriptionMap.put("426", "UpgradeRequired");
        httpCodeDescriptionMap.put("428", "PreconditionRequired");
        httpCodeDescriptionMap.put("429", "TooManyRequests");
        httpCodeDescriptionMap.put("431", "RequestHeaderFieldsTooLarge");
        httpCodeDescriptionMap.put("451", "UnavailableDueToLegalReasons");
        httpCodeDescriptionMap.put("500", "InternalServerError");
        httpCodeDescriptionMap.put("501", "NotImplemented");
        httpCodeDescriptionMap.put("502", "BadGateway");
        httpCodeDescriptionMap.put("503", "ServiceUnavailable");
        httpCodeDescriptionMap.put("504", "GatewayTimeout");
        httpCodeDescriptionMap.put("505", "HttpVersionNotSupported");
        httpCodeDescriptionMap.put("506", "VariantAlsoNegotiates");
        httpCodeDescriptionMap.put("507", "InsufficientStorage");
        httpCodeDescriptionMap.put("508", "LoopDetected");
        httpCodeDescriptionMap.put("510", "NotExtended");
        httpCodeDescriptionMap.put("511", "NetworkAuthenticationRequired");
        httpCodeDescriptionMap.put("default", "Default response");
        HTTP_CODE_DESCRIPTIONS = Collections.unmodifiableMap(httpCodeDescriptionMap);
    }
    public static final String HTTP_200 = "200";
    public static final String HTTP_201 = "201";
    public static final String HTTP_202 = "202";
    public static final String ACCEPTED = "Accepted";
    public static final String HTTP_400 = "400";
    public static final String BAD_REQUEST = "BadRequest";
    public static final String HTTP_500 = "500";
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
    public static final String UNDERSCORE = "_";
}
