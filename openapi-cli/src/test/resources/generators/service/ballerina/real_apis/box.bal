import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + return - returns can be any of following types
    # Pets (Expected response to a valid request)
    # http:TooManyRequests (unexpected error)
    resource function get pets/[string petId]() returns Pets|http:TooManyRequests {
    }
}
