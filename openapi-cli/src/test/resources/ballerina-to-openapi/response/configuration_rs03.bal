import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get cachingBackEnd() returns @http:Cache{maxAge : 5, isPrivate : true,
        privateFields : ["field1","filed2"], noCache: true, noCacheFields: ["field03", "fields04"]} string {
               return "Hello, World!!";}
}
