import  ballerina/http;
import  ballerina/xmldata;

public isolated client class Client {
    public final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration  clientConfig =  {}, string serviceUrl = "http://localhost:9090/petstore/v1") returns error? {
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
    remote isolated function  pet(Pet payload) returns http:Response | error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        http:Response  response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    remote isolated function getPetId(string petId, string payload) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        http:Request request = new;
        request.setPayload(payload);
        http:Response  response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    remote isolated function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}/Image/${getEncodedUri(imageId)}`;
        http:Response  response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
