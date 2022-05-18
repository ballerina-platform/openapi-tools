import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    contract: "parameter_with_reference.yaml"
}
service /data/'2\.5 on new http:Listener(9091) {
    resource function get weather(string? id, string? lat) returns string {
        return "hello";
    }
}