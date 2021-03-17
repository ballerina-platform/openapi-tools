import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: petstore.openapi.io});

service /payloadV on ep0 {
    resource function post user() returns string {
    }
}
