import ballerina/http;

service / on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}
