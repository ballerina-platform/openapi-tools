import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service / on ep0 {
    # Get operation for the path /user
    #
    # + return - Successful
    resource function get user() returns json {
    }
    # Put operation for the path /user
    #
    # + return - Successful
    resource function put user() returns json {
    }
    # Post operation for the path /user
    #
    # + return - Successful
    resource function post user() returns OkJson {
    }
}
