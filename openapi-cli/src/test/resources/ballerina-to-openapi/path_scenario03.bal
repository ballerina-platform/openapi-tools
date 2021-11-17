 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service /payloadV on helloEp {
     resource function post . () {
     }
     resource function get . () {
     }
     resource function get v1/[int id] () {
     }
 }

