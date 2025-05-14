// Copyright (c) 2023 All Rights Reserved.

import ballerina/http;

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

    # List all pets
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - An paged array of pets
    resource isolated function get pets(map<string|string[]> headers = {}, *ListPetsQueries queries) returns Pets|error {
        string resourcePath = string `/pets`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }

    # Create a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Null response
    resource isolated function post pets(map<string|string[]> headers = {}) returns error? {
        string resourcePath = string `/pets`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }

    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + headers - Headers to be sent with the request
    # + return - Expected response to a valid request
    resource isolated function get pets/[string petId](map<string|string[]> headers = {}) returns Dog|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        return self.clientEp->get(resourcePath, headers);
    }
}
