import ballerina/http;
import ballerina/data.jsondata;

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

    # + headers - Headers to be sent with the request
    # + return - Switching protocols
    resource isolated function post users(User payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/users`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request
    # + return - Created
    resource isolated function post users02(User payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/users02`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request
    # + return - Moved Permanently
    resource isolated function post user3(User payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/user3`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # This status code will be generate with previous approach till we address the error status code.
    #
    # + headers - Headers to be sent with the request
    # + return - Unauthorized
    resource isolated function post user4(User payload, map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/user4`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
