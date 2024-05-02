import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
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
    # Create a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Null response
    remote isolated function createPet(map<string|string[]> headers = {}) returns http:Response|error {
        string resourcePath = string `/pets`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }
    # List all pets
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An paged array of pets
    #
    # # Deprecated
    # This methods has deprecated since the Pet has deprecated
    @deprecated
    remote isolated function listPets(map<string|string[]> headers = {}, *ListPetsQueries queries) returns Pets|error {
        string resourcePath = string `/pets`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Expected response to a valid request
    #
    # # Deprecated
    @deprecated
    remote isolated function showPetById(string petId, map<string|string[]> headers = {}, *ShowPetByIdQueries queries) returns Pets|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
}
