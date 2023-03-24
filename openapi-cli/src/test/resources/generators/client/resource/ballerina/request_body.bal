import ballerina/http;
import ballerina/xmldata;

# refComponent
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
    # 02 Example for rb has inline requestbody.
    #
    # + return - OK
    resource isolated function put path01(Path01_body payload) returns http:Response|error {
        string resourcePath = string `/path01`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        http:Response response = check self.clientEp->put(resourcePath, request);
        return response;
    }
    # 01 Request body with reference.
    #
    # + return - OK
    resource isolated function post path01(User payload) returns http:Response|error {
        string resourcePath = string `/path01`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        http:Response response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    # 04 Example for rb has inline requestbody.
    #
    # + payload - A JSON object containing pet information
    # + return - OK
    resource isolated function put path02(User payload) returns http:Response|error {
        string resourcePath = string `/path02`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        http:Response response = check self.clientEp->put(resourcePath, request);
        return response;
    }
    # 03 Request body with record reference.
    #
    # + return - OK
    resource isolated function post path02(User[] payload) returns http:Response|error {
        string resourcePath = string `/path02`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        http:Response response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    # 06 Example for rb has array inline requestbody.
    #
    # + return - OK
    resource isolated function put path03(Path03_body payload) returns http:Response|error {
        string resourcePath = string `/path03`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        http:Response response = check self.clientEp->put(resourcePath, request);
        return response;
    }
    # 05 Example for rb has array inline requestbody.
    #
    # + return - OK
    resource isolated function post path03(Path03_body_1 payload) returns http:Response|error {
        string resourcePath = string `/path03`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        http:Response response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    # 07 Example for rb has array inline requestbody.
    #
    # + return - OK
    resource isolated function post path04(Path04_body[] payload) returns http:Response|error {
        string resourcePath = string `/path04`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        http:Response response = check self.clientEp->post(resourcePath, request);
        return response;
    }
}
