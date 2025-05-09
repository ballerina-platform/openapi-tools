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
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Returns the comments posted on the track(track_id).
    #
    # + track_id - SoundCloud Track id
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Success
    remote isolated function getCommentsOnTrack(int track_id, map<string|string[]> headers = {}, *GetCommentsOnTrackQueries queries) returns inline_response_200|error {
        string resourcePath = string `/tracks/${getEncodedUri(track_id)}/comments`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
}
