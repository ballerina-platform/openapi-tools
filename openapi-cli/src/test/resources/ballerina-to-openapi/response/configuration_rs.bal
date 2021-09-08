import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    // Default scenario
    resource function get cachingBackEnd(http:Request req) returns @http:Cache{} string {
        return "Hello, World!!";
    }
}
