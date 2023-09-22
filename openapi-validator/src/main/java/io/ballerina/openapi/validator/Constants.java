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
 * @since 1.1.0
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
    public static final String HTTP_CALLER = "http:Caller";
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
    public static final String MEDIA_TYPE = "mediaType";

    public static final Map<String, String> HTTP_CODES;
    static {
        Map<String, String> httpCodes = new HashMap<>();
        httpCodes.put("Continue", "100");
        httpCodes.put("SwitchingProtocols", "101");
        httpCodes.put("Processing", "102");
        httpCodes.put("EarlyHints", "103");
        httpCodes.put("Ok", "200");
        httpCodes.put("Created", "201");
        httpCodes.put("Accepted", "202");
        httpCodes.put("NonAuthoritativeInformation", "203");
        httpCodes.put("NoContent", "204");
        httpCodes.put("RestContent", "205");
        httpCodes.put("PartialContent", "206");
        httpCodes.put("MultiStatus", "207");
        httpCodes.put("AlreadyReported", "208");
        httpCodes.put("IMUsed", "226");
        httpCodes.put("MultipleChoices", "300");
        httpCodes.put("MovedPermanently", "301");
        httpCodes.put("Found", "302");
        httpCodes.put("SeeOther", "303");
        httpCodes.put("NotModified", "304");
        httpCodes.put("UseProxy", "305");
        httpCodes.put("TemporaryRedirect", "307");
        httpCodes.put("PermanentRedirect", "308");
        httpCodes.put("BadRequest", "400");
        httpCodes.put("Unauthorized", "401");
        httpCodes.put("PaymentRequired", "402");
        httpCodes.put("Forbidden", "403");
        httpCodes.put("NotFound", "404");
        httpCodes.put("MethodNotAllowed", "405");
        httpCodes.put("NotAcceptable", "406");
        httpCodes.put("ProxyAuthenticationRequired", "407");
        httpCodes.put("RequestTimeOut", "408");
        httpCodes.put("Conflict", "409");
        httpCodes.put("Gone", "410");
        httpCodes.put("LengthRequired", "411");
        httpCodes.put("PreconditionFailed", "412");
        httpCodes.put("PayloadTooLarge", "413");
        httpCodes.put("UriTooLong", "414");
        httpCodes.put("UnsupportedMediaType", "415");
        httpCodes.put("RangeNotSatisfiable", "416");
        httpCodes.put("ExpectationFailed", "417");
        httpCodes.put("MisdirectedRequest", "421");
        httpCodes.put("UnprocessableEntity", "422");
        httpCodes.put("Locked", "423");
        httpCodes.put("FailedDependency", "424");
        httpCodes.put("TooEarly", "425");
        httpCodes.put("UpgradeRequired", "426");
        httpCodes.put("PreconditionRequired", "428");
        httpCodes.put("TooManyRequests", "429");
        httpCodes.put("RequestHeaderFieldsTooLarge", "431");
        httpCodes.put("UnavailableDueToLegalReasons", "451");
        httpCodes.put("InternalServerError", "500");
        httpCodes.put("NotImplemented", "501");
        httpCodes.put("BadGateway", "502");
        httpCodes.put("ServiceUnavailable", "503");
        httpCodes.put("GatewayTimeout", "504");
        httpCodes.put("HttpVersionNotSupported", "505");
        httpCodes.put("VariantAlsoNegotiates", "506");
        httpCodes.put("InsufficientStorage", "507");
        httpCodes.put("LoopDetected", "508");
        httpCodes.put("NotExtended", "510");
        httpCodes.put("NetworkAuthenticationRequired", "511");
        httpCodes.put("NetworkAuthorizationRequired", "511"); //This status code was added since it is deprecated.
        // TODO: remove this after fixing https://github.com/ballerina-platform/ballerina-standard-library/issues/4245
        HTTP_CODES = Collections.unmodifiableMap(httpCodes);
    }
    public static final String HTTP_200 = "200";
    public static final String HTTP_500 = "500";
    public static final String HTTP_202 = "202";

    public static final String TRUE = "true";
    public static final String APPLICATION_JSON = "application/json";
    public static final String BODY = "body";
    public static final String SQUARE_BRACKETS = "[]";
    public static final String ANONYMOUS_RECORD = "Anonymous Record";

}
