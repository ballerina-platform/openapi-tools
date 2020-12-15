 import ballerina/http;
 import ballerina/log;
 //import ballerina/openapi;

 listener http:Listener helloEp = new (9090);
 //listener http:Listener helloEp02 = new (9091);

//listener http:Listener helloEp = new(9090, config = {host: "localhost"});
 //@openapi:ServiceInfo {
 //     contract: "../yamls/openapi-to-ballerina.yaml"
  //}
 service /hello on helloEp {
     resource function get hi/[int abc](http:Caller caller, http:Request request) {

     }
     resource function post hi(http:Caller caller, http:Request request) {

     }
 }
