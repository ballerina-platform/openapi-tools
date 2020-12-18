 import ballerina/http;
 import ballerina/log;

 listener http:Listener helloEp = new (9090);

 service /payloadV on helloEp {

     //resource function post hi(http:Caller caller, http:Request request, int id) {
     //
     //}
     resource function post hi(http:Caller caller, http:Request request, @http:Payload json payload) {

     }
 }

