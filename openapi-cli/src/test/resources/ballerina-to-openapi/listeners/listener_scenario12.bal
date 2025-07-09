import ballerina/http;

listener http:Listener ep0 = new (8080, {host: "http://petstore.openapi.io"});

service /payloadV on ep0 {
    resource function get pets() returns string {
            return "done";
        }
}
