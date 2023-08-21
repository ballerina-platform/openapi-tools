import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    # Update a user image.
    #
    # + request - parameter description
    # + return - OK
    resource function put users(http:Request request) returns http:Ok {
    }
    # Returns a list of users.
    #
    # + request - parameter description
    # + return - OK
    resource function post users(http:Request request) returns http:Ok {
    }
    # Returns a user by ID.
    #
    # + id - ID of user to fetch
    # + request - parameter description
    # + return - OK
    resource function put users/[string id](http:Request request) returns http:Ok {
    }
}
