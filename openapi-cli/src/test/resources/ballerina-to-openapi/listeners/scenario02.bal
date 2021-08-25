import ballerina/http;

listener http:Listener ep0 = new (80);

service /v1 on ep0 {
    resource function get pets(int? 'limit) returns Pets|Error {
    }
}

