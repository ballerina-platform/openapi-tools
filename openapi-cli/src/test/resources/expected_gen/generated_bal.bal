import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + petName - The id of the pet to retrieve
    # + return - Expected response to a valid request or unexpected error
    resource function get pets/[string petId]/petName/[string petName]() returns Pets|http:Response {
    }
}
