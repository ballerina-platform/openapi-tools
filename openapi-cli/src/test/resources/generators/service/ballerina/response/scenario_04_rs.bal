import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new user.
    #
    # + return - returns can be any of following types
    # http:Ok (OK)
    # http:BadRequest (Bad request. User ID must be an integer and larger than 0.)
    # http:Unauthorized (Authorization information is missing or invalid.)
    # http:NotFound (A user with the specified ID was not found.)
    resource function post user() returns http:Ok|http:BadRequest|http:Unauthorized|http:NotFound {
    }
}
