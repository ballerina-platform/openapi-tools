import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo{
    contract: "swagger/without_filter.yaml"
}
service /v4 on new http:Listener(9090) {
    resource function post pet() {
    }
}