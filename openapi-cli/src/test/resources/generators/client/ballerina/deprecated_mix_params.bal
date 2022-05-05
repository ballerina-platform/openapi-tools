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
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.soundcloud.com") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Returns the comments posted on the track(track_id).
    #
    # + trackId - SoundCloud Track id
    # + 'limit - Number of results to return in the collection.
    # + offset - Offset of first result. Deprecated, use `linked_partitioning` instead.
    # + linkedPartitioning - Returns paginated collection of items (recommended, returning a list without pagination is deprecated and should not be used)
    # # Deprecated parameters
    # + offset -
    # + return - Success
    remote isolated function getCommentsOnTrack(int trackId, int 'limit = 50, @deprecated int offset = 0, boolean? linkedPartitioning = ()) returns InlineResponse200|error {
        string resourcePath = string `/tracks/${getEncodedUri(trackId)}/comments`;
        map<anydata> queryParam = {"limit": 'limit, "offset": offset, "linked_partitioning": linkedPartitioning};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        InlineResponse200 response = check self.clientEp->get(resourcePath);
        return response;
    }
}
