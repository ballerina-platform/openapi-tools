import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"without_return.yaml"
}
service on new http:Listener(9090) {
    resource function get .() {
    }
}
