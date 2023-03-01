import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Creates a new user.
    #
    # + payload - parameter description
    # + return - OK
    resource function post requestBody(@http:Payload User payload) returns http:Ok {
    }
}