import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new user.
    #
    # + return - A User object
    resource function post user() returns http:Accepted {
    }
    # Creates a new user.
    #
    # + return - A User object
    resource function post user2() returns http:Accepted {
    }
}
