import ballerina/http;

# Here you can find documentation for COVID-19 REST API.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://api-cov19.now.sh/") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # List of all countries with COVID-19 cases
    #
    # + headers - Headers to be sent with the request
    # + return - Default response with array of strings
    remote isolated function getCountryList(map<string|string[]> headers = {}) returns CountryInfo[]|error {
        string resourcePath = string `/api/v1/countries/list/`;
        return self.clientEp->get(resourcePath, headers);
    }
}
