import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    'version: "",
    title: ""
}
service /bothBlank on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}

