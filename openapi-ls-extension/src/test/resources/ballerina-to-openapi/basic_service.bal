 import ballerina/http;

 //listener http:Listener helloEp = new (9090);
 listener http:Listener ep1 = new(443, config = {host: "petstore.swagger.io"});


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
