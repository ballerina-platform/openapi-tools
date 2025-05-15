import ballerina/http;
import ballerina/mime;
import ballerina/data.jsondata;

# API to handle multipart form-data requests.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Upload a file with metadata and tags
    #
    # + headers - Headers to be sent with the request
    # + return - File uploaded successfully.
    resource isolated function post upload(upload_body payload, map<string|string[]> headers = {}) returns inline_response_200|error {
        string resourcePath = string `/upload`;
        http:Request request = new;
        map<Encoding> encodingMap = {"file": {contentType: "application/octet-stream"}, "metadata": {contentType: "application/json"}, "tags": {contentType: "text/plain"}};
        mime:Entity[] bodyParts = check createBodyParts(check jsondata:toJson(payload).ensureType(), encodingMap);
        request.setBodyParts(bodyParts);
        return self.clientEp->post(resourcePath, request, headers);
    }
}
