 import ballerina/http;
 import ballerina/log;

 listener http:Listener helloEp = new (9090);

 service /hello/foo/bar on helloEp {
     resource function get hi/[int abc](http:Caller caller, http:Request request) {

     }
     resource function post hi(http:Caller caller, http:Request request, int id) {

     }
 }

 service /hello02/bar/baz on helloEp {
     //resource function get hi/[int abc](http:Caller caller, http:Request request) {
     //
     //}
     resource function post hi(http:Caller caller, http:Request request, int id) {

     }
 }
