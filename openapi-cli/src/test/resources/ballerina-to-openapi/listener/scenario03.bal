import ballerina/http;

listener http:Listener ep0 = new (80);
listener http:Listener ep1 = new (443, config = {host: petstore.openapi.io});

service /v1 on ep0, ep1 {
    resource function get pets(int? 'limit) returns Pets|Error {
    }
}

