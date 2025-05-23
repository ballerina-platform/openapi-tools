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

    # op1
    #
    # + id - id value
    # + payloadId - payload id value
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function operationId01(int:Signed32 id, int payloadId, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(id)}/payload/${getEncodedUri(payloadId)}`;
        return self.clientEp->get(resourcePath, headers);
    }
}
