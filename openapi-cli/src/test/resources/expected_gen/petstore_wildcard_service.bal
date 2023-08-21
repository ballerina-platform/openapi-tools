import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Create a pet
    #
    # + request - Request to add a pet
    # + return - Successful operation
    resource function post pets/my(http:Request request) returns OkAnydata {
    }
}
