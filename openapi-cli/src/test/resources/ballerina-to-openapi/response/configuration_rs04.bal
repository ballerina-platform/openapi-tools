import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    //With both etag and last-modified false
    resource function get cachingBackEnd01() returns @http:Cache{maxAge : -5} string {
           return "Hello, World!!";}

}
