import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new pets.
    #
    # + return - returns can be any of following types
    # http:Ok (Success)
    # http:BadRequest (Bad request)
    # http:NotFound (Not found)
    resource function post pets() returns UserOk|ErrorBadRequest|ErrorNotFound {
    }
}
