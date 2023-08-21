import ballerina/http;

listener http:Listener ep0 = new(80,config={host:"petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + storeId - The id of the store to retrieve
    # + userId - The id of the user to retrieve
    # + petId - The id of the pet to retrieve
    # + return - returns can be any of following types
    # Pets (Expected response to a valid request)
    # http:Response (unexpected error)
    resource function get store/[int storeId]/users/[int userId]/pets/[int:Signed32 petId]() returns Pets|http:Response {
    }
}
