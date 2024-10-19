import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    //With both etag and last-modified false
    resource function get cachingBackEnd01() returns @http:Cache{maxAge : 5, setETag : false,
    setLastModified : false} string {
           return "Hello, World!!";}
    //With last-modified false
    resource function get cachingBackEnd02() returns @http:Cache{maxAge : 5,
    setLastModified : false} string {
               return "Hello, World!!";}
    //With etag false
    resource function get cachingBackEnd03() returns @http:Cache{setETag : false,
    mustRevalidate:false} string {
               return "Hello, World!!";}
    //With private and without private fields
    resource function get cachingBackEnd04() returns @http:Cache{isPrivate: true} string {
               return "Hello, World!!";}

}
