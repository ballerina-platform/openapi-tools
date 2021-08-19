import ballerina/http;

service / on new http:Listener(9091) {
    resource function get greeting2() returns string {
        return "Hello, World!";
    }
}
