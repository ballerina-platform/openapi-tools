import ballerina/http;

listener http:Listener helloEp = new (9090);
type Error record {
    int id;
    string code;
};
service /payloadV on helloEp {
    resource function get pets () returns record{| *http:NotFound;  Error body;|} {
    }
}
