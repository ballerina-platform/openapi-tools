import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "localhost:9090/payloadV") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # + version - Version Id
    # + version\-name - Version Name
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function operationId04(int version, string version\-name, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri(version\-name)}`;
        return self.clientEp->get(resourcePath, headers);
    }

    # + version\-id - Version Id
    # + version\-limit - Version Limit
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function operationId05(int version\-id, int version\-limit, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version\-id)}/v2/${getEncodedUri(version\-limit)}`;
        return self.clientEp->get(resourcePath, headers);
    }
}
