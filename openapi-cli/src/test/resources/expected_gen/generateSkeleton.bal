import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - returns can be any of following types
    # http:Ok (An paged array of pets)
    # http:DefaultStatusCodeResponse (unexpected error)
    resource function get pets(int? 'limit) returns Pets|ErrorDefault {
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + return - returns can be any of following types
    # http:Ok (Expected response to a valid request)
    # http:DefaultStatusCodeResponse (unexpected error)
    resource function get pets/[string petId]() returns Dog|ErrorDefault {
    }
    # Create a pet
    #
    # + return - returns can be any of following types
    # http:Created (Null response)
    # http:DefaultStatusCodeResponse (unexpected error)
    resource function post pets() returns http:Created|ErrorDefault {
    }
}
