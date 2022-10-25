import ballerina/http;

# This is a generated connector for [SoundCloud API v1.0.0](https://developers.soundcloud.com/) OpenAPI Specification.
# SoundCloud API provides capability to access the online audio distribution platform and music sharing website that enables you to upload,promote, and share audio, as well as a digital signal processor enabling listeners to stream audio.
@display {label: "SoundCloud", iconPath: "resources/soundcloud.svg"}
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    # The connector initialization requires setting the API credentials.
    # Create an [SoundCloud account](https://soundcloud.com/) and obtain tokens following [this guide](https://developers.soundcloud.com/docs/api/guide).
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://api.soundcloud.com") returns error? {
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
    # Returns the comments posted on the track(track_id).
    #
    # + track_id - SoundCloud Track id
    # + 'limit - Number of results to return in the collection.
    # + offset - Offset of first result. Deprecated, use `linked_partitioning` instead.
    # + linked_partitioning - Returns paginated collection of items (recommended, returning a list without pagination is deprecated and should not be used)
    # # Deprecated parameters
    # + offset -
    # + return - Success
    remote isolated function getCommentsOnTrack(int track_id, int 'limit = 50, @deprecated int offset = 0, boolean? linked_partitioning = ()) returns Inline_response_200|error {
        string resourcePath = string `/tracks/${getEncodedUri(track_id)}/comments`;
        map<anydata> queryParam = {"limit": 'limit, "offset": offset, "linked_partitioning": linked_partitioning};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Inline_response_200 response = check self.clientEp->get(resourcePath);
        return response;
    }
}
