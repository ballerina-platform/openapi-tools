import ballerina/http;

listener http:Listener helloEp = new (9090);
type Tag record {
    int id;
    string name;
};
type Pet record {
    int id;
    string name;
    Tag tag;
};
service /payloadV on helloEp {
    resource function post pets(@http:Payload{} Pet payload) returns  http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}

