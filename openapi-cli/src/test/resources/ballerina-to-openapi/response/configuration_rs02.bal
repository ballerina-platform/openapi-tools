import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get cachingBackEnd(http:Request req) returns @http:CacheConfig{mustRevalidate: true, maxAge : 5}
    string {
        return "Hello, World!!";
    }
}
