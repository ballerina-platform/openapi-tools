import ballerina/http;
import ballerina/data.jsondata;
import ballerina/data.xmldata;
import ballerina/mime;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector` 
    # + serviceUrl - URL of the target service 
    # + return - An error if connector initialization failed 
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://api.example.com/v1") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # + headers - Headers to be sent with the request 
    # + return - Created item 
    resource isolated function post items(Item payload, map<string|string[]> headers = {}) returns Item|error {
        string resourcePath = string `/items`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json; charset=utf-8");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Replaced item 
    resource isolated function put items/[string id](Item payload, map<string|string[]> headers = {}) returns Item|error {
        string resourcePath = string `/items/${getEncodedUri(id)}`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml; charset=utf-8");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Updated item 
    resource isolated function patch items/[string id](Item payload, map<string|string[]> headers = {}) returns Item|error {
        string resourcePath = string `/items/${getEncodedUri(id)}`;
        http:Request request = new;
        string encodedRequestBody = createFormURLEncodedRequestBody(check jsondata:toJson(payload).ensureType());
        request.setPayload(encodedRequestBody, "application/x-www-form-urlencoded; charset=utf-8");
        return self.clientEp->patch(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Note added 
    resource isolated function post items/[string id]/notes(string? payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/items/${getEncodedUri(id)}/notes`;
        http:Request request = new;
        request.setPayload(payload, "text/plain; charset=utf-8");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Attachment uploaded 
    resource isolated function post items/[string id]/attachments(byte[] payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/items/${getEncodedUri(id)}/attachments`;
        http:Request request = new;
        request.setPayload(payload, "application/octet-stream; qs=0.9");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Image uploaded 
    resource isolated function post items/[string id]/images(id_images_body payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/items/${getEncodedUri(id)}/images`;
        http:Request request = new;
        mime:Entity[] bodyParts = check createBodyParts(check jsondata:toJson(payload).ensureType());
        request.setBodyParts(bodyParts);
        return self.clientEp->post(resourcePath, request, headers);
    }

    # + headers - Headers to be sent with the request 
    # + return - Metadata set 
    resource isolated function post items/[string id]/metadata(Item payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/items/${getEncodedUri(id)}/metadata`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/vnd.api+json;charset=utf-8");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
