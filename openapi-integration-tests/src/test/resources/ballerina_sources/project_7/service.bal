import ballerina/http;

service /mTitle on new http:Listener(9090) {
    resource function get title() returns string {
        return "Hello, World!";
    }
}
service /mVersion on new http:Listener(9090) {
    resource function get versions() returns string {
        return "Hello, World!";
    }
}
