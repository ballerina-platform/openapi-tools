/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for Constants used in validator plugin.
 *
 * @since 2.0.0
 */
public class Constants {
    public static final String ATTRIBUTE_CONTRACT_PATH = "contract";
    public static final String ATTRIBUTE_TAGS = "tags";
    public static final String ATTRIBUTE_OPERATIONS = "operations";
    public static final String ATTRIBUTE_EXCLUDE_TAGS = "excludeTags";
    public static final String ATTRIBUTE_EXCLUDE_OPERATIONS = "excludeOperations";
    public static final String ATTRIBUTE_FAIL_ON_ERRORS = "failOnErrors";
    public static final String BALLERINA = "ballerina";
    public static final String FALSE = "false";
    public static final String EMBED = "embed";
    public static final String JSON = ".json";
    public static final String HTTP = "http";
    public static final String OPENAPI_ANNOTATION = "openapi:ServiceInfo";
    public static final String YAML = ".yaml";
    public static final String YML = ".yml";
    public static final String GET = "get";
    public static final String POST = "post";
    public static final String PUT = "put";
    public static final String DELETE = "delete";
    public static final String HEAD = "head";
    public static final String PATCH = "patch";
    public static final String OPTIONS = "options";
    public static final String TRACE = "trace";
    public static final String FULL_STOP = ".";
    public static final String SLASH = "/";
    public static final String HTTP_HEADER = "http:Header";
    public static final String HTTP_PAYLOAD = "http:Payload";
    public static final String HTTP_REQUEST = "http:Request";
    public static final String HTTP_RESPONSE = "http:Response";
    public static final String ARRAY = "array";
    public static final String ARRAY_BRACKETS = "[]";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String INTEGER = "integer";
    public static final String OBJECT = "object";
    public static final String DECIMAL = "decimal";
    public static final String RECORD = "record";
    public static final String NUMBER = "number";
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String DEFAULT = "default";
    public static final String DOUBLE = "double";
    public static final String HEADER_NAME = "name";
    public static final String WILD_CARD_CONTENT_KEY = "*/*";
    public static final String WILD_CARD_SUMMARY = "Any type of entity body";

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

}
