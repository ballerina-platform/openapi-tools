import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Show a list of pets in the system.
    # Pet can be in any category
    #
    # + 'limit - How many items to return at one time (max 100). Min is 1
    # + return - returns can be any of following types
    # Pets (OK. An paged array of pets)
    # http:Response (unexpected error)
    resource function get pets(int? 'limit) returns Pets|http:Response {
    }
}
