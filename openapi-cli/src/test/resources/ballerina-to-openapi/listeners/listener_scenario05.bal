import ballerina/http;

listener http:Listener ep1 = new (443, config = {host: "http://petstore.openapi.io"});

service /payloadV on new http:Listener(8080), ep1  {
    resource function get pets() returns string {
        return "done";
    }
}

