import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"request_body.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function post pets(@http:Payload json payload) {
    }
}
