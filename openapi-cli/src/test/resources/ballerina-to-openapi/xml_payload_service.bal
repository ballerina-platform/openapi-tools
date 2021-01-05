 import ballerina/http;
 import ballerina/log;

 listener http:Listener helloEp = new (9090);

 service /payloadXml on helloEp {

     resource function post hi02(http:Caller caller, http:Request request, @http:Payload {} xml payload) {

     }
 }

