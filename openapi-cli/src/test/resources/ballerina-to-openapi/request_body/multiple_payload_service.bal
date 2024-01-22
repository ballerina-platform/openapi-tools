import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function post hi(@http:Payload {mediaType: ["application/vnd+json", "application/vnd+xml"]} json|xml payload) {
    }
}
