import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    # Gets a list of users.
    #
    # + return - Unauthorized
    resource function get users() returns UnauthorizedError {
    }
    # Gets a user by ID.
    #
    # + id - parameter description
    # + return - returns can be any of following types
    # User (OK)
    # UnauthorizedError (Unauthorized)
    # NotFoundError (The specified resource was not found)
    resource function get users/[int id]() returns User|UnauthorizedError|NotFoundError {
    }
}
