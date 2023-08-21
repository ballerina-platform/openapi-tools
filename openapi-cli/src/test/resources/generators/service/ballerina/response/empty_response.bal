import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /petstore/v1 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - http:Response
    resource function get pets(int? 'limit) returns http:Response {
    }
}
