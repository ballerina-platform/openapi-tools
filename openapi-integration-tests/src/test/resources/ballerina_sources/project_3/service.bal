import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    contract: "hello_openapi.yaml",
    title: "Mock API doc"
}
service /title on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}

