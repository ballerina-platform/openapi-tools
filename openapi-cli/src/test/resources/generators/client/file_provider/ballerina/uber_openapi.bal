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
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
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
    # User Profile
    #
    # + headers - Headers to be sent with the request
    # + return - Profile information for a user
    remote isolated function getUserProfile(map<string|string[]> headers = {}) returns Profile|error {
        string resourcePath = string `/me`;
        return self.clientEp->get(resourcePath, headers);
    }
}
