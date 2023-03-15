import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Description
    #
    # + 'limit - parameter description
    # + return - An paged array of pets
    resource function get pet(int 'limit = 10) returns http:Ok {
    }
    # Description
    #
    # + petType - parameter description
    # + return - An paged array of pets
    resource function get pets(string[] petType = ["dog","cat"]) returns http:Ok {
    }
}
