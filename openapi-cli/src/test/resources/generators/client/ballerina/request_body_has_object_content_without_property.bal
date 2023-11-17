import ballerina/http;
import ballerina/xmldata;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "http://localhost:9090/") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Request body has object schema content without the properties field
    #
    # + payload - A JSON object containing pet information
    # + return - Ok
    resource isolated function put greeting(record{} payload) returns Person|error {
        string resourcePath = string `/greeting`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        Person response = check self.clientEp->put(resourcePath, request);
        return response;
    }
    # The Request body is with reference to the reusable requestBody with content type has object without properties
    #
    # + return - Ok
    resource isolated function post greeting(record{} payload) returns string|error {
        string resourcePath = string `/greeting`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        string response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    # RequestBody has object content without properties with vendor-specific media type vnd.petstore.v3.diff+json
    #
    # + return - Ok
    resource isolated function put greeting02(record{} payload) returns string|error {
        string resourcePath = string `/greeting02`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/vnd.petstore.v3.diff+json");
        string response = check self.clientEp->put(resourcePath, request);
        return response;
    }
    # RequestBody has object content without properties with application/xml
    #
    # + return - Ok
    resource isolated function post greeting02(record{} payload) returns string|error {
        string resourcePath = string `/greeting02`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        string response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    # Request body has properties with {} value
    #
    # + return - Ok
    resource isolated function put greeting03(record{} payload) returns error? {
        string resourcePath = string `/greeting03`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request);
    }
    # Request body has non-standard media type application/zip with object content without properties
    #
    # + return - Ok
    resource isolated function post greeting03(http:Request request) returns error? {
        string resourcePath = string `/greeting03`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request);
    }
}
