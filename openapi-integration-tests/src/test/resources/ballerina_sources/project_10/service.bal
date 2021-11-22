import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    title: "Mock file"
}
service /titleBase on new http:Listener(9090) {
    resource function get title() returns string {
        return "Hello, World!";
    }
}
