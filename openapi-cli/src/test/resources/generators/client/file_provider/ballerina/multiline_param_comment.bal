import ballerina/http;

# APIs for fine-tuning and managing deployments of OpenAI models.
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding,};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
    }

    # Gets the events for the fine-tune job specified by the given fine-tune-id.
    # Events are created when the job status changes, e.g. running or complete, and when results are uploaded.
    #
    # + fine\-tune\-id - The identifier of the fine-tune job.
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Success
    remote isolated function fineTunesGetEvents(string fine\-tune\-id, map<string|string[]> headers = {}, *FineTunesGetEventsQueries queries) returns EventList|error {
        string resourcePath = string `/fine-tunes/${getEncodedUri(fine\-tune\-id)}/events`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        map<anydata> headerValues = {...headers};
        headerValues["api-key"] = self.apiKeyConfig.api\-key;
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
