import ballerina/http;

listener http:Listener helloEp = new (9090);
type Pet record {
    int id;
    string name;
};
service /payloadV on helloEp {
    resource function post pet(@http:Payload {} record {| string oderId; string userId; byte[] fileName |} payload)
    returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
   }
}
