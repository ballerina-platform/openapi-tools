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
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config = {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
        http:ClientConfiguration httpClientConfig = {
            httpVersion: config.httpVersion,
            timeout: config.timeout,
            forwarded: config.forwarded,
            poolConfig: config.poolConfig,
            compression: config.compression,
            circuitBreaker: config.circuitBreaker,
            retryConfig: config.retryConfig,
            validation: config.validation
        };
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
    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + return - An array of products
    remote isolated function getProducts(decimal latitude, decimal longitude) returns Product[]|error {
        string resourcePath = string `/products`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude, "server_token": self.apiKeyConfig.server_token};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Price Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + end_latitude - Latitude component of end location.
    # + end_longitude - Longitude component of end location.
    # + return - An array of price estimates by product
    remote isolated function getPrice(decimal start_latitude, decimal start_longitude, decimal end_latitude, decimal end_longitude) returns PriceEstimate[]|error {
        string resourcePath = string `/estimates/price`;
        map<anydata> queryParam = {"start_latitude": start_latitude, "start_longitude": start_longitude, "end_latitude": end_latitude, "end_longitude": end_longitude};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        PriceEstimate[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Time Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + customer_uuid - Unique customer identifier to be used for experience customization.
    # + product_id - Unique identifier representing a specific product for a given latitude & longitude.
    # + return - An array of products
    remote isolated function getTimeEstimates(decimal start_latitude, decimal start_longitude, string? customer_uuid = (), string? product_id = ()) returns Product[]|error {
        string resourcePath = string `/estimates/time`;
        map<anydata> queryParam = {"start_latitude": start_latitude, "start_longitude": start_longitude, "customer_uuid": customer_uuid, "product_id": product_id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # User Profile
    #
    # + return - Profile information for a user
    remote isolated function getUserProfile() returns Profile|error {
        string resourcePath = string `/me`;
        Profile response = check self.clientEp->get(resourcePath);
        return response;
    }
    # User Activity
    #
    # + offset - Offset the list of returned results by this amount. Default is zero.
    # + 'limit - Number of items to retrieve. Default is 5, maximum is 100.
    # + return - History information for the given user
    remote isolated function getUserActivity(int? offset = (), int? 'limit = ()) returns Activities|error {
        string resourcePath = string `/history`;
        map<anydata> queryParam = {"offset": offset, "limit": 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Activities response = check self.clientEp->get(resourcePath);
        return response;
    }
}
