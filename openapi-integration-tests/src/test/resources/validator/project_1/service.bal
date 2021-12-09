import ballerina/http;
import ballerina/openapi;

@openapi:ServiceInfo {
    contract: "openapi.yaml",
    tags: [ ],
    operations:[],
    failOnErrors: true
}
@http:ServiceConfig { mediaTypeSubtypePrefix: "vnd.snowpeak.reservation"}

service /data/'2\.5 on new http:Listener(9091) {
    resource function get weather(string? id, string? lat, string? lon, string? zip, string? units, string? lang, string? mode) returns string {
        return "hello";
    }
    resource function get onecall(string lon, string? exclude, string? units, string? lang) returns string {
        return "hello";
    }
}

service /mVersion on new http:Listener(9090) {
     resource function get versions() returns string {
         return "Hello, World!";
     }
 }
