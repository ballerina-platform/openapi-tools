import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo{
    contract: "unimplemented_resources.yaml"
}
service /v4 on new http:Listener(9090) {
    resource function post pet() {
    }
}
