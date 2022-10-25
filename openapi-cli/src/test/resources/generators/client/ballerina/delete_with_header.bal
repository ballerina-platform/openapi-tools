import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
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
    # Delete neither header nor request body.
    #
    # + order_id - Order ID
    # + risk_id - Order Risk ID
    # + return - Status OK
    remote isolated function delete_order_risk(string order_id, string risk_id) returns http:Response|error {
        string resourcePath = string `/admin/api/2021-10/orders/${getEncodedUri(order_id)}/risks/${getEncodedUri(risk_id)}.json`;
        http:Response response = check self.clientEp-> delete(resourcePath);
        return response;
    }
    # Delete with request body.
    #
    # + return - Status OK
    remote isolated function order_risk(json payload) returns http:Response|error {
        string resourcePath = string `/request-body`;
        http:Request request = new;
        request.setPayload(payload, "application/json");
        http:Response response = check self.clientEp->delete(resourcePath, request);
        return response;
    }
    # Delete with header.
    #
    # + xRequestId - Tests header 01
    # + return - Status OK
    remote isolated function deleteHeader(string xRequestId) returns http:Response|error {
        string resourcePath = string `/header`;
        map<any> headerValues = {"X-Request-ID": xRequestId};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Response response = check self.clientEp->delete(resourcePath, headers = httpHeaders);
        return response;
    }
    # Delete with header and request body.
    #
    # + xRequestId - Tests header 01
    # + return - Status OK
    remote isolated function deleteHeaderRequestBody(string xRequestId, json payload) returns http:Response|error {
        string resourcePath = string `/header-with-request-body`;
        map<any> headerValues = {"X-Request-ID": xRequestId};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        http:Response response = check self.clientEp->delete(resourcePath, request, httpHeaders);
        return response;
    }
}
