import ballerina/http;

# This is a sample Pet Store Server based on the OpenAPI 3.0 specification.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://petstore3.swagger.io/api/v3") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding,};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Returns pet inventories by status
    #
    # + headers - Headers to be sent with the request
    # + return - successful operation
    remote isolated function getInventory(GetInventoryHeaders headers = {}) returns record {|int:Signed32...;|}|error {
        string resourcePath = string `/store/inventory`;
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
