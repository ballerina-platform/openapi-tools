import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get ping(@http:Header string? 'x\-client) returns http:Ok {
    }
}
