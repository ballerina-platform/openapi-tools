import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    contract: "openapi.yaml",
    excludeTags: ["Weather Forecast"],
}

service /data on new http:Listener(90) {
    resource function get weather(string? id, string? lat, string? lon, string? zip, string? units, string? lang, int? mode) returns string {
        return "hello";
    }
    resource function get onecall(int lon, string? exclude, string? units, string? lang) returns string {
        return "hello";
    }
}

service /data02 on new http:Listener(92) {
    resource function get weather() returns string {
        return "hello";
    }
    resource function get onecall() returns string {
        return "hello";
    }
}