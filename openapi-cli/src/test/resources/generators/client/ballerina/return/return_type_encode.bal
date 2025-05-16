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
    # + country - Country name.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of products
    resource isolated function get products/[string country](map<string|string[]> headers = {}, *GetProductsCountryQueries queries) returns Product[]|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }

    # Product Types
    #
    # + country - Country name.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of products
    resource isolated function put products/[string country](map<string|string[]> headers = {}, *PutProductsCountryQueries queries) returns Product[]|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        http:Request request = new;
        return self.clientEp->put(resourcePath, request, headers);
    }

    # Product Types
    #
    # + country - Country name.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An array of products
    resource isolated function post products/[string country](map<string|string[]> headers = {}, *PostProductsCountryQueries queries) returns xml|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }
}
