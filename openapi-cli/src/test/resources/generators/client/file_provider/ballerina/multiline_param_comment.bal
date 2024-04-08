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
    # Gets the events for the fine-tune job specified by the given fine-tune-id.
    # Events are created when the job status changes, e.g. running or complete, and when results are uploaded.
    #
    # + fineTuneId - The identifier of the fine-tune job.
    # + 'stream - A flag indicating whether to stream events for the fine-tune job. If set to true,
    # events will be sent as data-only server-sent events as they become available. The stream will terminate with
    # a data: [DONE] message when the job is finished (succeeded, cancelled, or failed).
    # If set to false, only events generated so far will be returned..
    # + apiVersion - The requested API version.
    # + return - Success
    remote isolated function fineTunes_GetEvents(string fineTuneId, string apiVersion, boolean? 'stream = ()) returns EventList|error {
        string resourcePath = string `/fine-tunes/${getEncodedUri(fineTuneId)}/events`;
        map<anydata> queryParam = {"stream": 'stream, "api-version": apiVersion};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<any> headerValues = {"api-key": self.apiKeyConfig.apiKey};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        EventList response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
}
