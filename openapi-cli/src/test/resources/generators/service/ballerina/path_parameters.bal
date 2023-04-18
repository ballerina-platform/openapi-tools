import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + petName - The id of the pet to retrieve
    # + return - returns can be any of following types
    # Pets (Expected response to a valid request)
    # http:Response (unexpected error)
    resource function get pets/[int petId]/petName/[string petName]() returns Pets|http:Response {
    }
}
