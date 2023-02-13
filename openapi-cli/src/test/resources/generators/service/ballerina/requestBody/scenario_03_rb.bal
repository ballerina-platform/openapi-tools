import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function put pets(http:Request request) returns http:Ok {
    }
    resource function post pets(@http:Payload Pet|xml|PetForm|string payload) returns http:Ok {
    }
}
