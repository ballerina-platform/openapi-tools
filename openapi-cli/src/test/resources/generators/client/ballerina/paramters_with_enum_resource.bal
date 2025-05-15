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
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # List meetings
    #
    # + group - Employee group
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - HTTP Status Code:200. List of meetings returned.
    resource isolated function get users/meetings/["Admin"|"HR"|"Engineering" group](ListMeetingsHeaders headers, *ListMeetingsQueries queries) returns MeetingList|error {
        string resourcePath = string `/users/meetings/${getEncodedUri(group)}`;
        map<Encoding> queryParamEncoding = {"status": {style: FORM, explode: true}};
        resourcePath = resourcePath + check getPathForQueryParam(queries, queryParamEncoding);
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
