import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new pets.
    #
    # + return - A JSON object containing pet information
    resource function post pets() returns Inline_response_201 {
    }
}
