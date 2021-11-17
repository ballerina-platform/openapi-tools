 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service /payloadXml on helloEp {

     resource function post hi02(@http:Payload {} xml payload) {
     }
 }

