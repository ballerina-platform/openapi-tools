import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"header.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function get pets(@http:Header string x\-offset) {
    }
    resource function get pets02(@http:Header{name: "x-offset"} string header) {
    }
}
