import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets or unexpected error
    resource function get pets(int? 'limit) returns Pets|http:Response {
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + return - Expected response to a valid request or unexpected error
    resource function get pets/[string petId]() returns Pets|http:Response {
    }
}