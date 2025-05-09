import ballerina/http;

# Move your app forward with the Uber API
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    }

    # Product Types
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of products
    remote isolated function getProducts(map<string|string[]> headers = {}, *GetProductsQueries queries) returns Product[]|error {
        string resourcePath = string `/products`;
        map<anydata> queryParam = {...queries};
        queryParam["server_token"] = self.apiKeyConfig.server_token;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath, headers);
    }

    # Price Estimates
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of price estimates by product
    remote isolated function getPrice(map<string|string[]> headers = {}, *GetPriceQueries queries) returns PriceEstimate[]|error {
        string resourcePath = string `/estimates/price`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }

    # Time Estimates
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of products
    remote isolated function getTimeEstimates(map<string|string[]> headers = {}, *GetTimeEstimatesQueries queries) returns Product[]|error {
        string resourcePath = string `/estimates/time`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }

    # User Profile
    #
    # + headers - Headers to be sent with the request
    # + return - Profile information for a user
    remote isolated function getUserProfile(map<string|string[]> headers = {}) returns Profile|error {
        string resourcePath = string `/me`;
        return self.clientEp->get(resourcePath, headers);
    }

    # User Activity
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - History information for the given user
    remote isolated function getUserActivity(map<string|string[]> headers = {}, *GetUserActivityQueries queries) returns Activities|error {
        string resourcePath = string `/history`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
}
