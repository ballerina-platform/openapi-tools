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
    # + return - Ok
    resource isolated function get .() returns string|error {
        string resourcePath = string `/`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function post .() returns string|error {
        string resourcePath = string `/`;
        http:Request request = new;
        //TODO: Update the request as needed;
        string response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    # op2
    #
    # + id - id value
    # + return - Ok
    resource isolated function get v1/[int id]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(id)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int version]/v2/[string name]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri(name)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int version]/v2/[int 'limit]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri('limit)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    #
    # + return - Ok
    resource isolated function get v1/[int age]/v2/[string name]() returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(age)}/v2/${getEncodedUri(name)}`;
        string response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Retrieves a single customer.
    #
    # + customer_id - Customer ID
    # + fields - Show only certain fields, specified by a comma-separated list of field names.
    # + return - Requested customer
    resource isolated function get admin/api/'2021\-10/customers/[string customer_idJson](string? fields = ()) returns http:Response|error {
        if !customer_idJson.endsWith(".json") {
            return error("bad URL");
        }
        string customer_id = customer_idJson.substring(0, customer_idJson.length() - 4);
        string resourcePath = string `/admin/api/2021-10/customers/${getEncodedUri(customer_id)}.json`;
        map<anydata> queryParam = {"fields": fields};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Response response = check self.clientEp->get(resourcePath);
        return response;
    }
}
