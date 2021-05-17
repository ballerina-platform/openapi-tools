 import ballerina/http;

 listener http:Listener helloEp = new (9090);

 service /payloadV on helloEp {
     resource function post v1(@http:Payload{} json payload) returns http:Ok {
         http:Ok ok = {body: ()};
         return ok;
     }
 }

