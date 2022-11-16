import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://api.stripe.com/") returns error? {
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
    # <p>You can list all invoices, or list the invoices for a specific customer. The invoices are returned sorted by creation date, with the most recently created invoices appearing first.</p>
    #
    # + collection_method - The collection method of the invoice to retrieve. Either `charge_automatically` or `send_invoice`.
    # + created - A filter on the list based on the object created field. The value can be a string with an integer Unix timestamp, or it can be a dictionary
    # + due_date - A filter on the list based on the object due_date field. The value can be an integer Unix timestamp, or it can be a dictionary
    # + subscriptions - Only return invoices for the subscription specified by this subscription ID.
    # + return - Response
    remote isolated function listInvoices(string? collection_method = (), Created? created = (), Due_date? due_date = (), string[]? subscriptions = ()) returns json|error {
        string resourcePath = string `/v1/invoices`;
        map<anydata> queryParam = {"collection_method": collection_method, "created": created, "due_date": due_date, "subscriptions": subscriptions};
        map<Encoding> queryParamEncoding = {"created": {style: DEEPOBJECT, explode: true}, "due_date": {style: DEEPOBJECT, explode: true}, "subscriptions": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam, queryParamEncoding);
        json response = check self.clientEp->get(resourcePath);
        return response;
    }
}
