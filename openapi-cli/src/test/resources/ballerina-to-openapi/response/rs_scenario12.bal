import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get pets () returns record {| int id; string body;|} {
        return {id:1, body: "ooo"};
    }
}

