 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service / on helloEp {
     resource function post hi(int id) {}
 }
