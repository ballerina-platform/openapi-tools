 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service /payloadV on helloEp {

     resource function post . (http:Caller caller, http:Request request) {

     }
     resource function get . (http:Caller caller, http:Request request) {

     }
     resource function get v1/[int id] (http:Caller caller, http:Request request) {

     }
 }

