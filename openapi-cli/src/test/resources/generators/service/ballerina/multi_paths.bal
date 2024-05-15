import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific user
    #
    # + return - An paged array of pets
    resource function get .() returns Pets {
    }

    resource function get admin/api/'2021\-10/customers/[string \{customer_id\}\.json](string? fields) returns http:Ok|error {
    }

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
    resource function get pets/[string petId]() returns Pets|ErrorDefault {
    }

    # Create a pet
    #
    # + return - returns can be any of following types
    # http:Created (Null response)
    # http:DefaultStatusCodeResponse (unexpected error)
    resource function post pets() returns http:Created|ErrorDefault {
    }
}
