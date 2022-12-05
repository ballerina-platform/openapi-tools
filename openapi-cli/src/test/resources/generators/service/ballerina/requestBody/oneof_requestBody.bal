import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function put storageSpaces01(http:Request payload) returns http:Ok {
    }
    resource function post storageSpaces01(@http:Payload json payload) returns http:Ok {
    }
    resource function put storageSpaces02(@http:Payload string payload) returns http:Ok {
    }
    resource function post storageSpaces02(@http:Payload xml payload) returns http:Ok {
    }
    resource function put storageSpaces/[string name](@http:Payload json payload) returns http:Ok|http:BadRequest {
    }
    resource function post storageSpaces03(http:Request payload) returns http:Ok {
    }
}
