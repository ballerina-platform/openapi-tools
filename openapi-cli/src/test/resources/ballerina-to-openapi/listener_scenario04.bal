import ballerina/http;

listener http:Listener ep0 = new http:Listener(80);
listener http:Listener ep1 = new (443, config = {host: "http://petstore.openapi.io"});

service /payloadV on ep0, ep1 {
    resource function get pets() returns string {
        return "done";
    }
}

