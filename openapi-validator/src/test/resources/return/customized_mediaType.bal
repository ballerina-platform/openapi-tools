import ballerina/openapi;
import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

@openapi:ServiceInfo{
    contract: "customized_mediaType.yaml",
    failOnErrors: false
}
@http:ServiceConfig { mediaTypeSubtypePrefix: "vnd.snowpeak.reservation"}
service /v4 on ep0 {
    resource function post pet/[string id](@http:Payload string payload) returns string {
        return "test";
    }
}
