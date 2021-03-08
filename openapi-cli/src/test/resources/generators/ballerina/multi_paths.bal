import ballerina/http;
import ballerina/openapi;

listener http:Listener ep0 = new (80, config = {host: petstore.openapi.io});

service /v1 on ep0 {
    resource function get pets(http:Request request, int? 'limit) returns Pets|Error {
    }
    resource function post pets(http:Request request) returns http:|Error {
    }
    resource function get /pets/[string petId](http:Request request) returns Pets|Error {
    }
}
