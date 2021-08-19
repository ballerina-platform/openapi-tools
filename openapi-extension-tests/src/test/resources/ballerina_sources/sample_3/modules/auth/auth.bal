import ballerina/http;

service / on new http:Listener(9092) {
    resource function get auth() returns string {
        return "Hello, World!";
    }
}

