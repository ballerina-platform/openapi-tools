import ballerina/http;

# refComponent
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Creates a new user.
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    remote isolated function addUser(byte[] payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/user`;
        http:Request request = new;
        request.setPayload(payload, "application/octet-stream");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Creates a new payment.
    #
    # + headers - Headers to be sent with the request
    # + payload - Details of the pet to be purchased
    # + return - OK
    remote isolated function addPayment(byte[] payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/payment`;
        http:Request request = new;
        request.setPayload(payload, "application/octet-stream");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
