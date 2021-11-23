import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    version: "2.0.0",
    title: "Mock Yaml"
}
service / on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}
