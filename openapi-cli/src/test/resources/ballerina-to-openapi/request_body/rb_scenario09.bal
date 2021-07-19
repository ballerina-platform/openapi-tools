import ballerina/http;

listener http:Listener helloEp = new (9090);
type Pet record {
    int id;
    string name;
};
service /payloadV on helloEp {
    resource function post ping(@http:Payload {} byte[] payload) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
