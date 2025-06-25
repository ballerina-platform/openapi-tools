import ballerina/http;

listener http:Listener ep0 = new (host = "http://petstore.openapi.io", httpVersion = http:HTTP_1_1, port = 8080);

service /payloadV on ep0 {
    resource function get pets() returns string {
        return "done";
    }
}
