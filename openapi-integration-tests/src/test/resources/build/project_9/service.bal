import ballerina/http;

service /ชื่\u{E2D} on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}
