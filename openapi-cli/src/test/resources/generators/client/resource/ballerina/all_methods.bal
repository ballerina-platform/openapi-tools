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
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    }

    # List all pets
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An paged array of pets
    resource isolated function get pets(map<string|string[]> headers = {}, *ListPetsQueries queries) returns Pets|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {...queries};
        queryParam["appid1"] = self.apiKeyConfig.appid1;
        queryParam["appid2"] = self.apiKeyConfig.appid2;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, headers);
    }

    # Update a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Null response
    resource isolated function put pets(map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {};
        queryParam["appid1"] = self.apiKeyConfig.appid1;
        queryParam["appid2"] = self.apiKeyConfig.appid2;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->put(resourcePath, request, headers);
    }

    # Create a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Null response
    resource isolated function post pets(map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {};
        queryParam["appid1"] = self.apiKeyConfig.appid1;
        queryParam["appid2"] = self.apiKeyConfig.appid2;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Delete a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Ok response
    resource isolated function delete pets(map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {};
        queryParam["appid1"] = self.apiKeyConfig.appid1;
        queryParam["appid2"] = self.apiKeyConfig.appid2;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->delete(resourcePath, headers = headers);
    }
}
