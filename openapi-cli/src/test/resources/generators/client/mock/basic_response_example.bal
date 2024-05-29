import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
        return;
    }

    # Get user
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    resource isolated function get user(map<string|string[]> headers = {}) returns ABC|error {
        return {"id": 10, "name": "Jessica Smith"};
    }

    # Get a user
    #
    # + headers - Headers to be sent with the request
    # + return - OK
    resource isolated function get users(map<string|string[]> headers = {}) returns inline_response_200|error {
        return {"id": 10, "name": "Sam Smith"};
    }
}
