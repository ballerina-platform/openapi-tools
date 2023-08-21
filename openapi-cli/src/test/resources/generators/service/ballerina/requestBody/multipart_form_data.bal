import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # Post operation for the path /user
    #
    # + request - parameter description
    # + return - Successful
    resource function post user(http:Request request) returns OkJson {
    }
}
