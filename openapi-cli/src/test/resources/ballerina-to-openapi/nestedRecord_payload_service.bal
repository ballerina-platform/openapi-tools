//import ballerina/http;
//
//listener http:Listener helloEp = new (9090);
//
//type Pet record {
//    int id;
//    string name;
//    string tag?;
// };
//
// type Dog record {
//     Pet perant;
//     boolean bark;
//  };
// service /payloadV on helloEp {
//     resource function post hi(http:Caller caller, http:Request request, @http:Payload {} Dog payload) {
//
//     }}

public client class Client {
   public http:Client clientEp;
   public function init(string serviceUrl = "http://localhost:9090/v1", http:ClientConfiguration httpClientConfig = {}) returns error? {
       http:Client httpEp = check new (serviceUrl, httpClientConfig);
       self.clientEp = httpEp;
   }
   }
