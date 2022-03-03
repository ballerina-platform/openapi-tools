import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo{
    contract: "undocumented_resources.yaml"
}
service /v4 on new http:Listener(9090) {
    resource function post pet() {
    }
    resource function get pet() {
    }
    resource function get pet/[int id]() {
    }
}
