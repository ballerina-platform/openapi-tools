import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new pets.
    #
    # + return - returns can be any of following types
    # OkUser (Success)
    # BadRequestError (Bad request)
    # NotFoundError (Not found)
    resource function post pets() returns OkUser|BadRequestError|NotFoundError {
    }
}
