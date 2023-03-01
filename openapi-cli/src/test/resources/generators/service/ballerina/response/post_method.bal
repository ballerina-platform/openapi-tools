import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new user.
    #
    # + return - OK
    resource function post user() returns http:Ok {
    }
    # post method with 201
    #
    # + return - Created
    resource function post user02() returns http:Created {
    }
    # post method with 201 with content
    #
    # + return - Created
    resource function post user03() returns string {
    }
    # post method with 201,200
    #
    # + return - returns can be any of following types
    # string (Created)
    # OkString (ok)
    resource function post user04() returns string|OkString {
    }
}
