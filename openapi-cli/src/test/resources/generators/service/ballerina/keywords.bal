import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore.swagger.io"});

service /v2 on ep0 {
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets
    resource function get pets(int? 'limit) returns Pets {
    }
}
