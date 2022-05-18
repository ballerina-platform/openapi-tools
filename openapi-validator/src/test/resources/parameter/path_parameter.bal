import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"path_parameter.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function get pet/[int petId]() {
    }
    resource function get [string pet\-id]() {

    }
    resource function get pets/[boolean petId]/owner/[int owner\-id]() {
    }
}
