import ballerina/http;

listener http:Listener helloEp = new (9090);

type Pet record {
    int id;
    string name;
    string tag?;
 };

 type Dog record {
     Pet perant;
     boolean bark;
  };
 service /payloadV on helloEp {
     //resource function post hi(http:Caller caller, http:Request request, @http:Payload { mediaType:[​
     //"application/json"] } Dog payload) {
     //
     //}

     //resource function post pets (​http:Request request, @http:Payload {mediaType["application/json"]​} ​ json ​
     //payload​) returns http:Ok {
     //     }
     //}

     resource function get users (​ http:Caller​ ​ caller​, http:Request request) returns @http:Payload
     {mediaType:["application/json"]} ArrayOfUsers |
     @http:Payload {mediaType:["text/plain"]} string {

      }

