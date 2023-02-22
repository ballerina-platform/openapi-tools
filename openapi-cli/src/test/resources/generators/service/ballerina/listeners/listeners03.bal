import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /v1 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets or unexpected error
    resource function get pets(int? 'limit) returns Pets|http:Response {
    }
}
