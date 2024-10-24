import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "localhost:9090/payloadV") returns error? {
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
        return;
    }
    # op1
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get .(map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/`;
        return self.clientEp->get(resourcePath, headers);
    }
    # Retrieves a single customer.
    #
    # + customer_id - Customer ID
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Requested customer
    resource isolated function get admin/api/'2021\-10/customers/[string customer_id](map<string|string[]> headers = {}, *GetCustomerQueries queries) returns error? {
        string resourcePath = string `/admin/api/2021-10/customers/${getEncodedUri(customer_id)}`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get v1/[int age]/v2/[string name](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(age)}/v2/${getEncodedUri(name)}`;
        return self.clientEp->get(resourcePath, headers);
    }
    # op2
    #
    # + id - id value
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get v1/[int id](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(id)}`;
        return self.clientEp->get(resourcePath, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get v1/[int version]/v2/[int 'limit](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri('limit)}`;
        return self.clientEp->get(resourcePath, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get v1/[int version]/v2/[string name](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri(name)}`;
        return self.clientEp->get(resourcePath, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function post .(map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }
}
