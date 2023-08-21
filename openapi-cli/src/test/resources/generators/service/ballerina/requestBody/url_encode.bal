import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Post operation for the path /user
    #
    # + payload - parameter description
    # + return - Successful
    resource function post user(@http:Payload map<string> payload) returns OkJson {
    }
    # Post operation for the path /user
    #
    # + payload - parameter description
    # + return - Successful
    resource function post user02(@http:Payload map<string> payload) returns OkJson {
    }
    # Post operation for the path /user
    #
    # + payload - parameter description
    # + return - Successful
    resource function post user03(@http:Payload map<string> payload) returns OkJson {
    }
}
