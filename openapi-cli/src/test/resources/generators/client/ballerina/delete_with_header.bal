import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Delete neither header nor request body.
    #
    # + order_id - Order ID
    # + risk_id - Order Risk ID
    # + headers - Headers to be sent with the request
    # + return - Status OK
    remote isolated function deleteOrderRisk(string order_id, string risk_id, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/admin/api/2021-10/orders/${getEncodedUri(order_id)}/risks/${getEncodedUri(risk_id)}.json`;
        return self.clientEp->delete(resourcePath, headers = headers);
    }

    # Delete with request body.
    #
    # + headers - Headers to be sent with the request
    # + return - Status OK
    remote isolated function orderRisk(json payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/request-body`;
        http:Request request = new;
        request.setPayload(payload, "application/json");
        return self.clientEp->delete(resourcePath, request, headers);
    }

    # Delete with header.
    #
    # + headers - Headers to be sent with the request
    # + return - Status OK
    remote isolated function deleteHeader(DeleteHeaderHeaders headers) returns error? {
        string resourcePath = string `/header`;
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
        return self.clientEp->delete(resourcePath, headers = httpHeaders);
    }

    # Delete with header and request body.
    #
    # + headers - Headers to be sent with the request
    # + return - Status OK
    remote isolated function deleteHeaderRequestBody(DeleteHeaderRequestBodyHeaders headers, json payload) returns error? {
        string resourcePath = string `/header-with-request-body`;
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        return self.clientEp->delete(resourcePath, request, httpHeaders);
    }
}
