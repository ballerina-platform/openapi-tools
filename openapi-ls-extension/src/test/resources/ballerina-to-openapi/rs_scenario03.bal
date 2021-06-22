import ballerina/http;

listener http:Listener helloEp = new (9090);
type User record {
    int id;
    string name;
};
service /payloadV on helloEp {
    resource function get pets () returns User {
    }
}
