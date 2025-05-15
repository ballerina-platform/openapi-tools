import ballerina/http;
import ballerina/data.jsondata;
import ballerina/data.xmldata;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "http://localhost:9090/") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Request body has object schema content without the properties field
    #
    # + headers - Headers to be sent with the request
    # + payload - A JSON object containing pet information
    # + return - Ok
    remote isolated function op02(record {} payload, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/greeting`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # The Request body is with reference to the reusable requestBody with content type has object without properties
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function op01(record {} payload, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/greeting`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # RequestBody has object content without properties with vendor-specific media type vnd.petstore.v3.diff+json
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function op04(record {} payload, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/greeting02`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/vnd.petstore.v3.diff+json");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # RequestBody has object content without properties with application/xml
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function op03(record {} payload, map<string|string[]> headers = {}) returns string|error {
        string resourcePath = string `/greeting02`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Request body has properties with {} value
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function op05(record {} payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/greeting03`;
        http:Request request = new;
        json jsonBody = jsondata:toJson(payload);
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->put(resourcePath, request, headers);
    }

    # Request body has non-standard media type application/zip with object content without properties
    #
    # + headers - Headers to be sent with the request
    # + return - Ok
    remote isolated function op06(http:Request request, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/greeting03`;
        // TODO: Update the request as needed;
        return self.clientEp->post(resourcePath, request, headers);
    }
}
