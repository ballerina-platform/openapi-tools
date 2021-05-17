import ballerina/http;

listener http:Listener ep1 = new (443, config = {host: "http://petstore.openapi.io"});

service /payloadV on   {
    resource function get pets() returns string {
        return "done";
    }
}

