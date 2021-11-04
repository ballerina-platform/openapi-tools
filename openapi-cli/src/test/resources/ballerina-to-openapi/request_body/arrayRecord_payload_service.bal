import ballerina/http;

listener http:Listener helloEp = new (9090);

type Pet record {
    int id;
    string name;
    string [][][] tag?;
 };

 type Dog record {
     Pet perant;
     boolean bark;
  };
 service /payloadV on helloEp {
     resource function post hi(@http:Payload {} Dog payload) {
     }
 }
