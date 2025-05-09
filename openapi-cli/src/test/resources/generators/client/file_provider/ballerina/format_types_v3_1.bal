import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://app.launchdarkly.com/api/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding,};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op1(map<string|string[]> headers = {}) returns StringObject|error {
        string resourcePath = string `/projects`;
        return self.clientEp->get(resourcePath, headers);
    }

    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op2(map<string|string[]> headers = {}) returns IntegerObject|error {
        string resourcePath = string `/projects`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op3(map<string|string[]> headers = {}) returns NumberObject|error {
        string resourcePath = string `/projects`;
        return self.clientEp->delete(resourcePath, headers = headers);
    }
}
