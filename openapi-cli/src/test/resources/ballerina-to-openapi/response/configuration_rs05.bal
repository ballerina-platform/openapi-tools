import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    //With both etag and last-modified false
    resource function get cachingBackEnd01() returns @http:Cache{sMaxAge : -5,noStore: true,
    noTransform:true, proxyRevalidate: true} string {
           return "Hello, World!!";}

    resource function get cachingBackEnd02() returns @http:Cache{mustRevalidate: true, sMaxAge: 5,
     noStore: true, noTransform:true, proxyRevalidate: true} string {
               return "Hello, World!!";}

}
