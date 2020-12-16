 import ballerina/http;
 import ballerina/log;

 listener http:Listener helloEp = new (9090);

 service /hello on helloEp {
     //resource function get hi/[int abc](http:Caller caller, http:Request request) {
     //
     //}
     //resource function post hi(http:Caller caller, http:Request request, int id) {
     //
     //}
     resource function post hi(http:Caller caller, http:Request request, @http:Payload json payload) {

     }
 }

 service /hello02 on helloEp {
     //resource function get hi/[int abc](http:Caller caller, http:Request request) {
     //
     //}
     resource function post hi(http:Caller caller, http:Request request, int id) {

     }
 }