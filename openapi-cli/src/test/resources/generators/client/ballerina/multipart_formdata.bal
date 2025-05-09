import ballerina/http;
import ballerina/mime;
import ballerina/data.jsondata;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "http://petstore.{host}.io/v1") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Create a pet
    #
    # + headers - Headers to be sent with the request
    # + payload - Pet
    # + return - Null response
    remote isolated function createPet(pets_body payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/pets`;
        http:Request request = new;
        mime:Entity[] bodyParts = check createBodyParts(check jsondata:toJson(payload).ensureType());
        request.setBodyParts(bodyParts);
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Create an user
    #
    # + headers - Headers to be sent with the request
    # + payload - User
    # + return - Null response
    remote isolated function createUser(user_body payload, map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/user`;
        http:Request request = new;
        mime:Entity[] bodyParts = check createBodyParts(check jsondata:toJson(payload).ensureType());
        request.setBodyParts(bodyParts);
        return self.clientEp->post(resourcePath, request, headers);
    }
}
