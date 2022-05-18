import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"error_return.yaml"
}
service on new http:Listener(9090) {
    resource function get .() returns error? {
    }
}
