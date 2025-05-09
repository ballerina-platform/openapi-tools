import ballerina/http;

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

    # List all pets
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + payload - Pet
    # + return - Return json
    remote isolated function listPets(json payload, map<string|string[]> headers = {}, *ListPetsQueries queries) returns json|error {
        string resourcePath = string `/pets`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
