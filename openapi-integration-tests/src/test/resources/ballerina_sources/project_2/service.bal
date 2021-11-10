import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    contract: "hello_openapi.yaml",
    'version: "3.0.0"
}
service /greeting on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}

