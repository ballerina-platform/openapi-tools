import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    'version: "4.0.0"
}
service /versionBase on new http:Listener(9090) {
    resource function get title() returns string {
        return "Hello, World!";
    }
}
