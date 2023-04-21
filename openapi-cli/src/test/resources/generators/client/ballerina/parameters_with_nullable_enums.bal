import ballerina/http;

# This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
# Swagger at [https://swagger.io](https://swagger.io). In the third iteration of the pet store, we've switched to the design first approach!
# You can now help us improve the API whether it's by making changes to the definition itself or to the code.
# That way, with time, we can improve the API in general, and expose some of the new features in OAS3.
#
# _If you're looking for the Swagger 2.0/OAS 2.0 version of Petstore, then click [here](https://editor.swagger.io/?url=https://petstore.swagger.io/v2/swagger.yaml). Alternatively, you can load via the `Edit > Load Petstore OAS 2.0` menu option!_
#
# Some useful links:
# - [The Pet Store repository](https://github.com/swagger-api/swagger-petstore)
# - [The source API definition for the Pet Store](https://github.com/swagger-api/swagger-petstore/blob/master/src/main/resources/openapi.yaml)
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://petstore3.swagger.io/api/v3") returns error? {
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
    # List meetings
    #
    # + 'type - The meeting types. Scheduled, live or upcoming
    # + status - Status values that need to be considered for filter
    # + group - Employee group
    # + xDateFormat - Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) & [leettime.de](http://leettime.de/))
    # + location - Meeting location
    # + format - The response format you would like
    # + return - HTTP Status Code:200. List of meetings returned.
    remote isolated function listMeetings("Admin"|"HR"|"Engineering"? group, "scheduled"|"live"|"upcoming"? 'type = (), ("available"|"pending"?)[]? status = (), "UTC"|"LOCAL"|"OFFSET"|"EPOCH"|"LEET"? xDateFormat = (), RoomNo location = "R5", "json"|"jsonp"|"msgpack"|"html"? format = ()) returns MeetingList|error {
        string resourcePath = string `/users/meetings/${getEncodedUri(group)}`;
        map<anydata> queryParam = {"type": 'type, "status": status, "location": location, "format": format};
        map<Encoding> queryParamEncoding = {"status": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam, queryParamEncoding);
        map<any> headerValues = {"X-Date-Format": xDateFormat};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        MeetingList response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
}
