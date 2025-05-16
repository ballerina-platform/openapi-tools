import ballerina/http;
import ballerina/data.xmldata;
import ballerina/data.jsondata;

# refComponent
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Request Body has allOf with specific properties.
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    remote isolated function updateXMLUser(path01_body payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/path01`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # Request Body has nested allOf.
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    remote isolated function postXMLUser(path01_body_1 payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/path01`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Request Body has Array type AllOf.
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    remote isolated function postXMLUserInLineArray(record {*User; boolean? hunts?; int? age?;}[] payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/path02`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
