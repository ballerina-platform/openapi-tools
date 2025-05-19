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

package io.ballerina.openapi.core.generators.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for openapi code generator.
 *
 * @since 1.3.0
 */
public class GeneratorConstants {

    public static final String X_BALLERINA_NAME = "x-ballerina-name";
    public static final String X_PARAM_TYPE = "x-param-type";
    public static final String HTTP_VERSION_FIELD = "httpVersion";
    public static final String HTTP_1_SETTINGS_FIELD = "http1Settings";
    public static final String HTTP_2_SETTINGS_FIELD = "http2Settings";
    public static final String TIMEOUT_FIELD = "timeout";
    public static final String FORWARDED_FIELD = "forwarded";
    public static final String FOLLOW_REDIRECTS_FIELD = "followRedirects";
    public static final String POOL_CONFIG_FIELD = "poolConfig";
    public static final String CACHE_FIELD = "cache";
    public static final String COMPRESSION_FIELD = "compression";
    public static final String CIRCUIT_BREAKER_FIELD = "circuitBreaker";
    public static final String RETRY_CONFIG_FIELD = "retryConfig";
    public static final String COOKIE_CONFIG_FIELD = "cookieConfig";
    public static final String RESPONSE_LIMITS_FIELD = "responseLimits";
    public static final String SECURE_SOCKET_FIELD = "secureSocket";
    public static final String PROXY_FIELD = "proxy";
    public static final String SOCKET_CONFIG_FIELD = "socketConfig";
    public static final String VALIDATION_FIELD = "validation";
    public static final String LAX_DATA_BINDING_FIELD = "laxDataBinding";
    public static final List<String> CLIENT_CONFIG_FIELD_LIST = List.of(HTTP_VERSION_FIELD, HTTP_1_SETTINGS_FIELD,
            HTTP_2_SETTINGS_FIELD, TIMEOUT_FIELD, FORWARDED_FIELD, FOLLOW_REDIRECTS_FIELD, POOL_CONFIG_FIELD,
            CACHE_FIELD, COMPRESSION_FIELD, CIRCUIT_BREAKER_FIELD, RETRY_CONFIG_FIELD, COOKIE_CONFIG_FIELD,
            RESPONSE_LIMITS_FIELD, SECURE_SOCKET_FIELD, PROXY_FIELD, SOCKET_CONFIG_FIELD, VALIDATION_FIELD,
            LAX_DATA_BINDING_FIELD);
    public static final String HTTP_CLIENT_CONFIGURATION = "http:ClientConfiguration";
    public static final String HTTP_CLIENT_HTTP_1_SETTINGS = "http:ClientHttp1Settings";
    public static final String HTTP_CLIENT_HTTP_2_SETTINGS = "http:ClientHttp2Settings";
    public static final String DEFAULT_RECORD = "{}";
    public static final String DEFAULT_TIMEOUT = "30";
    public static final String HTTP_CACHE_CONFIG = "http:CacheConfig";
    public static final String HTTP_COMPRESSION = "http:Compression";
    public static final String HTTP_CIRCUIT_BREAKER_CONFIG = "http:CircuitBreakerConfig";
    public static final String HTTP_RETRY_CONFIG = "http:RetryConfig";
    public static final String HTTP_COOKIE_CONFIG = "http:CookieConfig";
    public static final String HTTP_RESPONSE_LIMIT_CONFIGS = "http:ResponseLimitConfigs";
    public static final String HTTP_CLIENT_SECURE_SOCKET = "http:ClientSecureSocket";
    public static final String HTTP_PROXY_CONFIG = "http:ProxyConfig";
    public static final String HTTP_CLIENT_SOCKET_CONFIG = "http:ClientSocketConfig";

    /**
     * Enum to select the code generation mode.
     * Ballerina service, mock and client generation is available
     */
    public enum GenType {
        GEN_SERVICE("gen_service"),
        GEN_CLIENT("gen_client"),
        GEN_BOTH("gen_both");

        private String name;

        GenType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Enum to select the relevant ballerina http auth record.
     */
    public enum AuthConfigTypes {
        BASIC("http:CredentialsConfig"),
        BEARER("http:BearerTokenConfig"),
        CLIENT_CREDENTIAL("http:OAuth2ClientCredentialsGrantConfig"),
        CUSTOM_CLIENT_CREDENTIAL("OAuth2ClientCredentialsGrantConfig"),
        REFRESH_TOKEN("http:OAuth2RefreshTokenGrantConfig"),
        CUSTOM_REFRESH_TOKEN("OAuth2RefreshTokenGrantConfig"),
        PASSWORD("http:OAuth2PasswordGrantConfig"),
        CUSTOM_PASSWORD("OAuth2PasswordGrantConfig");

        private final String authType;

        AuthConfigTypes(String authType) {
            this.authType = authType;
        }

        public String getValue() {
            return authType;
        }
    }

    public static final String TYPE_FILE_NAME = "types.bal";
    public static final String CLIENT_FILE_NAME = "client.bal";
    public static final String UTIL_FILE_NAME = "utils.bal";
    public static final String TEST_FILE_NAME = "test.bal";
    public static final String SERVICE_FILE_NAME = "service.bal";
    public static final String CONFIG_FILE_NAME = "Config.toml";
    public static final String BALLERINA_TOML = "Ballerina.toml";
    public static final String BALLERINA_TOML_CONTENT = "[package]\n" +
            "org= \"ballerina\"\n" +
            "name= \"testopenapi\"\n" +
            "version= \"0.0.0\"\n";

    public static final String OAS_PATH_SEPARATOR = "/";
    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~'`*\\-=^+();:\\/{}\\s|.$])";
    public static final String ESCAPE_PATTERN_FOR_MODIFIER = "([\\[\\]\\\\?!<>@#&~'`*\\_\\-=^+();:\\/{}\\s|.$])";
    public static final String REGEX_WITHOUT_SPECIAL_CHARACTERS = "\\b[_a-zA-Z][_a-zA-Z0-9]*\\b";
    public static final String REGEX_WORDS_STARTING_WITH_NUMBERS = "^[0-9].*";
    public static final String REGEX_ONLY_NUMBERS_OR_NUMBERS_WITH_SPECIAL_CHARACTERS = "\\b[0-9([\\[\\]\\\\?!<>@#&~'`" +
            "*\\-=^+();:\\/{}\\s|.$])]*\\b";
    //ClientCode generator
    public static final String HTTP = "http";
    public static final String J_BALLERINA = "jballerina.java";
    public static final String URL = "url";
    public static final String MODULE_TEST = "test";
    public static final String BALLERINA = "ballerina";
    public static final String CLIENT = "Client";
    public static final String STATUS_CODE_CLIENT = "StatusCodeClient";
    public static final String CLIENT_EP = "clientEp";
    public static final String EQUAL = "=";
    public static final String CONFIG = "config";
    public static final String FUNCTION = "function";
    public static final String PREFIX_TEST = " test";
    public static final String ANNOT_TEST = "test:Config";
    public static final String TEST_DIR = "tests";
    public static final String STRING = "string";
    public static final String XML = "xml";
    public static final String BYTE = "byte";
    public static final String JSON = "json";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String IDENTIFIER = "identifier";
    public static final String TYPE_NAME = "typeName";
    public static final String BINARY = "binary";
    public static final String DOUBLE = "double";
    public static final String FLOAT = "float";
    public static final String HEADER = "header";
    public static final String HEADER_VALUES = "headerValues";
    public static final String PAYLOAD = "payload";
    public static final String PAYLOAD_KEYWORD = "Payload";
    public static final String REQUEST = "request";
    public static final String HTTP_REQUEST = "http:Request";
    public static final String PDF = "pdf";
    public static final String QUERY_PARAM = "queryParam";
    public static final String QUERIES = "queries";
    public static final String SELF = "self";
    public static final String TEXT_PREFIX = "text/";
    public static final String XML_DATA = "data.xmldata";
    public static final String JSON_DATA = "data.jsondata";
    public static final String IMAGE = "image";
    public static final String VENDOR_SPECIFIC_TYPE = "vnd.";
    public static final String MIME = "mime";
    public static final String HTTP_HEADERS = "httpHeaders";
    public static final String RESOURCE_PATH = "resourcePath";
    public static final String ARRAY = "array";
    public static final String ERROR = "error";
    public static final String MAP_JSON = "map<json>";
    public static final String MEDIA_TYPE_KEYWORD = "mediaType";

    public static final String ANY_TYPE = "*/*";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String APPLICATION_URL_ENCODE = "application/x-www-form-urlencoded";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String APPLICATION_FORM_URLENCODED = "application//x-www-form-urlencoded";
    public static final String TEXT_EVENT_STREAM = "text/event-stream";

    public static final String TEXT = "text";
    public static final String IMAGE_PNG = "image/png";
    public static final String ENSURE_TYPE = "ensureType";

    // auth related constants
    public static final String API_KEY = "apikey";
    public static final String API_KEYS_CONFIG = "ApiKeysConfig";
    public static final String API_KEY_CONFIG_PARAM = "apiKeyConfig";
    public static final String AUTH = "auth";
    public static final String AUTH_CONFIG = "authConfig";
    public static final String HTTP_CLIENT_CONFIG = "httpClientConfig";
    public static final String CLIENT_HTTP1_SETTINGS = "ClientHttp1Settings";
    public static final String CLIENT_HTTP1_SETTINGS_FIELD = "http1Settings";
    public static final String HTTP2_SETTINGS = "http:ClientHttp2Settings";
    public static final String HTTP2_SETTINGS_FIELD = "http2Settings";
    public static final String SETTINGS = "settings";
    public static final String CACHE_CONFIG = "http:CacheConfig";
    public static final String CACHE_CONFIG_FIELD = "cache";
    public static final String RESPONSE_LIMIT = "http:ResponseLimitConfigs";
    public static final String RESPONSE_LIMIT_FIELD = "responseLimits";
    public static final String SECURE_SOCKET = "http:ClientSecureSocket";
    public static final String PROXY = "http:ProxyConfig";

    public static final String KEEP_ALIVE = "keepAlive";
    public static final String CHUNKING = "chunking";
    public static final String BASIC = "basic";
    public static final String BEARER = "bearer";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CLIENT_CRED = "client_cred";
    public static final String PASSWORD = "password";
    public static final String CONNECTION_CONFIG = "ConnectionConfig";
    public static final String OAUTH2 = "oauth2";
    public static final String SSL_FIELD_NAME = "secureSocket";
    public static final String PROXY_CONFIG = "proxy";
    public static final String VALIDATION = "validation";

    public static final String DEFAULT_API_KEY_DESC = "API keys for authorization";

    public static final String RESPONSE = "response";
    public static final String TYPE = "type";
    public static final String ANY_DATA = "anydata";

    //Http Methods
    public static final String POST = "post";
    public static final String GET = "get";
    public static final String PUT = "put";
    public static final String DELETE = "delete";
    public static final String PATCH = "patch";
    public static final String EXECUTE = "execute";
    public static final String HEAD = "head";
    public static final String OPTIONS = "options";
    //Encoding related constants
    public static final String DEEP_OBJECT = "DEEPOBJECT";
    public static final String FORM = "FORM";
    public static final String SPACE_DELIMITED = "SPACEDELIMITED";
    public static final String PIPE_DELIMITED = "PIPEDELIMITED";
    public static final String ENCODING = "Encoding";
    public static final String ENCODING_STYLE = "EncodingStyle";
    public static final String STYLE = "style";
    public static final String EXPLODE = "explode";

    //OpenAPI Ballerina extensions
    public static final String X_BALLERINA_INIT_DESCRIPTION = "x-ballerina-init-description";
    public static final String X_BALLERINA_DISPLAY = "x-ballerina-display";
    public static final String X_BALLERINA_DEPRECATED_REASON = "x-ballerina-deprecated-reason";
    public static final String X_BALLERINA_HTTP_CONFIGURATIONS = "x-ballerina-http-configurations";

    //Service related
    public static final String HOST = "host";
    public static final String NEW = "new";
    public static final String RESOURCE = "resource";
    public static final String QUERY = "query";
    public static final String HTTP_RESPONSE = "http:Response";
    public static final String DEFAULT = "default";
    public static final String DEFAULT_STATUS = "Default";
    public static final String DEFAULT_STATUS_CODE_RESPONSE = "DefaultStatusCodeResponse";

    /**
     * Util for select http keywords with http codes.
     *
     * @param code http code.
     * @return Http identification word.
     */
    public static final Map<String, String> HTTP_CODES_DES;

    static {
        Map<String, String> httpCodeMap = new HashMap<>();
        httpCodeMap.put("100", "Continue");
        httpCodeMap.put("101", "SwitchingProtocols");
        httpCodeMap.put("102", "Processing");
        httpCodeMap.put("103", "EarlyHints");
        httpCodeMap.put("200", "Ok");
        httpCodeMap.put("201", "Created");
        httpCodeMap.put("202", "Accepted");
        httpCodeMap.put("203", "NonAuthoritativeInformation");
        httpCodeMap.put("204", "NoContent");
        httpCodeMap.put("205", "ResetContent");
        httpCodeMap.put("206", "PartialContent");
        httpCodeMap.put("207", "MultiStatus");
        httpCodeMap.put("208", "AlreadyReported");
        httpCodeMap.put("226", "IMUsed");
        httpCodeMap.put("300", "MultipleChoices");
        httpCodeMap.put("301", "MovedPermanently");
        httpCodeMap.put("302", "Found");
        httpCodeMap.put("303", "SeeOther");
        httpCodeMap.put("304", "NotModified");
        httpCodeMap.put("305", "UseProxy");
        httpCodeMap.put("307", "TemporaryRedirect");
        httpCodeMap.put("308", "PermanentRedirect");
        httpCodeMap.put("400", "BadRequest");
        httpCodeMap.put("401", "Unauthorized");
        httpCodeMap.put("402", "PaymentRequired");
        httpCodeMap.put("403", "Forbidden");
        httpCodeMap.put("404", "NotFound");
        httpCodeMap.put("405", "MethodNotAllowed");
        httpCodeMap.put("406", "NotAcceptable");
        httpCodeMap.put("407", "ProxyAuthenticationRequired");
        httpCodeMap.put("408", "RequestTimeout");
        httpCodeMap.put("409", "Conflict");
        httpCodeMap.put("410", "Gone");
        httpCodeMap.put("411", "LengthRequired");
        httpCodeMap.put("412", "PreconditionFailed");
        httpCodeMap.put("413", "PayloadTooLarge");
        httpCodeMap.put("414", "UriTooLong");
        httpCodeMap.put("415", "UnsupportedMediaType");
        httpCodeMap.put("416", "RangeNotSatisfiable");
        httpCodeMap.put("417", "ExpectationFailed");
        httpCodeMap.put("421", "MisdirectedRequest");
        httpCodeMap.put("422", "UnprocessableEntity");
        httpCodeMap.put("423", "Locked");
        httpCodeMap.put("424", "FailedDependency");
        httpCodeMap.put("425", "TooEarly");
        httpCodeMap.put("426", "UpgradeRequired");
        httpCodeMap.put("428", "PreconditionRequired");
        httpCodeMap.put("429", "TooManyRequests");
        httpCodeMap.put("431", "RequestHeaderFieldsTooLarge");
        httpCodeMap.put("451", "UnavailableDueToLegalReasons");
        httpCodeMap.put("500", "InternalServerError");
        httpCodeMap.put("501", "NotImplemented");
        httpCodeMap.put("502", "BadGateway");
        httpCodeMap.put("503", "ServiceUnavailable");
        httpCodeMap.put("504", "GatewayTimeout");
        httpCodeMap.put("505", "HttpVersionNotSupported");
        httpCodeMap.put("506", "VariantAlsoNegotiates");
        httpCodeMap.put("507", "InsufficientStorage");
        httpCodeMap.put("508", "LoopDetected");
        httpCodeMap.put("510", "NotExtended");
        httpCodeMap.put("511", "NetworkAuthenticationRequired");
        httpCodeMap.put(DEFAULT, DEFAULT_STATUS_CODE_RESPONSE);
        HTTP_CODES_DES = Collections.unmodifiableMap(httpCodeMap);
    }

    public static final String HTTP_200 = "200";
    public static final String HTTP_201 = "201";
    public static final String INTEGER = "integer";
    public static final String BOOLEAN = "boolean";
    public static final String NUMBER = "number";
    public static final String OBJECT = "object";
    public static final Integer MAX_ARRAY_LENGTH = 2147483637;
    public static final String NILLABLE = "?";
    public static final String SQUARE_BRACKETS = "[]";

    public static final Map<String, List<String>> OPENAPI_TYPE_TO_FORMAT_MAP;
    public static final Map<String, String> OPENAPI_TYPE_TO_BAL_TYPE_MAP;

    static {
        // Add values to `OPENAPI_TYPE_TO_FORMAT_MAP`
        Map<String, List<String>> typeToFormatMap = new HashMap<>();
        typeToFormatMap.put("integer", List.of("int32", "int64"));
        typeToFormatMap.put("number", List.of("float", "double"));
        typeToFormatMap.put("string", List.of("date", "date-time", "password", "byte", "binary"));
        OPENAPI_TYPE_TO_FORMAT_MAP = Collections.unmodifiableMap(typeToFormatMap);

        // Add values to `OPENAPI_TYPE_TO_BAL_TYPE_MAP`,
        // ***Note: If any new format is added, Please check the constraint support also.***
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("integer", "int");
        typeMap.put("string", "string");
        typeMap.put("boolean", "boolean");
        typeMap.put("array", "[]");
        typeMap.put("object", "record {}");
        typeMap.put("decimal", "decimal");
        typeMap.put("number", "decimal");
        typeMap.put("double", "decimal");
        typeMap.put("float", "float");
        typeMap.put("binary", "byte[]");
        typeMap.put("byte", "string");
        typeMap.put("int32", "int:Signed32");
        typeMap.put("int64", "int");
        OPENAPI_TYPE_TO_BAL_TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

    public static final String DEFAULT_HTTP_VERSION = "2.0";
    public static final Map<String, String> HTTP_VERSION_MAP;

    static {
        Map<String, String> httpVersionMap = new HashMap<>();
        httpVersionMap.put("1.0", "http:HTTP_1_0");
        httpVersionMap.put("1.1", "http:HTTP_1_1");
        httpVersionMap.put("2.0", "http:HTTP_2_0");
        HTTP_VERSION_MAP = Collections.unmodifiableMap(httpVersionMap);
    }

    //Error related
    public static final String UNSUPPORTED_MEDIA_ERROR = "Unsupported media type '%s' is given in the request body";

    // OS specific line separator
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String DOUBLE_LINE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
    public static final String DEFAULT_RETURN = HTTP_RESPONSE + "|" + ERROR;

    // Service Generation
    public static final String TREAT_NILABLE_AS_OPTIONAL = "treatNilableAsOptional";
    public static final String SERVICE_CONFIG = "http:ServiceConfig";
    public static final String FALSE = "false";
    public static final String HEADER_ANNOT = "Header";
    public static final String MAP_STRING = "map<string>";
    public static final String TEXT_WILDCARD_REGEX = "text/.*";
    public static final String BODY = "body";
    public static final String HEADERS = "headers";
    public static final String SPECIAL_CHARACTERS_REGEX = "[^a-zA-Z0-9]";
    public static final String OPEN_CURLY_BRACE = "{";
    public static final String CLOSE_CURLY_BRACE = "}";
    public static final String SLASH = "/";
    public static final String CATCH_ALL_PATH = "/*";

    public static final String CONSTRAINT = "constraint";
    public static final String CONSTRAINT_STRING = "constraint:String";
    public static final String CONSTRAINT_NUMBER = "constraint:Number";
    public static final String CONSTRAINT_FLOAT = "constraint:Float";
    public static final String CONSTRAINT_INT = "constraint:Int";
    public static final String CONSTRAINT_ARRAY = "constraint:Array";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String MAX_LENGTH = "maxLength";
    public static final String MIN_LENGTH = "minLength";
    public static final String MINIMUM = "minValue";
    public static final String MAXIMUM = "maxValue";
    public static final String EXCLUSIVE_MAX = "maxValueExclusive";
    public static final String EXCLUSIVE_MIN = "minValueExclusive";
    public static final String SPECIAL_CHARACTER_REGEX = "([\\[\\]\\\\?!<>@#&~`*\\-=^+'();:\\/\\_{}\\s|.$])";
    public static final String HTTP_VERSION = "http:HttpVersion";
    public static final String YAML_EXTENSION = ".yaml";
    public static final String YML_EXTENSION = ".yml";
    public static final String JSON_EXTENSION = ".json";
    public static final String UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE = "attribute swaggerVersion is unexpected";
    public static final String PIPE = "|";
    public static final String RETURNS = "returns";
    public static final String ANYDATA = "anydata";
    public static final String RESPONSE_RECORD_NAME = "Response";
    public static final String REQUEST_RECORD_NAME = "Request";
    public static final String HTTP_VERIONS_EXT = "httpVersion";
    public static final String SERVICE_TYPE_NAME = "OASServiceType";
    public static final String DEFAULT_RETURN_COMMENT = "return value description";
    public static final String DEFAULT_FUNC_COMMENT = "Description";
    public static final String DEFAULT_PARAM_COMMENT = "parameter description";
    public static final String EMPTY_RECORD = "record{}";
    public static final String DO_NOT_MODIFY_FILE_HEADER = "// AUTO-GENERATED FILE. DO NOT MODIFY.\n" +
            "// This file is auto-generated by the Ballerina OpenAPI tool.\n\n";
    public static final String DEFAULT_FILE_HEADER = "// AUTO-GENERATED FILE.\n" +
            "// This file is auto-generated by the Ballerina OpenAPI tool.\n\n";
    public static final String HTTP_CALLER = "http:Caller";
    public static final String CALLER = "caller";
    public static final String NULL = "null";
    public static final String INT = "int";
    public static final String INT_SIGNED32 = "int:Signed32";
    public static final String DECIMAL = "decimal";
    public static final String RETURN = "return";
    public static final String OPTIONAL_ERROR = "error?";
    public static final String PATH = "path";
    public static final String NAME_ANNOTATION = "jsondata:Name";
    public static final String QUERY_ANNOTATION = "http:Query";
    public static final String HEADER_ANNOTATION = "http:Header";
}
