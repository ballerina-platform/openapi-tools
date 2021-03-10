import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: petstore.openapi.io});

service /v1 on ep0 {
    resource function get pets(int? 'limit) returns Pets|Error {
    }
    resource function post pets() returns http:Created|Error {
    }
    resource function get /pets/[string petId]() returns Pets|Error {
    }
}
