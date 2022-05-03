import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"type_mismatch_request_body.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function post pet(@http:Payload json payload) {
    }
}
