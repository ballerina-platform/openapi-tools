import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get pets () returns record {| *http:Ok; string body;|} {
        return {body: "ooo"};
    }
}

