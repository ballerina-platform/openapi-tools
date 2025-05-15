import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "localhost:9090/payloadV") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # op1
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function get .(map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/`;
        return self.clientEp->get(resourcePath, headers);
    }

    # + headers - Headers to be sent with the request
    # + return - Ok
    resource isolated function post .(map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
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
    resource isolated function get v1/[int version]/v2/[string name](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(version)}/v2/${getEncodedUri(name)}`;
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
    resource isolated function get v1/[int age]/v2/[string name](map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/v1/${getEncodedUri(age)}/v2/${getEncodedUri(name)}`;
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
}
