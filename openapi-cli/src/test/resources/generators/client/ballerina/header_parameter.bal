import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    }

    # Info for a specific pet
    #
    # + headers - Headers to be sent with the request
    # + return - Expected response to a valid request
    remote isolated function showPetById(ShowPetByIdHeaders headers) returns error? {
        string resourcePath = string `/pets`;
        map<anydata> headerValues = {...headers};
        headerValues["X-API-KEY"] = self.apiKeyConfig.X\-API\-KEY;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
