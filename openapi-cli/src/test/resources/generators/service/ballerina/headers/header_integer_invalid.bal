import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # List all pets
    #
    # + 'x\-request\-id - Unique request ID
    # + return - An array of pets
    resource function get pets(@http:Header int 'x\-request\-id) returns http:Ok {
    }
}
