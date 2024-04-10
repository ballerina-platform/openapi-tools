import ballerina/http;

# This is a sample Pet Store Server based on the OpenAPI 3.0 specification.
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
    # Add a new pet to the store
    #
    # + autoUnicode - Specifies how to deal with message text that contains characters not present in the GSM 03.38 character set.
    # Messages that contain only GSM 03.38 characters are not affected by this setting.
    # If the value is `true` then a message containing non-GSM 03.38 characters will be transmitted as a Unicode SMS (which is most likely more costly).
    # Please note: when `auto-unicode` is `true` and the value of the `encoding` property is specified as `UNICODE`, the message will always be sent as `UNICODE`.
    # If the value is `false` and the `encoding` property is `TEXT` then non-GSM 03.38 characters will be replaced by the `?` character.
    # When using this setting on the API, you should take case to ensure that your message is _clean_.
    # Invisible unicode and unexpected characters could unintentionally convert an message to `UNICODE`.  A common mistake is to use the backtick character (\`) which is unicode and will turn your`TEXT` message into a `UNICODE` message.
 MISSING[`]    # + payload - Create a new pet in the store
    # + return - Successful operation
    remote isolated function addPet(json payload, boolean autoUnicode = false) returns json|error {
        string resourcePath = string `/pet`;
        map<anydata> queryParam = {"auto-unicode": autoUnicode};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        request.setPayload(payload, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
}
