import ballerina/http;

listener http:Listener helloEp = new (9090);
type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

service /payloadV on helloEp {
    resource function get .() {

    }

    resource function get hi(@http:Header{} string X\-client) {

    }
    resource function put hi() returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }

    resource function get hi/[int id](int offset) returns error? {
        return;
    }

    resource function post hi() returns Pet {
        Pet pet = {
            id: 1,
            name: "abc"
        };
        return pet;
    }

    resource function post v1(@http:Payload{} Pet payload) returns http:NotFound {
        http:NotFound nf = {body: ()};
        return nf;
    }
}
