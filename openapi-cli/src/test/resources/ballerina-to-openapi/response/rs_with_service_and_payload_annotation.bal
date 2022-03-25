import ballerina/http;

@http:ServiceConfig{
    mediaTypeSubtypePrefix: "swanlake"
}
service /payloadV on new http:Listener(9090) {
    resource function get greeting() returns @http:Payload{mediaType: "application/fake+json"} string {
        return "Hello, World!";
    }
}
