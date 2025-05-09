import ballerina/http;

# This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
# Swagger at [https://swagger.io](https://swagger.io). In the third iteration of the pet store, we've switched to the design first approach!
# You can now help us improve the API whether it's by making changes to the definition itself or to the code.
# That way, with time, we can improve the API in general, and expose some of the new features in OAS3.
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

    # Finds Pets by status
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - successful operation
    remote isolated function findPetsByStatus(map<string|string[]> headers = {}, *FindPetsByStatusQueries queries) returns Pet[]|error {
        string resourcePath = string `/pet/findByStatus`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
}
