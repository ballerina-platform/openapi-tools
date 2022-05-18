import ballerina/openapi;
import ballerina/http;

@openapi:ServiceInfo {
    contract:"query_parameter.yaml"
}
service /v1 on new http:Listener(9090) {
    resource function get pets(string offset) {
    }
    resource function get pets02(int offset, string limits) {
    }
}
