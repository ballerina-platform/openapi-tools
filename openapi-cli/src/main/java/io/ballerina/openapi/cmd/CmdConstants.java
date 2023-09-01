/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.cmd;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for openapi commands.
 *
 * @since 1.2.0
 */
public class CmdConstants {
    public static final String RESOURCE = "resource";
    public static final String REMOTE = "remote";
    public static final String SERVICE = "service";
    public static final String CLIENT = "client";

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

    public static final String DEFAULT_CLIENT_PKG = "client";
    public static final String DEFAULT_MOCK_PKG = "mock";
    public static final String OAS_PATH_SEPARATOR = "/";
    public static final String UNTITLED_SERVICE = "UntitledAPI";
    //ClientCode generator
    public static final String HTTP = "http";
    public static final String URL = "url";
    public static final String TEST_DIR = "tests";
    public static final String STRING = "string";
    public static final String XML = "xml";
    public static final String JSON = "json";
    public static final String IDENTIFIER = "identifier";
    public static final String TYPE_NAME = "typeName";
    public static final String DOUBLE = "double";
    public static final String FLOAT = "float";
    public static final String MIME = "mime";

    // auth related constants
    public static final String API_KEY = "apikey";

    //Http Methods
    public static final String POST = "post";
    public static final String GET = "get";
    public static final String PUT = "put";
    public static final String DELETE = "delete";
    public static final String PATCH = "patch";
    public static final String HEAD = "head";
    public static final String OPTIONS = "options";
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
        httpCodeMap.put("200", "Ok");
        httpCodeMap.put("201", "Created");
        httpCodeMap.put("202", "Accepted");
        httpCodeMap.put("203", "NonAuthoritativeInformation");
        httpCodeMap.put("204", "NoContent");
        httpCodeMap.put("205", "RestContent");
        httpCodeMap.put("206", "PartialContent");
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
        httpCodeMap.put("408", "RequestTimeOut");
        httpCodeMap.put("409", "Conflict");
        httpCodeMap.put("410", "Gone");
        httpCodeMap.put("411", "LengthRequired");
        httpCodeMap.put("412", "PreconditionFailed");
        httpCodeMap.put("413", "PayloadTooLarge");
        httpCodeMap.put("414", "UriTooLong");
        httpCodeMap.put("415", "UnsupportedMediaType");
        httpCodeMap.put("416", "RangeNotSatisfiable");
        httpCodeMap.put("417", "ExpectationFailed");
        httpCodeMap.put("426", "UpgradeRequired");
        httpCodeMap.put("429", "TooManyRequests");
        httpCodeMap.put("431", "RequestHeaderFieldsTooLarge");
        httpCodeMap.put("500", "InternalServerError");
        httpCodeMap.put("501", "NotImplemented");
        httpCodeMap.put("502", "BadGateway");
        httpCodeMap.put("503", "ServiceUnavailable");
        httpCodeMap.put("504", "GatewayTimeout");
        httpCodeMap.put("505", "HttpVersionNotSupported");
        HTTP_CODES_DES = Collections.unmodifiableMap(httpCodeMap);
    }

    public static final String NUMBER = "number";
    public static final String BAL_EXTENSION = ".bal";
    public static final String JSON_EXTENSION = ".json";
    public static final String YAML_EXTENSION = ".yaml";
    public static final String YML_EXTENSION = ".yml";

    public static final Map<String, String> TYPE_MAP;

    static {
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
        typeMap.put("byte", "byte[]");
        TYPE_MAP = Collections.unmodifiableMap(typeMap);
    }

    // OS specific line separator
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String DOUBLE_LINE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
    public static final List<String> SUPPORTED_OPENAPI_VERSIONS =
            List.of("2.0", "3.0.0", "3.0.1", "3.0.2", "3.0.3", "3.1.0");
}
